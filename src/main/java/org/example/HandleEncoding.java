package org.example;

import java.util.List;

// Interface for different encodings
// And the methods that each encoding class has to implement
public interface HandleEncoding{
    // Returns whether a value meets the requirements for the encoding
    boolean isApplicable(String value);
    // Encodes a value given as a parameter to the corresponding encoding
    String encodeValue(String value);
    // Inserts a payload given as a parameter
    void insertPayload(String key, String value, int n);
    // Sets the base value for the encoding class
    void setValue(String value);
    // Getter for the value field
    String getValue();
    // For more complex structures
    List<KeyValueTuple> getValues();
    // Finds an element in a structure (if it's more complex)
    int findElement(String value);
    // Returns the key (if it exists)
    String getKey();
    // Sets the key (if it exists)
    void setKey(String key);
}