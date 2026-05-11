package service;

import org.junit.jupiter.api.*;
import dataaccess.*;
import service.RequestDataStructures.*;
import model.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private static UserDaoSQL userDAO;
    private static AuthDaoSQL authDAO;
    private static GameDaoSQL gameDAO;

    private static void makeDaoObjects() throws DataAccessException {
        userDAO = new UserDaoSQL();
        authDAO = new AuthDaoSQL();
        gameDAO = new GameDaoSQL();
    }



    @Test
    @Order(1)
    @DisplayName("clearTest")
    public void clearTest() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        userDAO.createUser(new UserData("username", "password", "email"));
        authDAO.createAuth(new AuthData("token", "username"));
        gameDAO.createGame(1, "name");

        ClearService.clear();

        Assertions.assertEquals(0, userDAO.getDataSize());
        Assertions.assertEquals(0, authDAO.getDataSize());
        Assertions.assertEquals(0, gameDAO.getDataSize());
    }

    @Test
    @Order(2)
    @DisplayName("registerPositive")
    public void registerPositive() throws DataAccessException {
        ClearService.clear();
        assertDoesNotThrow(() -> {
            UserService.register(new RegisterRequest("username", "password", "email"));
        });

        Assertions.assertEquals((new LoginResult("username", "authToken")).username(),
                ((new RegisterRequest("username", "password", "email"))).username());

    }

    @Test
    @Order(3)
    @DisplayName("registerNegative")
    public void registerNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            UserService.register(new RegisterRequest("username", "password", null));
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("loginPositive")
    public void loginPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        assertDoesNotThrow(() -> {
            UserService.register(new RegisterRequest("username", "password", "email"));
        });

        assertDoesNotThrow(() -> {
            UserService.login(new LoginRequest("username", "password"));
        });

        Assertions.assertEquals((new LoginResult("username", "authToken")).username(), ((new LoginRequest("username", "password"))).username());
    }

    @Test
    @Order(5)
    @DisplayName("loginNegative")
    public void loginNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            UserService.login(new LoginRequest("username", null));
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("logoutPositive")
    public void logoutPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));

        assertDoesNotThrow(() -> {
            UserService.logout("token");
        });
        Assertions.assertEquals(0, authDAO.getDataSize());
    }

    @Test
    @Order(7)
    @DisplayName("logoutNegative")
    public void logoutNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            UserService.logout(null);
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }

    @Test
    @Order(8)
    @DisplayName("listGamesPositive")
    public void listGamesPositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        gameDAO.createGame(1, "game");

        assertDoesNotThrow(() -> {
            GameService.listGames("token");
        });
        Assertions.assertEquals(1, GameService.listGames("token").length);
    }

    @Test
    @Order(9)
    @DisplayName("listGamesNegative")
    public void listGamesNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            GameService.listGames(null);
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }

    @Test
    @Order(10)
    @DisplayName("createGamePositive")
    public void createGamePositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        assertDoesNotThrow(() -> {
            GameService.createGame("token", "game");
        });
        Assertions.assertEquals(1, gameDAO.getDataSize());
    }

    @Test
    @Order(11)
    @DisplayName("createGameNegative")
    public void createGameNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            GameService.createGame(null, "game");
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }

    @Test
    @Order(12)
    @DisplayName("joinGamePositive")
    public void joinGamePositive() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        authDAO.createAuth(new AuthData("token", "username"));
        gameDAO.createGame(1, "game");

        assertDoesNotThrow(() -> {
            GameService.joinGame("token", new JoinGameData(PlayerColor.WHITE, 1));
        });
        Assertions.assertEquals("username", gameDAO.getGame(1).whiteUsername());
    }

    @Test
    @Order(13)
    @DisplayName("joinGameNegative")
    public void joinGameNegative() throws DataAccessException {
        ClearService.clear();
        makeDaoObjects();

        Exception ex = assertThrows(DataAccessException.class, () -> {
            GameService.joinGame(null, new JoinGameData(PlayerColor.BLACK, 1));
        });

        String expected = "Error: bad request";
        assertTrue(expected.equals(ex.getMessage()));
    }
}
