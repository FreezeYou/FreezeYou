package cf.playhi.freezeyou.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import cf.playhi.freezeyou.OneKeyScreenLockImmediatelyService;
import cf.playhi.freezeyou.R;

public class OneKeyLockScreen extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, OneKeyScreenLockImmediatelyService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_onekeylockscreen);
            views.setOnClickPendingIntent(R.id.aw_onekeyLockScreenButton, pendingIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
