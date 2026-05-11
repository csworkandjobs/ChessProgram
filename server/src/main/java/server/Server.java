package server;

import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import dataaccess.DatabaseManager;

import dataaccess.DataAccessException;

public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            DatabaseManager.createDatabase();
        } catch (Exception ex) {
            throw new RuntimeException();
        }

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        //Clear
        javalin.delete("/db", HTTPHandlers::clear);

        //Register
        javalin.post("/user", HTTPHandlers::register);

        //Login
        javalin.post("/session", HTTPHandlers::login);

        //Logout
        javalin.delete("/session", HTTPHandlers::logout);

        //List Games
        javalin.get("/game", HTTPHandlers::listGames);

        //Create Game
        javalin.post("/game", HTTPHandlers::createGame);

        //Join Game
        javalin.put("/game", HTTPHandlers::joinGame);

        //Websocket
        WebSocketHandler webSocketHandler = new WebSocketHandler();
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);});
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
