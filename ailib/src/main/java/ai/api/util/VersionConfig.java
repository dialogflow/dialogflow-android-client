package ai.api.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import ai.api.GsonFactory;
import ai.api.R;

public class VersionConfig {

    private static final String TAG = VersionConfig.class.getName();
    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private boolean destroyRecognizer = true;

    @Nullable
    public static VersionConfig init(final Context context) {
        String configJson = null;
        final InputStream is = context.getResources().openRawResource(R.raw.version_config);
        try {
            configJson = IOUtils.toString(is, "UTF-8");
        } catch (final IOException e) {
            Log.e(TAG, "Cannot load version config", e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }

        VersionConfig config = null;
        if (!TextUtils.isEmpty(configJson)) {
            try {
                config = fromJson(context, configJson);
            } catch (JSONException e) {
                Log.e(TAG, "Cannot load version config", e);
            }
        }

        return config;
    }

    private static VersionConfig fromJson(final Context context, final String configJson) throws JSONException {
        final JSONObject json = new JSONObject(configJson);

        final VersionConfig config = new VersionConfig();

        final JSONArray googleApps = json.getJSONArray("googleApp");
        if (googleApps != null) {
            final long number = numberFromBuildVersion(RecognizerChecker.getGoogleRecognizerVersion(context));
            long prevVersionNumber = 0;
            for (int i = 0; i < googleApps.length(); i++) {
                final JSONObject googleApp = googleApps.getJSONObject(i);
                final String versionName = googleApp.getString("version");
                if (!TextUtils.isEmpty(versionName)) {
                    final long versionNumber = numberFromBuildVersion(versionName);
                    if (number >= versionNumber && prevVersionNumber < versionNumber) {
                        config.destroyRecognizer = googleApp.getBoolean("destroyRecognizer");
                        prevVersionNumber = versionNumber;
                    }
                }
            }
        }

        return config;
    }

    public boolean isDestroyRecognizer() {
        return destroyRecognizer;
    }

    private static long numberFromBuildVersion(final String buildVersion) {
        if (TextUtils.isEmpty(buildVersion))
            return 0;

        final String[] parts = DOT_PATTERN.split(buildVersion);

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            builder.append(parts[i]);
        }
        try {
            return Long.parseLong(builder.toString());
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return GsonFactory.getGson().toJson(this);
    }
}