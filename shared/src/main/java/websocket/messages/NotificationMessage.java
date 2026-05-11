package websocket.messages;

import java.util.Objects;

public class NotificationMessage extends ServerMessage {
    private String message;

    public NotificationMessage(String message, ServerMessageType serverMessageType) {
        super(serverMessageType);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType() &&
                that.getMessage().equals(message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType()) + Objects.hash(message);
    }
}
