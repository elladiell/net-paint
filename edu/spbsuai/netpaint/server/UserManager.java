package edu.spbsuai.netpaint.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {

    private static  Map<Socket, User> userMap = new HashMap<>();

    private static Map<String, String> userPasswords = new HashMap<>();
    static {
        userPasswords.put("guest", "guest");
        userPasswords.put("iv", "mono");
        userPasswords.put("qwer", "qwer");
    }


    public static boolean addConnectedUser(String login, String passwd, Socket socket) throws UserManagerException {
        if(userMap.containsValue(new User(login))) {
            throw new UserManagerException("User with such login is already connected");
        }
        if(userPasswords.containsKey(login) && userPasswords.get(login).equals(passwd)) {
            User u = new User(login, socket);
            userMap.put(socket, u);
            return true;
        }
        return false;
    }

    public static void removeConnectedUser(Socket s){
        userMap.remove(s);
    }


    public static User getUserBySocket(Socket csocket) {
        return userMap.get(csocket);
    }

    public  static class UserManagerException extends Throwable {
        public UserManagerException(String s) {
            super(s);
        }
    }
}
