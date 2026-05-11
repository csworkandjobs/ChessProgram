package service;

public class RequestDataStructures {
    public enum PlayerColor {
        WHITE,
        BLACK,
    }

    public record RegisterRequest (
        String username,
        String password,
        String email) {}

    public record LoginRequest(
        String username,
        String password) {}

    public record LoginResult (
        String username,
        String authToken) {}

    public record JoinGameData (
        PlayerColor playerColor,
        int gameID) {}

    public record NewGameData (
        String gameName) {}
}
