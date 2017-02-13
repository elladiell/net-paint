package edu.spbsuai.netpaint.protocol;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ProtocolDataInputStream extends DataInputStream {
//    private static final int BUF_SIZE = 1024 * 10;
    public static final String ENCODING = "utf-8";

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ProtocolDataInputStream(InputStream in) {
        super(in);
    }



    public byte [] readSizeAndBytes() throws IOException {
        int size = readInt();
        byte[] buf = new byte[size];
        readFully(buf);
        return buf;
    }
//    public byte [] readBytes(int size) throws IOException {
//        int counter = 0;
//        int read = 0;
//        byte [] buff = new byte[BUF_SIZE];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        while((read = read(buff)) != -1 && counter < size){
//            bos.write(buff,0, read);
//            counter += read;
//        }
//        return bos.toByteArray();
//    }

    public String readString() throws IOException {
        return  readString(ENCODING);
    }
    public String readString(String charset) throws IOException {
        int size = readInt();
        byte[] buf = new byte[size];
        readFully(buf);
        return new String(buf, charset);
    }


    public BufferedImage readImage() throws IOException {
        int size = readInt();
        byte[] buf = new byte[size];
        readFully(buf);
        InputStream is = new ByteArrayInputStream(buf);
        return ImageIO.read(is);

    }

    public byte [] readByteArray() throws IOException {
        int size = readInt();
        byte[] buf = new byte[size];
        readFully(buf);
        return buf;
    }
}
