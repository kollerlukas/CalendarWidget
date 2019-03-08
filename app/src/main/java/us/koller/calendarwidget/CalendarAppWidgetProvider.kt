package us.koller.calendarwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews

/**
 *
 * */
class CalendarAppWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val EVENT_CLICKED_ACTION =
            "us.koller.calendarwidget.CalendarAppWidgetProvider.EVENT_CLICKED_ACTION"

        const val EVENT_URI_EXTRA = "us.koller.calendarwidget.CalendarAppWidgetProvider.EVENT_URI_EXTRA"
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        Log.d("CalendarAppWidgetProv", "onUpdate() called")

        if (appWidgetIds == null) {
            return
        }

        for (appWidgetId in appWidgetIds) {
            /* instantiate RemoteViews */
            val remoteViews = RemoteViews(context?.packageName, R.layout.calendar_appwidget_view)

            /* construct pendingIntent-template to receive click on an event */
            val eventClickedIntent = Intent(context, CalendarAppWidgetProvider::class.java)
            eventClickedIntent.action = EVENT_CLICKED_ACTION
            eventClickedIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val eventClickedPendingIntent =
                PendingIntent.getBroadcast(context, 0, eventClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            /* set the pendingIntent-template */
            remoteViews.setPendingIntentTemplate(R.id.text, eventClickedPendingIntent)

            /* create intent to start CalendarRemoteViewsService */
            val intent = Intent(context, CalendarRemoteViewsService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            /* set adapter */
            remoteViews.setRemoteAdapter(R.id.events_list, intent)

            // TODO: emptyView
            // rv.setEmptyView(R.id.stack_view, R.id.empty_view)

            /* update the widget */
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
        }
        // TODO
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            EVENT_CLICKED_ACTION ->
                /* launch calendar app and view given eventUri */
                context?.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(intent.getStringExtra(EVENT_URI_EXTRA)))
                )
        }
        super.onReceive(context, intent)
    }
}
