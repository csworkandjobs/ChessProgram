package client;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Indicates there was an error connecting to the database
 */
public class ClientDataAccessException extends Exception{
    //Types of errors, current implementation converts most to server errors regardless of initial type.
    public enum FailCode {
        badRequest,
        alreadyTaken,
        unauthorized,
        server,
    }

    //Permanently defines failcode associated with object instance.
    final private FailCode failCode;

    //Adds in default error messages for common errors, but client-side implementation is expected this much if at all.
    public ClientDataAccessException(FailCode failCode) {
        this(failCode, defaultErrorMessages(failCode));
    }

    //Constructor for all calls, both with default and custom error messages.
    public ClientDataAccessException(FailCode failCode, String message) {
        super("Error: " + message);
        this.failCode = failCode;
    }

    //Generate a conversion to http status code.
    public int toHttpStatusCode() {
        return switch(failCode) {
            case badRequest -> 400;
            case alreadyTaken -> 403;
            case unauthorized -> 401;
            case server -> 500;
            default -> 500; //If error type is unknown (which isn't possible), assume it to be an internal server error
        };
    }

    //Default error messages generated upon object creation, current implementation does not anticipate using these.
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

    //Convert error to json, not expected to be used in this implementation.
    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage()));
    }

    public FailCode getFailCode() {
        return this.failCode;
    }
}
