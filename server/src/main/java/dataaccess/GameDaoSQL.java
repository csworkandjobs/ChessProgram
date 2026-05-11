package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import dataaccess.DataAccessException.FailCode;

import java.sql.*;

import chess.ChessGame;
import service.RequestDataStructures.PlayerColor;

public class GameDaoSQL {
    private static Gson gson = new Gson();
    private static final String[] CREATE_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS  games (
              `gameID` int NOT NULL,
              `jsonGameData` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public GameDaoSQL() throws DataAccessException{
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : CREATE_STATEMENTS) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
    }

    public static void clearGames() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("DROP table games")) {
            //try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM games")) {
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
    }

    public static GameData[] listGames() throws DataAccessException {
        GameData[] allGameData = new GameData[0];
        try (Connection conn = DatabaseManager.getConnection(); ) {
            int tableLen;
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM games")) {
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                try {
                    tableLen = rs.getInt(1);
                } catch (Exception e) {
                    tableLen = 0;
                }
            }

            allGameData = new GameData[tableLen];

            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM games")) {
                ResultSet rs = preparedStatement.executeQuery();
                String databaseUsername;
                int i = 0;
                while(rs.next()) {
                    allGameData[i] = gson.fromJson(rs.getString(2), GameData.class);
                    i++;
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }

        return allGameData;
    }

    public static GameData getGame(int id) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection(); ) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM games")) {
                ResultSet rs = preparedStatement.executeQuery();
                int databaseGameID;
                while(rs.next()) {
                    databaseGameID = rs.getInt(1);
                    if(id == databaseGameID) {
                        return gson.fromJson(rs.getString(2), GameData.class);
                    }
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
        return null;
    }

    public static GameData createGame(int id, String gameName) throws DataAccessException {
        GameData newGame = new GameData(id, null, null, gameName, new ChessGame(), false, false, false);
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO games VALUES (?, ?)")) {
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, gson.toJson(newGame));
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }

        return newGame;
    }

    public static void updateGame(int id, GameData modifiedGameData) throws DataAccessException {
        String newGameJson = gson.toJson(modifiedGameData);
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("UPDATE games SET jsonGameData = ? WHERE gameID = ?")) {
                preparedStatement.setString(1, newGameJson);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
    }

    public static void updateGame(int id, String username, PlayerColor playerColor) throws DataAccessException {
        GameData existingGame = getGame(id);
        GameData newGame;
        if(playerColor == PlayerColor.WHITE) { //If Player is white, copy over everything, replacing whiteUsername
            newGame = new GameData(id, username, existingGame.blackUsername(), existingGame.gameName(),
                    existingGame.game(), existingGame.isResigned(), existingGame.isStalemate(),
                    existingGame.isCheckmate());
        } else { //If Player is black, copy over everything, replacing blackUsername
            newGame = new GameData(id, existingGame.whiteUsername(), username, existingGame.gameName(),
                    existingGame.game(), existingGame.isResigned(), existingGame.isStalemate(),
                    existingGame.isCheckmate());
        }

        String newGameJson = gson.toJson(newGame);
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("UPDATE games SET jsonGameData = ? WHERE gameID = ?")) {
                preparedStatement.setString(1, newGameJson);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
    }



    //Used for testing purposes
    public static int getDataSize() throws DataAccessException {
        int tableLen;
        try (Connection conn = DatabaseManager.getConnection(); ) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM games")) {
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                tableLen = rs.getInt(1);
            }
        }  catch(SQLException ex) {
            tableLen = 0;
            //throw new DataAccessException(FailCode.server, ex.getMessage());
        }

        return tableLen;
    }
}
