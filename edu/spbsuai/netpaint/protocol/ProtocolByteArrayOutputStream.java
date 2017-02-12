package edu.spbsuai.netpaint.protocol;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ProtocolByteArrayOutputStream extends ByteArrayOutputStream {

    public static final String ENCODING = "utf-8";

    public void writeInt(int value) throws IOException {
        byte [] bytes = ByteBuffer.allocate(4).putInt(value).array();
        write(bytes);
    }

    public void writeString(String s) throws IOException {
        writeString(s, ENCODING);
    }
    public void writeString(String s, String encoding) throws IOException {
        byte [] bytes = s.getBytes(encoding);
        writeInt(bytes.length);
        write(bytes);
    }

    public void writeImage(BufferedImage image) throws IOException {
        try(ByteArrayOutputStream baos2 = new ByteArrayOutputStream()){
            ImageIO.write(image, "png", baos2);
            writeInt(baos2.size());
            write(baos2.toByteArray());
        }
    }
}
