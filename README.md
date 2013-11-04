Test app to investigate cordova communication between the js and java layer. Idea is that we are loading a remote web
page inside a CordovaWebView that accesses locally the cordova.android.js file deployed with the application. This is
easier said than done as for SDK < 11 it looks like there is no way to modify the webview response. So the code below
only works for SDK=11 and up.
 
The cordova-js is about 70K minified (the whole set) which is too much to send over a 3G connection. Thus deploying
with the application allows us to have the best of both worlds:
  
 1. Access from js to native features like connection , acelerometer or camera 
 2. Small payloads on the webview side as we do not need to download the whole cordova framework on JS.
