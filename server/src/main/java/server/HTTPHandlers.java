package server;

import io.javalin.http.Context;
import dataaccess.DataAccessException;
import service.*;
import service.RequestDataStructures.*;
import com.google.gson.Gson;
import model.GameData;

import java.util.Map;

public class HTTPHandlers {
    private static Gson gson = new Gson();

    static void clear(Context ctx) throws DataAccessException {
        try {
            ClearService.clear();
        } catch(DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    static void register(Context ctx) throws DataAccessException {
        try {
            //Create registerRequest
            RegisterRequest registerRequest = (RegisterRequest)gson.fromJson(ctx.body(), RegisterRequest.class);

            //Send registerRequest to service
            LoginResult loginResult = UserService.register(registerRequest);

            //Return json result
            ctx.result(gson.toJson(loginResult));
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }

    }

    static void login(Context ctx) throws DataAccessException {
        try {
            //Create loginRequest
            LoginRequest loginRequest = (LoginRequest)gson.fromJson(ctx.body(), LoginRequest.class);

            //Send loginRequest to service
            LoginResult loginResult = UserService.login(loginRequest);

            //Return json result
            ctx.result(gson.toJson(loginResult));
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    static void logout(Context ctx) throws DataAccessException {
        try {
            String authToken = ctx.header("authorization");
            UserService.logout(authToken);
            ctx.status(200);
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    static void listGames(Context ctx) throws DataAccessException {
        try {
            String authToken = ctx.header("authorization");
            GameData[] allGameData = GameService.listGames(authToken);

            //Report data
            ctx.result(gson.toJson(Map.of("games",allGameData)));
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    static void createGame(Context ctx) throws DataAccessException {
        try {
            String authToken = ctx.header("authorization");
            NewGameData newGameData = gson.fromJson(ctx.body(), NewGameData.class);
            GameData newGame = GameService.createGame(authToken, newGameData.gameName());

            //Report new gameID
            ctx.result(gson.toJson(Map.of("gameID", newGame.gameID())));
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    static void joinGame(Context ctx) throws DataAccessException {
        try {
            //Extract information from call
            String authToken = ctx.header("authorization");
            JoinGameData joinData = (JoinGameData)gson.fromJson(ctx.body(), JoinGameData.class);

            GameService.joinGame(authToken, joinData);
            ctx.status(200);
        }
        catch (DataAccessException ex) {
            errorReporter(ctx, ex);
        }
    }

    //Error handling
    static void errorReporter(Context ctx, DataAccessException ex) {
        ctx.status(ex.toHttpStatusCode());
        ctx.result(ex.toJson());
    }
}
