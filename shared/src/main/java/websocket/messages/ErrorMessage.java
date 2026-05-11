package websocket.messages;

import java.util.Objects;

public class ErrorMessage extends ServerMessage {
    private String errorMessage;

    public ErrorMessage(String message, ServerMessage.ServerMessageType serverMessageType) {
        super(serverMessageType);
        this.errorMessage = message;
    }

    public String getMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType() &&
                that.getMessage().equals(errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType()) + Objects.hash(errorMessage);
    }
}
