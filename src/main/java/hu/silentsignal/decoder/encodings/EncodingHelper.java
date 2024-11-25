package hu.silentsignal.decoder.encodings;

import java.util.ArrayList;

public class EncodingHelper {

    private final ArrayList<HandleEncoding> encodings;
    private final EncodingFactory encodingFactory;

    public EncodingHelper() {
        encodings = new ArrayList<>();
        encodings.add(new B64Encoding());
        encodings.add(new JSONEncoding());
        encodings.add(new CommaSeparatedEncoding());

        encodingFactory = new EncodingFactory();
    }

    public EncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    // Method to find the encoding of a String value
    public HandleEncoding findEncoding(String value, String key){
        HandleEncoding finalEncoding = null;
        boolean found = false;
        // Traverse through each implemented encoding class except No Encoding
        for (HandleEncoding encoding : encodings) {
            if (encoding.isApplicable(value)) {
                found = true;
                encoding.setValue(value);
                encoding.setKey(key);
                // Creating a new encoding with the value and it's key
                finalEncoding = encodingFactory.createNewEncoding(encoding);
            }
        }
        // If the String parameter didn't fit for either encoding
        // Then it's simply a String value
        if (!found) {
            finalEncoding = new NoEncoding();
            finalEncoding.setValue(value);
            finalEncoding.setKey(key);
        }
        return finalEncoding;
    }
}
