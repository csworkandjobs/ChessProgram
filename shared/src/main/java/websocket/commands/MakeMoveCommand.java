package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {
    private ChessMove move;

    public MakeMoveCommand(ChessMove chessMove, CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
        this.move = chessMove;
    }

    public ChessMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MakeMoveCommand that)) {
            return false;
        }
        return that.getMove().equals(move) &&
                getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(move, getCommandType(), getAuthToken(), getGameID());
    }
}
