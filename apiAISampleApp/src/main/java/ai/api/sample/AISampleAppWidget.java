package ai.api.sample;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;


/**
 * Implementation of App Widget functionality.
 */
public class AISampleAppWidget extends AppWidgetProvider {
    private static final String ACTION_KEY = "tap_on_icon";
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_KEY)) {

            Intent popUpIntent = new Intent(context, AIWidgetActivity.class);

            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(popUpIntent);
            Toast.makeText(context.getApplicationContext(), "hop hey",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ai_app_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        Intent intent = new Intent(context, AISampleAppWidget.class);
        intent.setAction(ACTION_KEY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_new_app_widget, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

