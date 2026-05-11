package dataaccess;

import java.util.HashMap;
import model.UserData;

public class UserDaoMemory {
    private static HashMap<String, UserData> userDataSet = new HashMap<>();

    public static void clearUsers() {
        userDataSet.clear();
    }

    public static UserData getUser(String username) {
        return userDataSet.get(username);
    }

    public static void createUser(UserData userData) {
        userDataSet.put(userData.username(), userData);
    }



    //Used for testing purposes
    public static int getDataSize() {
        return userDataSet.size();
    }
}
