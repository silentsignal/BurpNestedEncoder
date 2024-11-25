package hu.silentsignal.decoder.encodings;

import java.util.*;

public class B64Encoding implements HandleEncoding{
    private String value;
    private String key;

    // Decides whether a String is B64 or not
    @Override
    public boolean isApplicable(String value) {
        return mightBeBase64(value);
    }

    // Encodes the given parameter to B64
    @Override
    public String encodeValue(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    // Inserts the payload (since it's a B64, it simply replaces the previous value with the B64 encoded payload)
    @Override
    public void insertPayload(String key, String value, int n) {
        setValue(encodeValue(value));
    }

    // Setter for value
    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void appendValue(String value) {
        this.value += value;
    }

    // Returning the decoded values
    // Since it's a B64, it'll only contain one String
    @Override
    public List<KeyValueTuple> getValues() {
        KeyValueTuple kvt = new KeyValueTuple(new String(Base64.getDecoder().decode(value)));
        List<KeyValueTuple> list = new ArrayList<>();
        list.add(kvt);
        return list;
    }

    // Not needed for B64
    // Since it's not a complex structure
    @Override
    public int findElement(String value) {
        return 0;
    }

    // Getter for value
    public String getValue() {
        return value;
    }

    // Getter for the key
    @Override
    public String getKey() {
        return key;
    }

    // Setter for the key
    @Override
    public void setKey(String key) {
        this.key = key;
    }

    // Checking if the value is Base64 encoded or not
    boolean mightBeBase64(String value){
        // Regex for B64 detection
        String b64Regex = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/][AQgw]==|[A-Za-z0-9+/]{2}[AEIMQUYcgkosw048]=)?$";
        // Threshold to minimize false-positives
        // If the value's length is less than 8 chars, we drop it
        int lengthThreshold = 8;
        if (value.length() < lengthThreshold) {
            return false;
        }else return value.matches(b64Regex);
    }
}
