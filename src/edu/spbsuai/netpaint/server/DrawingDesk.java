package edu.spbsuai.netpaint.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DrawingDesk {

    private User owner;
    private String name;
    private byte[] image;
    private List<DeskObserver> observerList = new ArrayList<>();

    public synchronized  boolean isSendUpdatesToObservers() {
        return sendUpdatesToObservers;
    }

    public synchronized void setSendUpdatesToObservers(boolean sendUpdatesToObservers) {
        this.sendUpdatesToObservers = sendUpdatesToObservers;
    }

    private  boolean sendUpdatesToObservers = true;

    public DrawingDesk(String name, User owner) {
        this.owner = owner;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void addDeskObserver(DeskObserver d) {
        if (!observerList.contains(d))
            observerList.add(d);
    }

    public synchronized void removeDeskObserver(DeskObserver d) {
        observerList.remove(d);
    }
    public synchronized void removeAllDeskObservers() {
        observerList.clear();
    }

    public synchronized List<DeskObserver> getAllDeskObservers() {
        return Collections.unmodifiableList(observerList);
    }


    public void setImage(byte[] newImage, User updateInitiator) {
        if(newImage != null) {
            synchronized (this) {
                image = newImage;
            }
        }
        try {
            notifyListeners(updateInitiator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized byte[] getImage() {
        return image;
    }


    public User getOwner() {
        return owner;
    }

    public void notifyListeners(User updateInitiator) throws IOException {
        List<DeskObserver> list = null;
        synchronized (this) {
            list = Collections.unmodifiableList(observerList);
        }
        if (list != null)
            for (DeskObserver deskObserver : list) {
                if(deskObserver.equals(updateInitiator)) continue;
                deskObserver.deskUpdated(name, getImage());
            }
    }


}
