package tk.leoforney.passcheckerserver;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Leo on 5/5/2018.
 */
public class User extends Person {
    public String token;
    private Base64 base64;

    public User() {
        base64 = new Base64();
    }

    public User(String name, String token) {
        this.name = name;
        this.token = token;
        base64 = new Base64();
    }

    public String getEmail() {
        if (token != null && token.length() > 1) {
            String passwordHash = new String(base64.decode(token));
            String[] hashArray = passwordHash.split(":");
            if (hashArray.length == 2) {
                return hashArray[0];
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String getPassword() {
        if (token != null && token.length() > 1) {
            String passwordHash = new String(base64.decode(token));
            String[] hashArray = passwordHash.split(":");
            if (hashArray.length == 2) {
                return hashArray[1];
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public void setPassword(String password) {
        try {
            token = Arrays.toString(base64.encode((getEmail() + ":" + password).getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    boolean isValid() {
        if (token == null) token = "";
        if (name == null) name = "";
        return token.length() != 0 && name.length() != 0;
    }
}
