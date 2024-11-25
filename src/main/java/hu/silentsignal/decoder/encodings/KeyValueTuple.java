package hu.silentsignal.decoder.encodings;

public class KeyValueTuple {
    private final String key;
    private final String value;

    public KeyValueTuple(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KeyValueTuple(String value) {
        this.key = "";
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
