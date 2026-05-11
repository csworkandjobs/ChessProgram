package client;

public class RequestDataStructures {
    //Defines team colors, needs to be phased out in favor of ChessGame.TeamColor
    public enum PlayerColor {
        WHITE,
        BLACK,
    }

    //Information for register, is always paired with a LoginRequest shortly after unless an exception is thrown.
    public record RegisterRequest (
        String username,
        String password,
        String email) {}

    //Information for login, passed through http request
    public record LoginRequest(
        String username,
        String password) {}

    //Contains all information for login return, is built from login http response.
    public record LoginResult (
        String username,
        String authToken) {}

    //Record defining data for joining game, future versions should phase out PlayerColor for ChessGame.TeamColor.
    public record JoinGameData (
        PlayerColor playerColor,
        int gameID) {}

    //Record to contain all data to define new game, should be phased out in future versions with a simple string.
    public record NewGameData (
        String gameName) {}
}
