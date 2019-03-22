package us.koller.calendarwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.RemoteViews
import us.koller.calendarwidget.R

/**
 *
 * */
class CalendarAppWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val EVENT_CLICKED_ACTION =
            "us.koller.calendarwidget.widget.CalendarAppWidgetProvider.EVENT_CLICKED_ACTION"

        const val EVENT_URI_EXTRA = "us.koller.calendarwidget.widget.CalendarAppWidgetProvider.EVENT_URI_EXTRA"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            /* instantiate RemoteViews */
            val views = RemoteViews(
                context.packageName,
                R.layout.calendar_appwidget_view
            )

            /* create intent to start CalendarRemoteViewsService */
            val intent = Intent(context, CalendarRemoteViewsService::class.java)
                /* set new data to create new Factory & Adapter instance */
                .setData(Uri.parse("$widgetId"))
                /* put widget id as extra */
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            /* set adapter via the intent */
            views.setRemoteAdapter(R.id.events_list, intent)

            /* construct pendingIntent-template to receive click on an event */
            val eventClickedIntent = Intent(context, CalendarAppWidgetProvider::class.java)
            eventClickedIntent.action =
                EVENT_CLICKED_ACTION
            val eventClickedPendingIntent =
                PendingIntent.getBroadcast(context, widgetId, eventClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            /* set the pendingIntent-template */
            views.setPendingIntentTemplate(R.id.events_list, eventClickedPendingIntent)

            /* update the widget */
            appWidgetManager.updateAppWidget(widgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }


    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            EVENT_CLICKED_ACTION ->
                /* launch calendar app and view given eventUri */
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(Uri.parse(intent.getStringExtra(EVENT_URI_EXTRA)))
                        /* copy over begin & end time */
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1L)
                        )
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_END_TIME,
                            intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1L)
                        )
                )
        }
        super.onReceive(context, intent)
    }
}
