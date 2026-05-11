package client;

import dataaccess.AuthDaoSQL;
import dataaccess.DataAccessException;
import dataaccess.GameDaoSQL;
import dataaccess.UserDaoSQL;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import service.ClearService;
import client.RequestDataStructures.*;
import service.GameService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;

    //Allow interaction with serverFacade
    private static ServerFacade serverFacade;

    //Access to DAO objects ensures that server changes can be verified independent of client
    private static UserDaoSQL userDAO;
    private static AuthDaoSQL authDAO;
    private static GameDaoSQL gameDAO;
    private static void makeDaoObjects() {
        try {
            userDAO = new UserDaoSQL();
            authDAO = new AuthDaoSQL();
            gameDAO = new GameDaoSQL();
        } catch (DataAccessException neverTriggers) {}
    }

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        makeDaoObjects();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }



    @Test
    @Order(1)
    @DisplayName("doClearPositive")
    public void doClearPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        Assertions.assertTrue(userDAO.getDataSize() == 0);
        Assertions.assertTrue(authDAO.getDataSize() == 0);
        Assertions.assertTrue(gameDAO.getDataSize() == 0);

        String authToken = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email2")).authToken();
        serverFacade.doRegister(new RegisterRequest("username2", "pass2", "email2"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName1"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName2"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName3"));
        serverFacade.doLogout(authToken);
        Assertions.assertTrue(userDAO.getDataSize() == 2);
        Assertions.assertTrue(authDAO.getDataSize() == 1);
        Assertions.assertTrue(gameDAO.getDataSize() == 3);

    }

    @Test
    @Order(2)
    @DisplayName("doRegisterPositive")
    public void doRegisterPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        Assertions.assertTrue(userDAO.getDataSize() == 0);
        Assertions.assertTrue(authDAO.getDataSize() == 0);
        Assertions.assertTrue(gameDAO.getDataSize() == 0);

        serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email2")).authToken();
        serverFacade.doRegister(new RegisterRequest("username2", "pass2", "email2"));

        Assertions.assertTrue(userDAO.getDataSize() == 2);
        Assertions.assertTrue(authDAO.getDataSize() == 2);
    }

    @Test
    @Order(3)
    @DisplayName("doRegisterNegative")
    public void doRegisterNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email2")).authToken();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email1"));
        });

        assertTrue(ex.getMessage().contains("403"));
    }

    @Test
    @Order(4)
    @DisplayName("doLoginPositive")
    public void doLoginPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        String authToken = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email2")).authToken();
        serverFacade.doLogout(authToken);

        LoginResult loginResult = serverFacade.doLogin(new LoginRequest("username1", "pass1"));

        assertTrue(authDAO.getAuth(loginResult.authToken()).username().equals("username1"));
    }

    @Test
    @Order(5)
    @DisplayName("doLoginNegative")
    public void doLoginNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doLogin(new LoginRequest("username1", "pass1"));
        });

        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    @Order(6)
    @DisplayName("doLogoutPositive")
    public void doLogoutPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        LoginResult loginResult = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email1"));

        Assertions.assertTrue(userDAO.getDataSize() == 1);
        Assertions.assertTrue(authDAO.getDataSize() == 1);

        serverFacade.doLogout(loginResult.authToken());
        Assertions.assertTrue(userDAO.getDataSize() == 1);
        Assertions.assertTrue(authDAO.getDataSize() == 0);
    }

    @Test
    @Order(7)
    @DisplayName("doLogoutNegative")
    public void doLogoutNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doLogout("badToken");
        });

        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    @Order(8)
    @DisplayName("doListGamesPositive")
    public void doListGamesPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        Assertions.assertTrue(gameDAO.getDataSize() == 0);

        String authToken = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email1")).authToken();
        serverFacade.doCreateGame(authToken, new NewGameData("gameName1"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName2"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName3"));

        GameData[] allGames = serverFacade.doListGames(authToken);
        assertTrue(allGames.length == gameDAO.getDataSize());
    }

    @Test
    @Order(9)
    @DisplayName("doListGamesNegative")
    public void doListGamesNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doListGames("badToken");
        });

        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    @Order(10)
    @DisplayName("doCreateGamesPositive")
    public void doCreateGamesPositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        Assertions.assertTrue(gameDAO.getDataSize() == 0);

        String authToken = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email1")).authToken();
        serverFacade.doCreateGame(authToken, new NewGameData("gameName1"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName2"));
        serverFacade.doCreateGame(authToken, new NewGameData("gameName3"));

        assertTrue(gameDAO.getDataSize() == 3);
    }

    @Test
    @Order(11)
    @DisplayName("doCreateGamesNegative")
    public void doCreateGamesNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doCreateGame("badToken", new NewGameData("gameName"));
        });

        assertTrue(ex.getMessage().contains("401"));
    }

    @Test
    @Order(12)
    @DisplayName("doJoinGamePositive")
    public void doJoinGamePositive() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();
        Assertions.assertTrue(gameDAO.getDataSize() == 0);

        String authToken1 = serverFacade.doRegister(new RegisterRequest("username1", "pass1", "email1")).authToken();
        String authToken2 = serverFacade.doRegister(new RegisterRequest("username2", "pass2", "email2")).authToken();

        int gameID = serverFacade.doCreateGame(authToken1, new NewGameData("gameName"));
        Assertions.assertTrue(gameDAO.getDataSize() == 1);

        assertTrue(gameDAO.getGame(gameID).whiteUsername() == null);
        assertTrue(gameDAO.getGame(gameID).blackUsername() == null);

        serverFacade.doJoinGame(authToken1, new JoinGameData(PlayerColor.WHITE, gameID));
        serverFacade.doJoinGame(authToken2, new JoinGameData(PlayerColor.BLACK, gameID));
        assertTrue(gameDAO.getGame(gameID).whiteUsername().equals("username1"));
        assertTrue(gameDAO.getGame(gameID).blackUsername().equals("username2"));

    }

    @Test
    @Order(13)
    @DisplayName("doJoinGameNegative")
    public void doJoinGameNegative() throws ClientDataAccessException, DataAccessException {
        serverFacade.doClear();

        ClientDataAccessException ex = assertThrows(ClientDataAccessException.class, () -> {
            serverFacade.doJoinGame("badToken", new JoinGameData(PlayerColor.WHITE, 1));
        });

        assertTrue(ex.getMessage().contains("401"));
    }

}
