package us.koller.calendarwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.RemoteViews
import us.koller.calendarwidget.R

/**
 *
 * */
class CalendarAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val EVENT_CLICKED_ACTION =
            "us.koller.calendarwidget.widget.CalendarAppWidgetProvider.EVENT_CLICKED_ACTION"
        const val UPDATE_WIDGET_ACTION =
            "us.koller.calendarwidget.widget.CalendarAppWidgetProvider.UPDATE_WIDGET_ACTION"

        const val EVENT_URI_EXTRA = "us.koller.calendarwidget.widget.CalendarAppWidgetProvider.EVENT_URI_EXTRA"
    }

    /**
     * Create RemoteViews and attach Adapter.
     * @param context
     * @param widgetId
     * */
    private fun createRemoteViews(context: Context, widgetId: Int): RemoteViews {
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
        val eventClickedPendingIntent =
            PendingIntent.getBroadcast(context, widgetId, eventClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        /* set the pendingIntent-template */
        views.setPendingIntentTemplate(R.id.events_list, eventClickedPendingIntent)

        return views
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            /* update the widget */
            appWidgetManager.updateAppWidget(widgetId, createRemoteViews(context, widgetId))
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            /* when an individual event was clicked */
            EVENT_CLICKED_ACTION -> {
                /* get event data from intent extras */
                val eventUri = Uri.parse(intent.getStringExtra(EVENT_URI_EXTRA))
                val eventBeginTime = intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1L)
                val eventEndTime = intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1L)
                /* launch calendar app and view given eventUri */
                val eventIntent = Intent(Intent.ACTION_VIEW)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(eventUri)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventBeginTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventEndTime)
                /* launch calendar with event */
                context.startActivity(eventIntent)
            }
            /* when a widget refresh was requested (e.g.: refresh-button) */
            UPDATE_WIDGET_ACTION -> {
                Log.d("CalendarAppWidgetProvider", "UPDATE_WIDGET_ACTION")
                /* retrieve widget id from intent extras */
                val widgetId =
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                /* force reload of events list */
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.events_list)
            }
            else -> super.onReceive(context, intent)
        }
    }
}
