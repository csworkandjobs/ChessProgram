package dataaccess;

import org.junit.jupiter.api.*;
import dataaccess.*;
import service.RequestDataStructures.*;
import model.*;
import service.ClearService;
import chess.ChessGame;

import static org.junit.jupiter.api.Assertions.*;

public class DaoTests {
    private static UserDaoSQL userDAO;
    private static AuthDaoSQL authDAO;
    private static GameDaoSQL gameDAO;

    private static void makeDaoObjects() throws DataAccessException {
        userDAO = new UserDaoSQL();
        authDAO = new AuthDaoSQL();
        gameDAO = new GameDaoSQL();
    }



    // UserDAO tests
    @Test
    @Order(1)
    @DisplayName("clearUsersPositive")
    public void clearUsersPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        Assertions.assertTrue(1 == userDAO.getDataSize());

        userDAO.clearUsers();
        Assertions.assertTrue(0 == userDAO.getDataSize());
    }

    @Test
    @Order(2)
    @DisplayName("clearUsersNegative")
    public void clearUsersNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        userDAO.clearUsers();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
                userDAO.clearUsers();
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("getUserPositive")
    public void getUserPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        Assertions.assertTrue(1 == userDAO.getDataSize());

        UserData userData = userDAO.getUser("username");
        Assertions.assertTrue(userData.equals(new UserData("username", "password", "email")));
    }

    @Test
    @Order(4)
    @DisplayName("getUserNegative")
    public void getUserNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        Assertions.assertTrue(1 == userDAO.getDataSize());

        //Simulate database deletion during execution
        userDAO.clearUsers();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("username");
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("createUserPositive")
    public void createUserPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        Assertions.assertTrue(1 == userDAO.getDataSize());
    }

    @Test
    @Order(6)
    @DisplayName("createUserNegative")
    public void createUserNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        Assertions.assertTrue(1 == userDAO.getDataSize());

        //Simulate database deletion during execution
        userDAO.clearUsers();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("username", "password", "email"));
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }



    // AuthDAO
    @Test
    @Order(7)
    @DisplayName("clearAuthsPositive")
    public void clearAuthsPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        authDAO.clearAuths();
        Assertions.assertTrue(0 == authDAO.getDataSize());
    }

    @Test
    @Order(8)
    @DisplayName("clearAuthsNegative")
    public void clearAuthsNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        authDAO.clearAuths();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            authDAO.clearAuths();
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(9)
    @DisplayName("getAuthPositive")
    public void getAuthPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        AuthData authData = authDAO.getAuth("token");
        Assertions.assertTrue(authData.equals(new AuthData("token", "username")));
    }

    @Test
    @Order(10)
    @DisplayName("getAuthNegative")
    public void getAuthNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        //Simulate database deletion during execution
        authDAO.clearAuths();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("token");
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(11)
    @DisplayName("createAuthPositive")
    public void createAuthPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());
    }

    @Test
    @Order(12)
    @DisplayName("createAuthNegative")
    public void createAuthNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        //Simulate database deletion during execution
        authDAO.clearAuths();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(new AuthData("token", "username"));
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(13)
    @DisplayName("deleteAuthPositive")
    public void deleteAuthPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        authDAO.deleteAuth("token");
        Assertions.assertTrue(0 == authDAO.getDataSize());
    }

    @Test
    @Order(14)
    @DisplayName("deleteAuthNegative")
    public void deleteAuthNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        Assertions.assertTrue(1 == authDAO.getDataSize());

        //Simulate database deletion during execution
        authDAO.clearAuths();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            authDAO.deleteAuth("token");
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }



    // GameDAO
    @Test
    @Order(15)
    @DisplayName("clearGamesPositive")
    public void clearGamesPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        gameDAO.createGame(1, "game");
        Assertions.assertTrue(1 == gameDAO.getDataSize());

        gameDAO.clearGames();
        Assertions.assertTrue(0 == gameDAO.getDataSize());
    }

    @Test
    @Order(16)
    @DisplayName("clearGamesNegative")
    public void clearGamesNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        gameDAO.clearGames();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            gameDAO.clearGames();
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(17)
    @DisplayName("listGamesPositive")
    public void listGamesPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        gameDAO.createGame(1, "game1");
        Assertions.assertTrue(1 == gameDAO.getDataSize());

        gameDAO.createGame(2, "game2");
        Assertions.assertTrue(2 == gameDAO.getDataSize());

        GameData[] gameDataArray = gameDAO.listGames();

        GameData[] expectedDataArray = new GameData[]{
                new GameData(1, null, null, "game1", new ChessGame(), false, false, false),
                new GameData(2, null, null, "game2", new ChessGame(), false, false, false)};

        Assertions.assertEquals(gameDataArray[0].gameID(), expectedDataArray[0].gameID());
        Assertions.assertEquals(gameDataArray[0].whiteUsername(), expectedDataArray[0].whiteUsername());
        Assertions.assertEquals(gameDataArray[0].blackUsername(), expectedDataArray[0].blackUsername());
        Assertions.assertEquals(gameDataArray[0].gameName(), expectedDataArray[0].gameName());
        Assertions.assertEquals(gameDataArray[0].game(), expectedDataArray[0].game());

        Assertions.assertEquals(gameDataArray[1].gameID(), expectedDataArray[1].gameID());
        Assertions.assertEquals(gameDataArray[1].whiteUsername(), expectedDataArray[1].whiteUsername());
        Assertions.assertEquals(gameDataArray[1].blackUsername(), expectedDataArray[1].blackUsername());
        Assertions.assertEquals(gameDataArray[1].gameName(), expectedDataArray[1].gameName());
        Assertions.assertEquals(gameDataArray[1].game(), expectedDataArray[1].game());

    }

    @Test
    @Order(18)
    @DisplayName("listGamesNegative")
    public void listGamesNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        gameDAO.clearGames();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            gameDAO.listGames();
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(19)
    @DisplayName("getGamePositive")
    public void getGamePositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        gameDAO.createGame(1, "game");
        Assertions.assertTrue(1 == gameDAO.getDataSize());

        GameData gameData = gameDAO.getGame(1);
        Assertions.assertTrue(gameData.equals(new GameData(1, null, null, "game", new ChessGame(), false, false, false)));
    }

    @Test
    @Order(20)
    @DisplayName("getGameNegative")
    public void getGameNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        gameDAO.clearGames();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(1);
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(21)
    @DisplayName("createGamePositive")
    public void createGamePositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        gameDAO.createGame(1, "game");
        Assertions.assertTrue(1 == gameDAO.getDataSize());
    }

    @Test
    @Order(22)
    @DisplayName("createGameNegative")
    public void createGameNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        gameDAO.clearGames();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(1, "game");
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }

    @Test
    @Order(23)
    @DisplayName("updateGamePositive")
    public void updateGamePositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        gameDAO.createGame(1, "game");
        Assertions.assertTrue(1 == gameDAO.getDataSize());

        gameDAO.updateGame(1, "username", PlayerColor.BLACK);

        GameData gameData = gameDAO.getGame(1);
        Assertions.assertEquals(gameData, new GameData(1, null, "username", "game", new ChessGame(), false, false, false));
    }

    @Test
    @Order(24)
    @DisplayName("updateGameNegative")
    public void updateGameNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        //Simulate database deletion during execution
        gameDAO.clearGames();
        DataAccessException ex = assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(1, "username", PlayerColor.BLACK);
        });

        Assertions.assertEquals(500, ex.toHttpStatusCode());
    }
}
