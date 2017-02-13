package edu.spbsuai.netpaint.server;

import edu.spbsuai.netpaint.protocol.Protocol;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesksManager {
    private static Map<String, DrawingDesk> desks = new HashMap<>();


    public static synchronized List<DrawingDesk> getDesksList() {
        List<DrawingDesk> ds = new ArrayList<>(desks.values());
        return ds;
    }

    public static synchronized boolean shareDesk(String deskName, Socket csocket) {
        if (desks.containsKey(deskName)) {
            return false;
        }
        DrawingDesk dd = new DrawingDesk(deskName, UserManager.getUserBySocket(csocket));
        dd.addDeskObserver(UserManager.getUserBySocket(csocket));
        desks.put(deskName, dd);
        return true;
    }

    public static synchronized void unshareDesk(Socket csocket) {
        List<String> toRemove = new ArrayList<>();
        for (String name : desks.keySet()) {
            if (desks.get(name).getOwner().getSocket() == csocket) {
                toRemove.add(name);
            }
        }
        toRemove.forEach(name -> {
            desks.remove(name);
        });
    }

    public static synchronized boolean unshareDesk(String deskName, Socket csocket) throws IOException {
        if (!desks.containsKey(deskName)) {
            return false;
        }
        DrawingDesk dd = desks.get(deskName);
        dd.setSendUpdatesToObservers(false);
        for (DeskObserver d : dd.getAllDeskObservers()) {
            byte []  resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_UNJOIN, Protocol.ResponseStatuses.OK, "Desk is unjoined");
            OutputStream os = d.getSocket().getOutputStream();
            os.write(resp);
            os.flush();
        }
        dd.removeAllDeskObservers();
        desks.remove(deskName);
        return true;
    }

    public static synchronized boolean joinDesk(String deskName, Socket csocket) {
        if (!desks.containsKey(deskName)) {
            return false;
        }
        DrawingDesk dd = desks.get(deskName);
        dd.addDeskObserver(UserManager.getUserBySocket(csocket));
        return true;
    }

    public static synchronized boolean unjoinDesk(String deskName, Socket csocket) {
        if (!desks.containsKey(deskName)) {
            return false;
        }
        DrawingDesk dd = desks.get(deskName);
        dd.removeDeskObserver(UserManager.getUserBySocket(csocket));
        return true;
    }

    public static List<String> getDesksNamesList() {
        List<DrawingDesk> dds = getDesksList();
        List<String> deskNames = new ArrayList<>();
        for (DrawingDesk dd : dds) {
            deskNames.add(dd.getName());
        }
        return deskNames;
    }

    public static void updateDesk(String deskName, byte [] img, User updateInitiator) {
        if (!desks.containsKey(deskName)) {
            return;
        }
        DrawingDesk dd = desks.get(deskName);
        dd.setImage(img, updateInitiator);
    }

    public static void notifyDeskListeners(String deskName) throws IOException {
        if (!desks.containsKey(deskName)) {
            return;
        }
        DrawingDesk dd = desks.get(deskName);
        dd.notifyListeners(null);
    }
    public static void notifyUser(String deskName, User u) throws IOException {
        if (!desks.containsKey(deskName)) {
            return;
        }
        DrawingDesk dd = desks.get(deskName);
        u.deskUpdated(deskName, dd.getImage());
    }
}
