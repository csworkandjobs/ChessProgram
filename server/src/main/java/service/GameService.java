package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessException.FailCode;
import dataaccess.AuthDaoSQL;
import model.*;
import dataaccess.*;
import service.RequestDataStructures.JoinGameData;
import service.RequestDataStructures.PlayerColor;


public class GameService {
    private static int nextGameID = 1;

    public static GameData[] listGames(String authToken) throws DataAccessException {
        GameDaoSQL gameDAO = new GameDaoSQL();
        validateAuthToken(authToken);

        //Return all game data
        return gameDAO.listGames();
    }

    public static GameData createGame(String authToken, String gameName) throws DataAccessException {
        GameDaoSQL gameDAO = new GameDaoSQL();

        validateAuthToken(authToken);

        //Verify the game name exists
        if(gameName == null) {
            throw new DataAccessException(FailCode.badRequest);
        }

        //Find an unused gameID (typically should never actually loop unless system has been rebooted)
        int gameID;
        boolean gameIDCreated = false;
        do{
            gameID = nextGameID;
            nextGameID++;
            GameData checkForGame = gameDAO.getGame(gameID);
            if(checkForGame == null) {
                gameIDCreated = true;
            }
        } while(!gameIDCreated);

        //Create game
        return gameDAO.createGame(gameID, gameName);
    }

    public static void joinGame(String authToken, JoinGameData joinData) throws DataAccessException {
        GameDaoSQL gameDAO = new GameDaoSQL();

        String username = validateAuthToken(authToken).username();

        //Verify that stated data exists
        if(joinData.playerColor() == null || joinData.gameID() <= 0) {
            throw new DataAccessException(FailCode.badRequest);
        }

        //Verify gameID exists
        GameData game = gameDAO.getGame(joinData.gameID());
        if(game == null) {
            throw new DataAccessException(FailCode.alreadyTaken);
        }

        //Check that the desired playerColor is unoccupied
        checkColorAvailability(game, joinData.playerColor());

        //Update game
        gameDAO.updateGame(joinData.gameID(), username, joinData.playerColor());
    }



    //Helper functions
    public static AuthData validateAuthToken(String authToken) throws DataAccessException {
        GameDaoSQL gameDAO = new GameDaoSQL();
        AuthDaoSQL authDAO = new AuthDaoSQL();

        //Verify authToken exists
        if(authToken == null) {
            throw new DataAccessException(FailCode.badRequest);
        }

        //Verify authToken is registered
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null) {
            throw new DataAccessException(FailCode.unauthorized);
        }

        return authData; //Return is only used by joinGame, ignored by listGames and createGame.
    }

    //Verify that desired color is unoccupied, throws an error if not
    private static void checkColorAvailability(GameData game, PlayerColor playerColor) throws DataAccessException {
        if(playerColor == PlayerColor.WHITE && game.whiteUsername() != null) {
            throw new DataAccessException(FailCode.alreadyTaken);
        } else if(playerColor== PlayerColor.BLACK && game.blackUsername() != null) {
            throw new DataAccessException(FailCode.alreadyTaken);
        }
    }
}
