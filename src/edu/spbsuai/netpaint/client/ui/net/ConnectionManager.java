package edu.spbsuai.netpaint.client.ui.net;

import edu.spbsuai.netpaint.protocol.Message;
import edu.spbsuai.netpaint.protocol.NetPaintProtocolException;
import edu.spbsuai.netpaint.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionManager {


    private String connectionStatusMessage;

    public static class SingletonHolder {
        public static final ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
    }

    private static String serverName = "127.0.0.1";
    private static int serverPort = Protocol.SERVER_PORT;
    private static boolean connected;
    private static Socket socket;


    private Map<Protocol.MessageCodes, Message> respMessages = Collections.synchronizedMap(new HashMap<>());
    private Map<Protocol.MessageCodes, List<MessageListener>> messageListeners = Collections.synchronizedMap(new HashMap<>());


    public static void setServerName(String sn){
        serverName = sn;
    }
    public static void setPort(int p){
        serverPort = p;
    }
    public static String getServerName(){
        return serverName;
    }
    public static int getPort(){
        return serverPort;
    }

    public void addMessageListener(Protocol.MessageCodes code, MessageListener ml) {
        List<MessageListener> listeners = messageListeners.get(code);
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(ml);
        messageListeners.put(code, listeners);
    }

    private void notifyMessageListeners(Protocol.MessageCodes code, Message msg) {
        List<MessageListener> listeners = messageListeners.get(code);
        if (listeners != null) {
            for (MessageListener ml : listeners) {
                ml.messageReceived(msg);
            }
        }
    }

    private Runnable socketReader = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    Message resp = Protocol.readMessage(socket.getInputStream());
                    respMessages.put(resp.getCode(), resp);
                    synchronized (resp.getCode()) {
                        resp.getCode().notify();
                    }
                    notifyMessageListeners(resp.getCode(), resp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable socketWriter = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    byte[] msg = messageQueueToServer.take();
                    OutputStream os = socket.getOutputStream();
                    os.write(msg);
                    os.flush();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private ConnectionManager() {

    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.CONNECTION_MANAGER;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getConnectionStatusMessage() {
        return connectionStatusMessage;
    }

    private BlockingDeque<byte[]> messageQueueToServer = new LinkedBlockingDeque<>();

    public boolean doConnect(String username, String password) throws IOException {
        try {

            initSocketAndThreads();
            byte[] message = Protocol.buildRequestLogin(username, password);

            sendMessage(message);
            Message response = receiveMessage(Protocol.MessageCodes.RESPONSE_LOGIN);
            int okOrError = (Integer) response.getParamByIndex(0);
            connectionStatusMessage = (String) response.getParamByIndex(1);
            if (Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK) {
                connected = true;
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    private void initSocketAndThreads() throws IOException {
        if(socket == null) {
            socket = new Socket(serverName, serverPort);
            new Thread(socketWriter).start();
            new Thread(socketReader).start();
        }
    }


    public boolean doRegister(String username, String password) throws IOException {
        try {
            initSocketAndThreads();
            byte[] message = Protocol.buildRequestRegister(username, password);

            sendMessage(message);
            Message response = receiveMessage(Protocol.MessageCodes.RESPONSE_REGISTER);
            int okOrError = (Integer) response.getParamByIndex(0);
            connectionStatusMessage = (String) response.getParamByIndex(1);
            if (Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK) {
                connected = true;
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public void sendMessage(byte[] msg) throws IOException {
        messageQueueToServer.offer(msg);
    }

    public Message receiveMessage(Protocol.MessageCodes messageCode) throws IOException {
        synchronized (messageCode) {
            while (!respMessages.containsKey(messageCode)) {
                try {
                    messageCode.wait();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
            return respMessages.remove(messageCode);
        }
    }

    public void doDisconnect() throws IOException {
        socket.close();
    }
}
