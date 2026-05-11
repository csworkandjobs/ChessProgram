package dataaccess;

import java.util.Map;
import com.google.gson.Gson;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    public enum FailCode {
        badRequest,
        alreadyTaken,
        unauthorized,
        server,
    }
    final private FailCode failCode;

    //Adds in default error messages for common errors.
    public DataAccessException(FailCode failCode) {
        this(failCode, defaultErrorMessages(failCode));
    }

    //Builds exception for all calls, both with default and with custom error messages.
    public DataAccessException(FailCode failCode, String message) {
        super("Error: " + message);
        this.failCode = failCode;
    }

    public int toHttpStatusCode() {
        return switch(failCode) {
            case badRequest -> 400;
            case alreadyTaken -> 403;
            case unauthorized -> 401;
            case server -> 500;
            default -> 500; //If error type is unknown (which isn't possible), assume it to be an internal server error
        };
    }

    public static String defaultErrorMessages(FailCode failCode) {
        return switch(failCode) {
            case badRequest -> "bad request";
            case alreadyTaken -> "already taken";
            case unauthorized -> "unauthorized";
            case server -> "internal server error";

            //To have gotten here indicates an unknown error since a message was not passed in.
            default -> "unknown error";
        };
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage()));
    }
}
