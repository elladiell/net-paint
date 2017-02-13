package edu.spbsuai.netpaint.server;

import edu.spbsuai.netpaint.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class User implements DeskObserver{
    private String login;
    private Socket socket;

    public User(String login, Socket socket) {
        this(login);
        this.socket = socket;
    }

    public User(String login) {
        this.login = login;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return login.equals(user.login);

    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public void deskUpdated(String name, byte[] data) throws IOException {
        if(data != null) {
            OutputStream os = socket.getOutputStream();
            byte[] msg = Protocol.buildResponsePaint(name, data);
            os.write(msg);
            os.flush();
        }else{
            System.out.println("8888!!");
        }
    }
}
