package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommaSeparatedEncoding implements HandleEncoding {
    private String value;
    private String key;

    // Decides whether a String is comma-separated
    // e.g. ["commaSeparated1", "commaSeparated2", "commaSeparated3"]
    @Override
    public boolean isApplicable(String value) {
        return isCommaSeparated(value);
    }

    // Setter for value
    // Also removing any whitespaces
    public void setValue(String value) {
        this.value = value.replaceAll(" +", "");
    }

    // Encode is not needed for this encoding
    public String encodeValue(String value) {
        return value;
    }

    // Inserts the payload into the given index
    @Override
    public void insertPayload(String key, String value, int n) {
        // Splitting the value alongside the separator character
        String[] split = this.value.split(",");
        // Check if the index is bigger than the length
        // Or if it's -1
        if (split.length < n || n == -1){
            System.out.println("Insert position is greater than the elements in the list.");
        }else{
            split[n] = value;
        }
        // Reconstructing the list alongside the separator character
        //setValue(String.join(",", split));
        setValue(String.join(",", split));
    }

    // Returns each element of the list
    @Override
    public List<KeyValueTuple> getValues() {
        List<KeyValueTuple> list = new ArrayList<>();

        String[] vals = value.split(",");
        for (String val : vals) {
            list.add(new KeyValueTuple(val));
        }

        return list;
    }

    // Getter for the value field
    public String getValue() {
        return value;
    }

    // Finds an element given as a parameter in the list
    // Returns -1 if it doesn't exist in the list
    @Override
    public int findElement(String value) {
        String [] elements = getValue().split(",");
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    // Getter for the key field
    @Override
    public String getKey() {
        return key;
    }

    // Setter for the key field
    @Override
    public void setKey(String key) {
        this.key = key;
    }

    // Decides whether a string might be a comma separated encoding
    private boolean isCommaSeparated(String value) {
        String[] split = value.split(",");
        value = value.replaceAll(" +", "");
        // Not to confuse it with JSON
        boolean beginsWithSquareBracket = value.charAt(0) == '[';
        // Not to confuse it with JSON
        boolean beginsWithCurlyBraces = value.charAt(0) == '{';
        // Minimum two elements, doesn't start with "{" or "["
        return split.length >= 2 && !beginsWithCurlyBraces && !beginsWithSquareBracket;
    }
}
