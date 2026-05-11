package websocket.messages;

import chess.ChessGame;
import java.util.Objects;
import model.*;

public class LoadGameMessage extends ServerMessage {
    private GameData game;

    public LoadGameMessage(GameData game, ServerMessageType serverMessageType) {
        super(serverMessageType);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoadGameMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType() &&
                that.getGame().equals(game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType()) + Objects.hash(game);
    }
}
