# JSON Base64 Provider

Teaching Burp to recognize Base64 inside JSON.

This is an Insertion Point Provider extension that
1. Recognizes Base64 content inside JSON structures
2. Provides new insertion points for Burp based on the previous recognition result

There is a [PoC implementation](https://s2dev.silentsignal.hu/s2crew/burp-json-jtree/src/branch/insertion-point) that can be used as "inspiration" for development :)

## Roadmap

This is a single-purpose extension. However, it'd be nice if we could generalize the solution to 
- Support multiple encodings and serializations
- Recognize these elements recursively (e.g. b64(urlencode(b64urlsafe(payload))))