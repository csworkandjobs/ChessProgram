package dataaccess;

import java.util.HashMap;
import model.AuthData;

public class AuthDaoMemory {
    private static HashMap<String, AuthData> authDataSet = new HashMap<>();
    public static void clearAuths() {
        authDataSet.clear();
    }

    public static AuthData getAuth(String authToken) {
        return authDataSet.get(authToken);
    }

    public static void createAuth(AuthData authData) {
        authDataSet.put(authData.authToken(), authData);
    }

    public static void deleteAuth(String authToken) {
        authDataSet.remove(authToken);
    }



    //Used for testing purposes
    public static int getDataSize() {
        return authDataSet.size();
    }
}
