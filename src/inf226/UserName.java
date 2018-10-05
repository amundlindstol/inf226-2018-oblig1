package inf226;

import java.security.InvalidParameterException;

public class UserName {
    private final String userName;

    public UserName(String userName) {
        String testUsername = userName.replaceAll("[^a-zA-Z0-9]", "");
        if (!testUsername.equals(userName))
            throw new InvalidParameterException();
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
