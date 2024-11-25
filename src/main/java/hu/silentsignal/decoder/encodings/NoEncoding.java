package hu.silentsignal.decoder.encodings;

import java.util.List;

public class NoEncoding implements HandleEncoding{
    private String value;
    private String key;

    // Returns false on every call to avoid false-positives
    @Override
    public boolean isApplicable(String value) {
        return false;
    }

    @Override
    public String encodeValue(String value) {
        return value;
    }

    @Override
    public void insertPayload(String key, String value, int n) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void appendValue(String value) {
        this.value += value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<KeyValueTuple> getValues() {
        return List.of();
    }

    @Override
    public int findElement(String value) {
        return 0;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
}
