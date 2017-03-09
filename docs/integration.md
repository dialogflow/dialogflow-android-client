Integration 
==================

This instruction describes two different ways of integration [API.AI](http://www.api.ai) Android SDK with your app.

Before integration make sure your app have these permissions:

* **android.permission.INTERNET** for internet access
* **android.permission.RECORD_AUDIO** for microphone access

Also SDK library must be in your app dependencies (see **build.gradle**)
```
dependencies {
    // some another dependencies...
    compile 'ai.api:libai:1.4.8'
    compile 'ai.api:sdk:2.0.5@aar'
    // api.ai SDK dependencies
    compile 'com.android.support:appcompat-v7:21.0.3'
    compile 'com.google.code.gson:gson:2.3'
    compile 'commons-io:commons-io:2.4'
}
```

* [Integration with AIButton](#integration-with-aibutton)
* [Integration with AIService](#integration-with-aiservice)
* [Integration with AIDialog](#integration-with-aidialog)

# Integration with AIButton

Use this type of integration if you want quickly integrate natural language processing functionality into your application. The idea is to add ready button control from the SDK. Use the following steps.

1. Add the following code snippet to your activity
    
    ```xml
    <ai.api.ui.AIButton
        android:id="@+id/micButton"
        android:layout_height="152dp"
        style="@style/Microphone"
        />
    ```

2. Add the following code to your activity's `onCreate` function

    ```java
    final AIConfiguration config = new AIConfiguration("YOUR_ACCESS_TOKEN",  
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);

    aiButton = (AIButton) findViewById(R.id.micButton);

    aiButton.initialize(config);
    aiButton.setResultsListener(new AIButton.AIButtonListener() {
        @Override
        public void onResult(final AIResponse result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("ApiAi", "onResult");
                    // TODO process response here
                }
            });
        }de

        @Override
        public void onError(final AIError error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("ApiAi", "onError");
                    // TODO process error here
                }
            });
        }
    });
    ```

3. Replace "YOUR_ACCESS_TOKEN" with key from your development console on the [API.AI](http://www.api.ai) site.
4. Write your custom code instead of `// process response here` and `//process error here` to process success and error responses from the api.ai.

# Integration with AIService

Use this type of integration if you want integrate natural language processing functionality from the [API.AI](http://www.api.ai) in more customizable way. In this way you will be need to create your own controls for start listening, show sound level, cancel request etc. 
Use the following steps:

1. Use one of your application's Activity as the class that will be called when events occur by having it implement the AIListener class. Replace the class declaration with this:
    
    ```java
    public class MainActivity extends ActionBarActivity implements AIListener {
    ```

2. In the Activity class, create a private member for the **AIService** class named `aiService`.
    
    ```java
    private AIService aiService;
    ```
    
3. In the OnCreate method, add the following line to set up the configuration to use Google speech recognition. Replace CLIENT_ACCESS_TOKEN with your client access token.
    
    ```java
     final AIConfiguration config = new AIConfiguration("CLIENT_ACCESS_TOKEN",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);
    ```
    
    ![Api keys](images/apiKeys.png)
    
4. Below this line, initialize the AI service and add this instance as the listener to handle events.
    
    ```java
    aiService = AIService.getService(this, config);
    aiService.setListener(this);
    ```
    
5. Add button to your activity and add method to start listening on the button click:
    ```java
    public void listenButtonOnClick(final View view) {
        aiService.startListening();
    }
    ```
    
6. Add the following method to show the results when the listening is complete. *Make sure you interact with UI in the UI thread.*
    
    ```java
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Result result = response.getResult();

                    // Get parameters
                    String parameterString = "";
                    if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                        for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                            parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                        }
                    }

                    // Show results in TextView.
                    resultTextView.setText("Query:" + result.getResolvedQuery() +
                        "\nAction: " + result.getAction() +
                        "\nParameters: " + parameterString);
                }
            });
    }
    ```
    
7. Add the following method to handle errors. *Make sure you interact with UI in the UI thread.*
    
    ```java
    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultTextView.setText(error.toString());
                }
            });
    }
    ```
    
8. Use the following methods to implement the AIListener interface and process ListeningStarted, ListeningFinished, SoundLevelChanged events:
    
    ```java
    @Override
    public void onListeningStarted() {
        // show recording indicator
    }
    
    @Override
    public void onListeningFinished() {
        // hide recording indicator
    }
    
    @Override
    public void onAudioLevel(final float level) {
        // show sound level
    }
    ```

# Integration with AIDialog

AIDialog is simple and ready to use dialog for making voice requests. All you need is create `AIConfiguration`:

```java
 final AIConfiguration config = new AIConfiguration("CLIENT_ACCESS_TOKEN",
        AIConfiguration.SupportedLanguages.English,
        AIConfiguration.RecognitionEngine.System);
```

And implement `AIDialog.AIDialogListener` interface methods:

```java
void onResult(final AIResponse result);
void onError(final AIError error);
void onCancelled();
```

Then create `AIDialog` instance and show it:

```java
AIDialog aiDialog = new AIDialog(this, config);
aiDialog.setResultsListener(yourListenerImplementation);
aiDialog.showAndListen();
```

AIDialog instance can be used multiple times.