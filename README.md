api-ai-android-sdk
==================

API.AI Android SDK allows using voice commands and integration with dialog scenarios defined for a particular agent in API.AI.

In order to use API.AI, Android application needs to have Internet access and access to the microphone (RECORD_AUDIO) permission.

SDK requires specification of the OAuth2 Access Token that could be found in the API.AI agents details page for each of the agents.

The SDK allows using twi types of speech recognition:

1. Google. Speech recognition is performed using Google's speech recognition on the client / in the cloud. Recognized text is passed to the API.AI query service for processing.

2. Speaktoit. (!NB - COMING SOON). Speech recognition is performed on the API.AI server. The client app sends audio file or stream to API.AI server. 

# GETTING STARTED:


1. Add two permissions into the AndroidManifest:
    * android.permission.INTERNET
    * android.permission.RECORD_AUDIO
    
2. Implement AIListener interface to process responses from API.AI
3. Create an instance of AIConfiguration and get a reference to the AIService from it.
4. Set AIListener into the AIService.
5. Launch listening from the mic via the "startListening" method. The SDK will start listening for the microphone input of the mobile device.
6. To stop or cancel listening to a user commend call "stopListening" or "cancel" methods of AIService.

