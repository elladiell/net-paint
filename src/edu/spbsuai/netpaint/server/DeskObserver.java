package edu.spbsuai.netpaint.server;

import java.io.IOException;
import java.net.Socket;

public interface DeskObserver {
    void deskUpdated(String name, byte [] data) throws IOException;
    Socket getSocket();
}
