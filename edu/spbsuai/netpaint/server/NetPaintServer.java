package edu.spbsuai.netpaint.server;

import edu.spbsuai.netpaint.protocol.Message;
import edu.spbsuai.netpaint.protocol.NetPaintProtocolException;
import edu.spbsuai.netpaint.protocol.Protocol;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetPaintServer implements Runnable {

    private static int portToListen = Protocol.SERVER_PORT;
    private Socket csocket;


    NetPaintServer(Socket csocket) {
        this.csocket = csocket;
    }

    public static void main(String args[]) throws Exception {
        if (args.length > 1) {
            if (args[0].startsWith("-p")) {
                try {
                    portToListen = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Usage: NetPaintServer -port {portNumber}");
                }
            }
        }
        ServerSocket ssock = new ServerSocket(portToListen);
        System.out.println("Listening");

        while (true) {
            Socket sock = ssock.accept();
            System.out.println("Connected");
            new Thread(new NetPaintServer(sock)).start();
        }
    }

    public void run() {
        try (InputStream is = csocket.getInputStream(); OutputStream os = csocket.getOutputStream()) {
            while (true) {
                Message m = Protocol.readMessage(is);
                processMessage(m, os);
            }
        } catch (IOException e) {
            System.out.println(e);
            UserManager.removeConnectedUser(csocket);
            DesksManager.unshareDesk(csocket);
        }
    }

    private void processMessage(Message m, OutputStream os) throws IOException {
        switch (m.getCode()) {
            case REQ_LOGIN: {
                byte[] resp;
                try {
                    boolean added = UserManager.addConnectedUser((String) m.getParamByIndex(0), (String) m.getParamByIndex(1), csocket);
                    if (added) {
                        resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_LOGIN, Protocol.ResponseStatuses.OK, "User is connected");
                    } else {
                        resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_LOGIN, Protocol.ResponseStatuses.ERROR, "Authetication failed. Wrong login or password.");
                    }
                }catch(UserManager.UserManagerException e){
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_LOGIN, Protocol.ResponseStatuses.ERROR, e.getMessage());
                }
                os.write(resp);
                os.flush();
                break;
            }
            case REQ_DESK_SHARE: {
                byte[] resp;
                boolean shared = DesksManager.shareDesk((String) m.getParamByIndex(0), csocket);
                if (shared) {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_SHARE, Protocol.ResponseStatuses.OK, "Desk is shared");
                } else {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_SHARE, Protocol.ResponseStatuses.ERROR, "Desk name is already in use");
                }
                os.write(resp);
                os.flush();
                break;
            }

            case REQ_DESK_UNSHARE: {
                byte[] resp;
                boolean shared = DesksManager.unshareDesk((String) m.getParamByIndex(0), csocket);
                if (shared) {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_UNSHARE, Protocol.ResponseStatuses.OK, "Desk is unshared");
                } else {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_UNSHARE, Protocol.ResponseStatuses.ERROR, "Desk is already not shared");
                }
                os.write(resp);
                os.flush();
                break;
            }
            case REQ_DESK_JOIN: {
                String deskName = (String) m.getParamByIndex(0);
                boolean joined = DesksManager.joinDesk(deskName, csocket);
                if (joined) {
                    byte[] resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_JOIN, Protocol.ResponseStatuses.OK, "Desk is joined");
                    os.write(resp);
                    os.flush();
                    DesksManager.notifyUser(deskName, UserManager.getUserBySocket(csocket));
                } else {
                    byte[] resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_JOIN, Protocol.ResponseStatuses.ERROR, "Desk with such name is not shared");
                    os.write(resp);
                    os.flush();
                }
                break;
            }

            case REQ_DESK_UNJOIN: {
                byte[] resp;
                boolean shared = DesksManager.unjoinDesk((String) m.getParamByIndex(0), csocket);
                if (shared) {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_UNJOIN, Protocol.ResponseStatuses.OK, "Desk is unjoined");
                } else {
                    resp = Protocol.buildResponse(Protocol.MessageCodes.RESPONSE_DESK_UNJOIN, Protocol.ResponseStatuses.ERROR, "Desk was not be joined");
                }
                os.write(resp);
                os.flush();
                break;
            }
            case REQ_DESKS_LIST: {
                byte [] resp = Protocol.buildResponseDesksList(Protocol.ResponseStatuses.OK, DesksManager.getDesksNamesList());
                os.write(resp);
                os.flush();
                break;
            }
            case REQ_DESK_PAINT: {
                String deskName = (String) m.getParamByIndex(0);
                byte [] img = (byte []) m.getParamByIndex(1);
                DesksManager.updateDesk(deskName, img, UserManager.getUserBySocket(csocket));
                break;
            }

        }

    }

}
