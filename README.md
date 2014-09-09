api-ai-android-sdk
==================

The API.AI Android SDK makes it easy to integrate speech recognition with [API.AI](http://www.api.ai) natural language processing API on Android devices. API.AI allows using voice commands and integration with dialog scenarios defined for a particular agent in API.AI.

Two permissions are required to use the API.AI Android SDK:

* **android.permission.INTERNET** for internet access
* **android.permission.RECORD_AUDIO** for microphone access

Currently, speech recognition is performed using Google's Android SDK, either on the client device or in the cloud. Recognized text is passed to the API.AI through HTTP requests. In the future, your client app will be able to use the SDK to send an audio file or stream to the API.AI server so that it can be processed there.

Authentication is accomplished through setting the client access token when initializing an **AIConfiguration** object. The client access token specifies which agent will be used for natural language processing.

**Note:** The API.AI Android SDK only makes query requests, and cannot be used to manage entities and intents. Instead, use the API.AI user interface or REST API to  create, retreive, update, and delete entities and intents.

# Running the Sample Code

The API.AI Android SDK comes with a simple sample that illustrates how voice commands can be integrated with API.AI. Use the following steps to run the sample code:

1. Have an API.AI agent created that has entities and intents. See the API.AI documentation on how to do this. 
1. Open [Android Studio](https://developer.android.com/sdk/installing/studio.html).
2. Import the **api-ai-android-master** directory.
3. Open the SDK Manager and be sure that you have installed Android Build Tools 1.9.
4. In the Project browser, open **apiAISampleApp/src/main/java/ai.api.sample/MainActivity**.
5. Towards the top of the file, you will see a declaration of a static final string called *ACCESS_TOKEN*. Set its value to be the client access token of your agent. 
6. Attach an Android device, or have the emulator set up with an emulated device.
7. From the **Run** menu, choose **Debug** (or click the Debug symbol).
8. You should see an app running with three buttons: **Listen**, **StopListen**, and **Cancel**.
9. Click **Listen** and say a phrase that will be understood by your agent. Wait a few seconds. The Java will appear that is returned by the API.AI service.

# Getting Started with Your Own App

Follow these steps for creating your own app that uses the API.AI Android SDK:

1. Add two permissions into the AndroidManifest:
    * **android.permission.INTERNET**
    * **android.permission.RECORD_AUDIO**
    
2. Create a class that implements the AIListener interface. This class will process responses from API.AI.
3. Create an instance of AIConfiguration, specifying the access token, locale, and recognition engine.
4. Use the AIConfiguration object to get a reference to the AIService, which will make the query requests.
5. Set the AIListener instance for the AIService instance.
6. Launch listening from the microphone via the **startListening** method. The SDK will start listening for the microphone input of the mobile device.
7. To stop listening and start the request to the API.AI service using the current recognition results, call the **stopListening** method of the AIService class.
8. To cancel the listening process without sending a request to the API.AI service, call the **cancel** method of the AIService class.

