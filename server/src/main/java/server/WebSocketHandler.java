package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import model.*;
import dataaccess.*;
import chess.ChessGame.TeamColor;
import websocket.messages.*;
import chess.*;


public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    ConnectionManager connections = new ConnectionManager();
    Gson gson = new Gson();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();

    }

    @Override
    public void handleMessage(WsMessageContext ctx) throws IOException {
        try {
            UserGameCommand clientMessage = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (clientMessage.getCommandType()) {
                case CONNECT:
                    joinGame(clientMessage, ctx.session);
                    break;
                case MAKE_MOVE:
                    MakeMoveCommand moveMessage = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(moveMessage, ctx.session);
                    break;
                case LEAVE:
                    leaveGame(clientMessage, ctx.session);
                    break;
                case RESIGN:
                    resignGame(clientMessage, ctx.session);
                    break;
            }
        } catch (DataAccessException ex) {

            //ctx.session.getRemote().sendString(gson.toJson(ex));
        } catch(Exception ex) {
            //DataAccessException serverError = new DataAccessException(DataAccessException.FailCode.server);
            //ctx.session.getRemote().sendString(gson.toJson(serverError));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        //HOW TO FIND AUTHTOKEN
        //HOW TO FIND GAMEID
        //UserGameCommand closeMessage = new UserGameCommand(UserGameCommand.CommandType.LEAVE, null, GAMEID);
        //leaveGame(closeMessage, ctx.session);
    }


    //Handler classes
    public void joinGame(UserGameCommand clientMessage, Session clientSession) throws DataAccessException, IOException {
        //Build data
        AuthData authData = AuthDaoSQL.getAuth(clientMessage.getAuthToken());
        GameData gameData = GameDaoSQL.getGame(clientMessage.getGameID());

        //Verify game exists
        if(gameData == null) {
            String message = "Error: That game does not exist!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Verifying user exists
        if(authData == null) {
            String message = "Error: User not recognized!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Find out kind of user
        String clientType = "Observer";
        if(authData.username().equals(gameData.blackUsername())) {
            clientType = "Black Player";
        } else if(authData.username().equals(gameData.whiteUsername())) {
            clientType = "White Player";
        }

        //Join game
        connections.add(clientMessage.getGameID(), clientSession);


        //Send notification to all other clients
        String message = String.format("%s joined the game as %s.", authData.username(), clientType);
        connections.broadcast(clientMessage.getGameID(), clientSession,
                gson.toJson(new NotificationMessage(message, ServerMessage.ServerMessageType.NOTIFICATION)));

        //Load game for new client
        LoadGameMessage loadGame = new LoadGameMessage(gameData, ServerMessage.ServerMessageType.LOAD_GAME);
        clientSession.getRemote().sendString(gson.toJson(loadGame));
    }

    public void makeMove(MakeMoveCommand moveMessage, Session clientSession) throws DataAccessException, IOException, InvalidMoveException {
        //Get user and game data
        AuthData authData = AuthDaoSQL.getAuth(moveMessage.getAuthToken());
        GameData gameData = GameDaoSQL.getGame(moveMessage.getGameID());

        if(gameData == null) { //Verify game exists
            String message = "Error: That game does not exist!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        if(authData == null) { //Verifying user exists
            String message = "Error: User not recognized!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Verify game is not already over
        if(gameData.isResigned()) { //ERROR - game has been resigned
            ErrorMessage failMessage = new ErrorMessage("Error: Game has already been resigned!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        } else if(gameData.isCheckmate()) { //ERROR - checkmate has occurred
            ErrorMessage failMessage = new ErrorMessage("Error: Game is already in checkmate!  Game over!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        } else if(gameData.isStalemate()) { //ERROR - stalemate has occurred
            ErrorMessage failMessage = new ErrorMessage("Error: Game is already in stalemate!  Game over!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Find the user's team color
        TeamColor playerColor = null;
        TeamColor opponentColor = null;
        if(authData.username().equals(gameData.blackUsername())) {
            playerColor = TeamColor.BLACK;
            opponentColor = TeamColor.WHITE;
        } else if(authData.username().equals(gameData.whiteUsername())) {
            playerColor = TeamColor.WHITE;
            opponentColor = TeamColor.BLACK;
        } else { //ERROR - observer cannot make move
            ErrorMessage failMessage = new ErrorMessage("Error: Observers cannot make moves!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        if(!gameData.game().getTeamTurn().equals(playerColor)) { //ERROR - not your turn
            ErrorMessage failMessage = new ErrorMessage("Error: It's not your turn!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Make move
        GameData modifiedGameData = gameData;
        try {
            modifiedGameData.game().makeMove(moveMessage.getMove());
        } catch(Exception ex) {
            ErrorMessage failMessage = new ErrorMessage("Error: Invalid move!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Check for stalemate or checkmate
        if(modifiedGameData.game().isInCheckmate(opponentColor)) {
            modifiedGameData = new GameData(modifiedGameData.gameID(), modifiedGameData.whiteUsername(),
                    modifiedGameData.blackUsername(), modifiedGameData.gameName(), modifiedGameData.game(),
                    modifiedGameData.isResigned(), modifiedGameData.isStalemate(), true);
            String message = String.format("%s is in check. The game is over.", authData.username());
            connections.broadcast(moveMessage.getGameID(), null,
                    gson.toJson(new NotificationMessage(message, ServerMessage.ServerMessageType.NOTIFICATION)));
        } else if(modifiedGameData.game().isInStalemate(opponentColor)) {
            modifiedGameData = new GameData(modifiedGameData.gameID(), modifiedGameData.whiteUsername(),
                    modifiedGameData.blackUsername(), modifiedGameData.gameName(), modifiedGameData.game(),
                    modifiedGameData.isResigned(), true, modifiedGameData.isCheckmate());
            String message = String.format("%s is in stalemate. The game is over.", authData.username());
            connections.broadcast(moveMessage.getGameID(), null,
                    gson.toJson(new NotificationMessage(message, ServerMessage.ServerMessageType.NOTIFICATION)));
        }

        //Update game
        GameDaoSQL.updateGame(gameData.gameID(), modifiedGameData);
        connections.broadcast(moveMessage.getGameID(), null,
                gson.toJson(new LoadGameMessage(modifiedGameData, ServerMessage.ServerMessageType.LOAD_GAME)));

        //Notify users of move
        GameDaoSQL.updateGame(gameData.gameID(), modifiedGameData);
        String message = moveMessageGenerator(authData.username(), moveMessage.getMove());
        connections.broadcast(moveMessage.getGameID(), clientSession,
                gson.toJson(new NotificationMessage(message,
                        ServerMessage.ServerMessageType.NOTIFICATION)));
    }

    public String moveMessageGenerator(String username, ChessMove move) {
        String fromCol = indexToLetter(move.getStartPosition().getColumn());
        String fromRow = indexToString(move.getStartPosition().getRow());
        String toCol = indexToLetter(move.getEndPosition().getColumn());
        String toRow = indexToString(move.getEndPosition().getRow());
        return String.format("%s has moved %s%s to %s%s!", username, fromCol, fromRow, toCol, toRow);
    }

    public String indexToLetter(int input) {
        return Character.toString((char)(input+96));
    }

    public String indexToString(int input) {
        return Character.toString((char)(input+48));
    }

    public void leaveGame(UserGameCommand clientMessage, Session clientSession) throws DataAccessException, IOException {
        //Construct data for broadcast
        AuthData authData = AuthDaoSQL.getAuth(clientMessage.getAuthToken());
        GameData gameData = GameDaoSQL.getGame(clientMessage.getGameID());

        //Identify what king of user is leaving
        String clientType = "Observer";
        if(authData.username().equals(gameData.blackUsername())) {
            clientType = "Black Player";
        } else if(authData.username().equals(gameData.whiteUsername())) {
            clientType = "White Player";
        }

        //Broadcast that the user has left the game
        String message = String.format("%s %s has left the game.", clientType, authData.username());
        connections.exit(gameData, authData.username(), clientSession);
        connections.broadcast(clientMessage.getGameID(), clientSession,
                gson.toJson(new NotificationMessage(message, ServerMessage.ServerMessageType.NOTIFICATION)));
    }

    public void resignGame(UserGameCommand clientMessage, Session clientSession) throws DataAccessException, IOException{
        //Get user and game data
        AuthData authData = AuthDaoSQL.getAuth(clientMessage.getAuthToken());
        GameData gameData = GameDaoSQL.getGame(clientMessage.getGameID());

        //Verify game exists, if not send back a server error
        if(gameData == null) {
            String message = "Error: That game does not exist!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Verifying user exists, if not send back a server error
        if(authData == null) {
            String message = "Error: User not recognized!";
            ErrorMessage failMessage = new ErrorMessage(message, ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Verify game is not already over by some means
        if(gameData.isResigned()) {
            //ERROR - game has been resigned
            ErrorMessage failMessage = new ErrorMessage("Error: Game has already been resigned!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        } else if(gameData.isCheckmate()) {
            //ERROR - checkmate has occurred, cant resign
            ErrorMessage failMessage = new ErrorMessage("Error: Game is already in checkmate!  Game over!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        } else if(gameData.isStalemate()) {
            //ERROR - stalemate has occurred, cant resign
            ErrorMessage failMessage = new ErrorMessage("Error: Game is already in stalemate!  Game over!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Find the user's team color
        TeamColor playerColor = null;
        String opponentUsername = null;
        if(authData.username().equals(gameData.blackUsername())) {
            playerColor = TeamColor.BLACK;
            opponentUsername = gameData.whiteUsername();
        } else if(authData.username().equals(gameData.whiteUsername())) {
            playerColor = TeamColor.WHITE;
            opponentUsername = gameData.blackUsername();
        } else {
            //ERROR - observer cannot resign
            ErrorMessage failMessage = new ErrorMessage("Error: Observers cannot resign!",
                    ServerMessage.ServerMessageType.ERROR);
            clientSession.getRemote().sendString(gson.toJson(failMessage));
            return;
        }

        //Send notification of resignation
        String message = String.format("%s has resigned, %s wins!", authData.username(), opponentUsername);
        connections.broadcast(clientMessage.getGameID(), null,
                gson.toJson(new NotificationMessage(message, ServerMessage.ServerMessageType.NOTIFICATION)));

        //Update game
        GameData modifiedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                gameData.blackUsername(), gameData.gameName(), gameData.game(),
                true, gameData.isStalemate(), gameData.isCheckmate());
        GameDaoSQL.updateGame(gameData.gameID(), modifiedGameData);
//        connections.broadcast(clientMessage.getGameID(), null,
//                gson.toJson(new LoadGameMessage(modifiedGameData.game(), ServerMessage.ServerMessageType.LOAD_GAME)));
    }
}
