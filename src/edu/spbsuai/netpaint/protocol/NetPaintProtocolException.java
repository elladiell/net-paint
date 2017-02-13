package edu.spbsuai.netpaint.protocol;

import java.io.IOException;

public class NetPaintProtocolException extends IOException {

    public NetPaintProtocolException(String message) {
        super(message);
    }

    public NetPaintProtocolException() {
    }
}
