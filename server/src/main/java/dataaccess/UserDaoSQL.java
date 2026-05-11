package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import dataaccess.DataAccessException.FailCode;

import java.sql.*;

public class UserDaoSQL {
    private static Gson gson = new Gson();
    private static final String[] CREATE_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `username` varchar(256) NOT NULL,
              `jsonUserData` TEXT DEFAULT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public UserDaoSQL() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : CREATE_STATEMENTS) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException exp) {
            throw new DataAccessException(FailCode.server, exp.getMessage());
        }
    }

    public static void clearUsers() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("DROP table users")) {
            //try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM users")) {
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
    }

    public static UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM users")) {
                ResultSet rs = preparedStatement.executeQuery();
                String databaseUsername;
                while(rs.next()) {
                    databaseUsername = rs.getString(1);
                    if(username.equals(databaseUsername)) {
                        return gson.fromJson(rs.getString(2), UserData.class);
                    }
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException(FailCode.server, ex.getMessage());
        }
        return null;
    }

    public static void createUser(UserData userData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO users VALUES (?, ?)")) {
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, gson.toJson(userData));
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
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
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
