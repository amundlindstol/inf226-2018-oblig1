package inf226;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.security.InvalidParameterException;

public class Password {
    private final String password;

    public Password(String password) {
        String safePass = Jsoup.clean(password, Whitelist.basic());
        if (!safePass.equals(password))
            throw new InvalidParameterException();
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
