package ai.api.sample;

/***********************************************************************************************************************
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 * <p/>
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 * <p/>
 * **********************************************************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***********************************************************************************************************************/

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {

    private AIApplication app;

    private static final long PAUSE_CALLBACK_DELAY = 500;

    private final Handler handler = new Handler();
    private Runnable pauseCallback = new Runnable() {
        @Override
        public void run() {
            app.onActivityPaused();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AIApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(pauseCallback, PAUSE_CALLBACK_DELAY);
    }

}
