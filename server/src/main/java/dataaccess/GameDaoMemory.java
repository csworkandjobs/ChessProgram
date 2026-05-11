package dataaccess;

import java.util.HashMap;
import model.GameData;
import chess.ChessGame;
import service.RequestDataStructures.PlayerColor;

public class GameDaoMemory {
    private static HashMap<Integer, GameData> gameDataSet = new HashMap<>();

    public static void clearGames() {
        gameDataSet.clear();
    }

    public static GameData[] listGames() {
        int gameDataSetSize = gameDataSet.size();
        GameData[] allGameData = new GameData[gameDataSetSize];

        int i = 0;
        for(GameData gameData : gameDataSet.values()) {
            allGameData[i] = gameData;
            i++;
        }

        return allGameData;
    }

    public static GameData getGame(int id) {
        return gameDataSet.get(id);
    }

    public static GameData createGame(int id, String gameName) {
        GameData newGame = new GameData(id, null, null, gameName, new ChessGame(), false, false, false);
        gameDataSet.put(id, newGame);
        return newGame;
    }

    public static void updateGame(int id, String username, PlayerColor playerColor) {
        GameData oldGame = getGame(id);
        GameData newGame;
        if(playerColor == PlayerColor.WHITE) {
            newGame = new GameData(id, username, oldGame.blackUsername(), oldGame.gameName(), oldGame.game(),
                    oldGame.isResigned(), oldGame.isStalemate(), oldGame.isCheckmate());
        } else {
            newGame = new GameData(id, oldGame.whiteUsername(), username, oldGame.gameName(), oldGame.game(),
                    oldGame.isResigned(), oldGame.isStalemate(), oldGame.isCheckmate());
        }
        gameDataSet.put(id, newGame);
    }



    //Used for testing purposes
    public static int getDataSize() {
        return gameDataSet.size();
    }
}
