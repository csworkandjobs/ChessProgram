package service;

import service.RequestDataStructures.*;
import dataaccess.UserDaoSQL;
import dataaccess.AuthDaoSQL;
import dataaccess.DataAccessException;
import dataaccess.DataAccessException.FailCode;
import model.*;
import java.util.UUID;
import org.mindrot.jbcrypt.*;

public class UserService {
    public static LoginResult register(RegisterRequest registerRequest) throws DataAccessException {
        UserDaoSQL userDAO = new UserDaoSQL();

        //Ensure inputs are good
        validateInputs(new String[]{registerRequest.username(), registerRequest.password(), registerRequest.email()});

        if(registerRequest.username() == null ||
                registerRequest.password() == null ||
                registerRequest.email() == null) {

        }

        //If username already exists, throw an error
        UserData userData = userDAO.getUser(registerRequest.username());
        if(userData != null) {
            throw new DataAccessException(FailCode.alreadyTaken);
        }

        //If username is available, make new user
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        userData = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        userDAO.createUser(userData);

        //Login to the new account and return
        LoginRequest loginRequest = new LoginRequest(registerRequest.username(), registerRequest.password());
        return login(loginRequest);
    }

    public static LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserDaoSQL userDAO = new UserDaoSQL();
        AuthDaoSQL authDAO = new AuthDaoSQL();

        //Ensure inputs are good
        validateInputs(new String[]{loginRequest.username(), loginRequest.password()});

        //If username does not exist, throw an error
        UserData userData = userDAO.getUser(loginRequest.username());
        if(userData == null) {
            throw new DataAccessException(FailCode.unauthorized);
        }

        //Check password matches
        if(!(BCrypt.checkpw(loginRequest.password(), userData.password()))) {
            throw new DataAccessException(FailCode.unauthorized);
        }

        //Create new authData
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        authDAO.createAuth(authData);
        return new LoginResult(userData.username(), authToken);
    }

    public static void logout(String authToken) throws DataAccessException {
        AuthDaoSQL authDAO = new AuthDaoSQL();

        //Ensure inputs are good
        validateInputs(new String[]{authToken});

        //Verify authToken exists
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null) {
            throw new DataAccessException(FailCode.unauthorized);
        }

        //If authToken is real, proceed to delete it
        authDAO.deleteAuth(authToken);
    }



    //Helper functions

    //If any given inputs are null, the request is invalid
    private static void validateInputs(String[] inputs) throws DataAccessException {
        for(String input : inputs) {
            if(input == null) {
                throw new DataAccessException(FailCode.badRequest);
            }
        }
    }
}
