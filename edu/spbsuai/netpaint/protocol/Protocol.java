package edu.spbsuai.netpaint.protocol;

import edu.spbsuai.netpaint.server.User;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class Protocol {
//    public static RequestMessage parseRequest(InputStream is, User user) throws IOException, NetPaintProtocolException {
//        ProtocolDataInputStream dis = new ProtocolDataInputStream(is);
//        MessageCodes requestCode = MessageCodes.values()[dis.readInt()];
//        switch (requestCode){
//            case REQ_DESK_JOIN:
//                String deskName = dis.readString();
//                return new RequestJoinDesk(deskName);
//            default:
//                throw new NetPaintProtocolException();
//        }
//
//    }


    public static final int SERVER_PORT = 3421;


    public enum MessageCodes {
        REQ_LOGIN, REQ_DESK_SHARE, REQ_DESK_UNSHARE, REQ_DESK_JOIN, REQ_DESK_UNJOIN, REQ_DESKS_LIST, REQ_DESK_PAINT,
        RESPONSE_LOGIN, RESPONSE_DESK_SHARE, RESPONSE_DESK_UNSHARE, RESPONSE_DESK_JOIN, RESPONSE_DESK_UNJOIN, RESPONSE_DESKS_LIST, RESPONSE_DESK_PAINT
    }

    public enum ResponseStatuses {OK, ERROR}

    public static final String ENCODING = "utf-8";


    public static Message readMessage(InputStream is) throws IOException, NetPaintProtocolException {
        ProtocolDataInputStream dis = new ProtocolDataInputStream(is);
        MessageCodes code = MessageCodes.values()[dis.readInt()];
        Message msg = new Message(code);
        switch (code) {
            case REQ_DESKS_LIST:
                break;
            case REQ_DESK_SHARE:
            case REQ_DESK_UNSHARE:
            case REQ_DESK_UNJOIN:
            case REQ_DESK_JOIN: {
                String deskName = dis.readString();
                msg.addParam(deskName);
                break;
            }
            case REQ_LOGIN: {
                String login = dis.readString();
                msg.addParam(login);
                String password = dis.readString();
                msg.addParam(password);
                break;
            }
            case RESPONSE_DESK_UNSHARE:
            case RESPONSE_DESK_UNJOIN:
            case RESPONSE_DESK_SHARE:
            case RESPONSE_DESK_JOIN:
            case RESPONSE_LOGIN: {
                int respCode = dis.readInt();
                msg.addParam(respCode);
                String comment = dis.readString();
                msg.addParam(comment);
                break;
            }
            case RESPONSE_DESKS_LIST: {
                int respCode = dis.readInt();
                msg.addParam(respCode);
                int countOfDesks = dis.readInt();
                msg.addParam(countOfDesks);
                for (int i = 0; i < countOfDesks; i++) {
                    String deskName = dis.readString();
                    msg.addParam(deskName);
                }
                break;
            }
            case REQ_DESK_PAINT: {
                String name = dis.readString();
                msg.addParam(name);
                byte[] img = dis.readByteArray();
                msg.addParam(img);
                break;
            }
            case RESPONSE_DESK_PAINT:{
                String name = dis.readString();
                msg.addParam(name);
                BufferedImage img = dis.readImage();
                msg.addParam(img);
                break;
            }
            default:
                throw new NetPaintProtocolException("Wrong message code " + code);
        }
        return msg;
    }

    public static void writeMessage(byte[] message, OutputStream os) throws IOException {
        os.write(message);
        os.flush();
    }

    public static byte[] buildRequestLogin(String login, String password) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_LOGIN.ordinal());
            baos.writeString(login, ENCODING);
            baos.writeString(password, ENCODING);
            return baos.toByteArray();
        }
    }


    public static byte[] buildRequestShareDesk(String deskName) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESK_SHARE.ordinal());
            baos.writeString(deskName, ENCODING);
            return baos.toByteArray();
        }
    }

    public static byte[] buildRequestUnshareDesk(String deskName) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESK_UNSHARE.ordinal());
            baos.writeString(deskName, ENCODING);
            return baos.toByteArray();
        }
    }

    public static byte[] buildRequestJoinDesk(String deskName) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESK_JOIN.ordinal());
            baos.writeString(deskName, ENCODING);
            return baos.toByteArray();
        }
    }

    public static byte[] buildRequestUnjoinDesk(String deskName) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESK_UNJOIN.ordinal());
            baos.writeString(deskName, ENCODING);
            return baos.toByteArray();
        }
    }

    public static byte[] buildRequestDesksList() throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESKS_LIST.ordinal());
            return baos.toByteArray();
        }
    }

    public static byte[] buildResponseDesksList(ResponseStatuses status, List<String> message) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.RESPONSE_DESKS_LIST.ordinal());
            baos.writeInt(status.ordinal());
            baos.writeInt(message.size());
            for (String s : message) {
                baos.writeString(s, ENCODING);
            }
            return baos.toByteArray();
        }
    }


    public static byte[] buildResponse(MessageCodes responseCode, ResponseStatuses status, String message) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(responseCode.ordinal());
            baos.writeInt(status.ordinal());
            baos.writeString(message, ENCODING);
            return baos.toByteArray();
        }
    }


    public static byte[] buildRequestPaint(String deskName, BufferedImage image) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.REQ_DESK_PAINT.ordinal());
            baos.writeString(deskName, ENCODING);
            baos.writeImage(image);
            return baos.toByteArray();
        }
    }

    public static byte[] buildResponsePaint(String deskName, byte [] image) throws IOException {
        try (ProtocolByteArrayOutputStream baos = new ProtocolByteArrayOutputStream()) {
            baos.writeInt(MessageCodes.RESPONSE_DESK_PAINT.ordinal());
            baos.writeString(deskName, ENCODING);
            baos.writeInt(image.length);
            baos.write(image);
            return baos.toByteArray();
        }
    }


}
