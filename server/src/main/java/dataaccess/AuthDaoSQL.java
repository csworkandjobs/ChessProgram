package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import model.AuthData;

public class AuthDaoSQL {
    private static Gson gson = new Gson();
    private static final String[] CREATE_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS  auths (
              `authToken` varchar(256) NOT NULL,
              `jsonAuthData` TEXT DEFAULT NULL,
              PRIMARY KEY (`authToken`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public AuthDaoSQL() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : CREATE_STATEMENTS) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }
    }

    public static void clearAuths() throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("DROP table auths")) {
            //try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM auths")) {
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }
    }

    public static AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection(); ) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM auths")) {
                ResultSet rs = preparedStatement.executeQuery();
                String databaseAuthToken;
                while(rs.next()) {
                    databaseAuthToken = rs.getString(1);
                    if(authToken.equals(databaseAuthToken)) {
                        return gson.fromJson(rs.getString(2), AuthData.class);
                    }
                }
            }
        } catch(SQLException ex) {
            throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }
        return null;
    }

    public static void createAuth(AuthData authData) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO auths VALUES (?, ?)")) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, gson.toJson(authData));
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }
    }

    public static void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection(); ) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM auths WHERE authToken = ?")) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch(SQLException ex) {
            throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }
    }



    //Used for testing purposes
    public static int getDataSize() throws DataAccessException {
        int tableLen;
        try (Connection conn = DatabaseManager.getConnection(); ) {
            try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) FROM auths")) {
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                tableLen = rs.getInt(1);
            }
        }  catch(SQLException ex) {
            tableLen = 0;
            //throw new DataAccessException(DataAccessException.FailCode.server, ex.getMessage());
        }

        return tableLen;
    }
}
