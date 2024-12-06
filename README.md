# Burp Nested Encoder 

A modular and customizable insertion point provider extension created for Burp which can detect arbitrary complex nested structures using recursion and a tree-like structure with parent-child hierarchy.

Currently supported encodings are
- Base64
- JSON
- Comma separated (list-like objects)

However it's easy and simple to add new encodings.

## Usage

Build with Gradle

```
$ ./gradlew build
```

Inside Burp under the extensions tab add the build/libs/*.jar file.

## How it works

Using the Montoya API a custom AuditInsertionPointProvider class is created which implements the corresponding interface. Inside the overriden provideInsertionPoints method the application goes through each parameter parsed from the base HttpRequestResponse. During the loop the extension attempts to build a tree for each parameter with the following logic:
- Inside the EncodingHelper class each implemented encoding class is registered in a list
- The parameters original value is tested against each encoding
    - If it meets the requirement of the given encoding, a root node is created with the appropriate encoding type
- The extension then tries to "decode" (the meaning of decode is heavily dependent on the encoding, for B64 it's simply a B64 decode, but for example in the case of a JSON, it extracts every key-value pair)
- The decoded value(s) will become the child(ren) of the root node
- The decoded value(s) are tested against every encoding
- Recursively continue
- When a value doesn't fulfill either encodings requirements, then it's considered a leaf node (it can't be decoded further, hence it can't have children)
    - Basically this will be our insertion point

When the tree is built from the initial parameters value, the extension queries all of its leaf nodes, a new CustomInsertionPoint instance is created based on the leaf node, which is then added to our list. 

Inside the buildHttpRequestWithPayload method the extension modifies the leaf nodes value with the payload provided by Burp. This starts a chain reaction: the node notifies its parent about the change, then the parent modifies and re-encodes itself accordingly, and so on, until we reach the root node.

See our [blog post](https://blog.silentsignal.eu/2024/12/06/custom-decoder-for-burp/) for some more details!

## Implementing a new encoding

Create a new class implementing the HandleEncoding interface, then implement the inherited methods based on the encoding-specific logic. After this, simply register the encoding class in the list found inside the EncodingHelper class, and also create a new entry in the EncodingFactory class.

## Testing

Testing is done using JUnit tests. For a new test create a text file under /test/resources - the first line should be the dummy parameter value, the second should be the dummy payload, the rest should be the expected outputs. After that, create a new test function inside the BurpSimulationTest class according to the implemented test cases. Be careful, the program automatically removes every whitespace and newline break from the actual output, so create the expected output accordingly.
