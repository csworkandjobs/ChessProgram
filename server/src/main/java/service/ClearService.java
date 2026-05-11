package service;

import dataaccess.*;

public class ClearService {
    public static void clear() throws DataAccessException {
        AuthDaoSQL authDAO = new AuthDaoSQL();
        GameDaoSQL gameDAO = new GameDaoSQL();
        UserDaoSQL userDAO = new UserDaoSQL();

        authDAO.clearAuths();
        gameDAO.clearGames();
        userDAO.clearUsers();
    }
}
