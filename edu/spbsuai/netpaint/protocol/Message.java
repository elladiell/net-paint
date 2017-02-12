package edu.spbsuai.netpaint.protocol;

import java.util.*;

public class Message {


//    Map<String, Object> params = new LinkedHashMap<>();
    List<Object> paramsList = new ArrayList<>();
    private Protocol.MessageCodes code;


//    public Object getParamByName(String name){
//        return params.get(name);
//    }
    public Object getParamByIndex(int idx){
        return paramsList.get(idx);
    }

    public void addParam(Object value){
        paramsList.add(value);
    }

    public Protocol.MessageCodes getCode() {
        return code;
    }

    public Message(Protocol.MessageCodes code) {
        this.code = code;
    }
}
