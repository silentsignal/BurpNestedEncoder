package hu.silentsignal.decoder.encodings;

import java.io.Serializable;

public class EncodingFactory implements Serializable {

    public EncodingFactory() {}

    public HandleEncoding createNewEncoding(HandleEncoding currEncoding){
        if (currEncoding instanceof B64Encoding) {
            B64Encoding newEncoding = new B64Encoding();
            newEncoding.setValue(currEncoding.getValue());
            newEncoding.setKey(currEncoding.getKey());
            return newEncoding;
        } else if (currEncoding instanceof JSONEncoding) {
            JSONEncoding newEncoding = new JSONEncoding();
            newEncoding.setValue(currEncoding.getValue());
            newEncoding.setKey(currEncoding.getKey());
            return newEncoding;
        } else if (currEncoding instanceof CommaSeparatedEncoding) {
            CommaSeparatedEncoding newEncoding = new CommaSeparatedEncoding();
            newEncoding.setValue(currEncoding.getValue());
            newEncoding.setKey(currEncoding.getKey());
            return newEncoding;
            // NoEncoding must be always at the bottom
        } else{
            NoEncoding newEncoding = new NoEncoding();
            newEncoding.setValue(currEncoding.getValue());
            newEncoding.setKey(currEncoding.getKey());
            return newEncoding;
        }
    }
}
