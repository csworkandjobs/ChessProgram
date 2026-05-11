package client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;
import java.util.ArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

import client.RequestDataStructures.*;
import model.*;

import static client.ClientDataAccessException.FailCode.*;

public class ServerFacade {
    //private static final int TIMEOUT_MILLIS = 5000;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverURL;
    private static final Gson GSON = new Gson();

    public ServerFacade(String url) {
        serverURL = url;
    }

    public void doClear() throws ClientDataAccessException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public LoginResult doRegister(RegisterRequest registerRequest) throws ClientDataAccessException {
        return makeRequest("POST", "/user", registerRequest, null, LoginResult.class);
    }

    public LoginResult doLogin(LoginRequest loginRequest) throws ClientDataAccessException {
        return makeRequest("POST", "/session", loginRequest, null, LoginResult.class);
    }

    public void doLogout(String authToken) throws ClientDataAccessException {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public GameData[] doListGames(String authToken) throws ClientDataAccessException {
        Map<String, ArrayList<GameData>> gameList = makeRequest("GET", "/game", null, authToken, Map.class);

        return GSON.fromJson(GSON.toJson(gameList.get("games")), GameData[].class);
    }

    public int doCreateGame(String authToken, NewGameData newGameData) throws ClientDataAccessException {
        Map<String, Double> gameIDJsonPair = makeRequest("POST", "/game", newGameData, authToken, Map.class);
        return gameIDJsonPair.get("gameID").intValue();
    }

    public GameData doJoinGame(String authToken, JoinGameData joinGameData) throws ClientDataAccessException {
        return makeRequest("PUT", "/game", joinGameData, authToken, null);
    }



    private <T> T makeRequest(String method, String path, Object requestBody,
                              String authToken, Class<T> responseClass) throws ClientDataAccessException {
        try {
            URL specificURL = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) specificURL.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            //If authToken provided, append it to request as a header
            if(authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            writeBody(requestBody, http);
            http.connect();
            verifySuccess(http);
            return readBody(http, responseClass);
        } catch (ClientDataAccessException ex) {
            //Acknoledge specific errors, otherwise mark the exception as a generic server error.
            throw new ClientDataAccessException(ex.getFailCode(), ex.getMessage());
        } catch (Exception ex) {
            throw new ClientDataAccessException(ClientDataAccessException.FailCode.server, ex.getMessage());
        }
    }

    private static void writeBody(Object requestBody, HttpURLConnection http) throws IOException {
        if (requestBody != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = GSON.toJson(requestBody);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream responseBody = http.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(responseBody);
            if(responseClass != null) {
                response = GSON.fromJson(reader, responseClass);
            }
        }
        return response;
    }

    private void verifySuccess(HttpURLConnection http) throws IOException, ClientDataAccessException {
        int status = http.getResponseCode();
        if(status/100 != 2) {
            ClientDataAccessException.FailCode code;
            switch(status) {
                case 400:
                    code = badRequest;
                    break;
                case 403:
                    code = alreadyTaken;
                    break;
                case 401:
                    code = unauthorized;
                    break;
                default:
                    code = server;
            }
            throw new ClientDataAccessException(code, "failure: " + status);
        }
    }
}
