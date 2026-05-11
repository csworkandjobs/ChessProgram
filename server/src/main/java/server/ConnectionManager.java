package server;

import org.eclipse.jetty.websocket.api.Session;
import model.GameData;

import java.util.HashMap;
import java.util.ArrayList;
import dataaccess.*;
import chess.ChessGame.TeamColor;
import service.RequestDataStructures.PlayerColor;
import java.io.IOException;


public class ConnectionManager {
    private static HashMap<Integer, ArrayList<Session>> connections = new HashMap<Integer, ArrayList<Session>>();

    public void add(int gameID, Session client) {
        //Get the array of sessions for current game
        ArrayList<Session> gameClients = connections.get(gameID);

        //If game is not yet in active use, gameClients will be null.  Fix this by creating a new list.
        if(gameClients == null) {
            gameClients = new ArrayList<Session>();
        }

        //Add new client to list, then replace old session list with new session list.
        gameClients.add(client);
        connections.put(gameID, gameClients);
    }

    public void remove(GameData targetGame) {
        connections.remove(targetGame.gameID());
    }

    //When a player leave a game, remove their connection from the assocaited game.
    public void exit(GameData gameData, String username, Session client) throws DataAccessException {
        //Get the array of sessions for current game.
        ArrayList<Session> gameClients = connections.get(gameData.gameID());

        //Remove the client from the GameData in the database if client is a player
        if(username.equals(gameData.blackUsername())) {
            GameDaoSQL.updateGame(gameData.gameID(), null, PlayerColor.BLACK);
        } else if(username.equals(gameData.whiteUsername())) {
            GameDaoSQL.updateGame(gameData.gameID(), null, PlayerColor.WHITE);
        }

        //Remove client from list, then replace old session list with new session list.
        gameClients.remove(client);
        connections.put(gameData.gameID(), gameClients);
    }

    public void broadcast(int gameID, Session excludeSession, String message) throws IOException{
        for(Session client : connections.get(gameID)) {
            if(client.isOpen()) {
                if(!client.equals(excludeSession)) {
                    client.getRemote().sendString(message);
                }
            }
        }
    }
}
