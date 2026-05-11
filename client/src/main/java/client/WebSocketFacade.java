
package client;

import com.google.gson.Gson;
import client.ClientDataAccessException;
import websocket.commands.*;
import websocket.messages.*;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import websocket.commands.*;
import websocket.messages.*;

public class WebSocketFacade extends Endpoint {
    Session session;
    ChessREPL gameLoop;
    Gson gson = new Gson();

    public WebSocketFacade(String url, ChessREPL gameLoop) throws ClientDataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.gameLoop = gameLoop;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //ser message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    switch(serverMessage.getServerMessageType()) {
                        case LOAD_GAME:
                            LoadGameMessage loadGame = gson.fromJson(message, LoadGameMessage.class);
                            gameLoop.updateGame(loadGame);
                            break;
                        case ERROR:
                            ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                            gameLoop.notify(errorMessage.getMessage());
                            break;
                        case NOTIFICATION:
                            NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                            gameLoop.notify(notification.getMessage());
                            break;
                    }
                }
            });


        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ClientDataAccessException(ClientDataAccessException.FailCode.server, ex.getMessage());
        }
    }

    public void sendToServer(Object data) {
        try {
            session.getBasicRemote().sendText(gson.toJson(data));
        } catch (Exception ex) {}
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}
}

