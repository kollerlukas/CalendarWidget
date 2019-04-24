package us.koller.calendarwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ContentUris
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import us.koller.calendarwidget.CalendarLoader
import us.koller.calendarwidget.CalendarLoaderImpl
import us.koller.calendarwidget.Event
import us.koller.calendarwidget.R
import us.koller.calendarwidget.util.Prefs
import us.koller.calendarwidget.util.SectionedRemoteViewsFactory
import java.util.*

/**
 * CalendarRemoteViewsService to create an instance of CalendarRemoteViewsFactory
 * */
class CalendarRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        /* retrieve widget id from intent extras */
        val widgetId =
            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        /* new Preferences instance */
        val prefs = Prefs(applicationContext)
        /* create new factory */
        val factory = CalendarRemoteViewsFactory(
            applicationContext.packageName,
            CalendarLoaderImpl.wrap(applicationContext.contentResolver),
            prefs.getDaysShownInWidget(),
            prefs.loadWidgetPrefs(widgetId).calendarIds,
            todayString = applicationContext.getString(R.string.today),
            tomorrowString = applicationContext.getString(R.string.tomorrow)
        )
        /* set refresh fillInIntent */
        val fillInIntent = Intent(CalendarAppWidgetProvider.UPDATE_WIDGET_ACTION)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        factory.refreshFillInIntent = fillInIntent
        /* return factory */
        return factory
    }
}

/**
 * CalendarRemoteViewsFactory: provide RemoteView for the ListView in the Widget
 * */
class CalendarRemoteViewsFactory(
    packageName: String,
    private val loader: CalendarLoader,
    private val daysShown: Int,
    private val selectedCalendars: List<Long>,
    private val todayString: String?,
    private val tomorrowString: String?,
    private val timeFormatter: DateFormat = SimpleDateFormat("HH:mm"),
    private val dateFormatter: DateFormat = SimpleDateFormat("EEE, dd MMMM")
) : SectionedRemoteViewsFactory<Event.Instance>(packageName) {

    /**
     * fillInIntent for refresh onClick
     * */
    lateinit var refreshFillInIntent: Intent

    override fun onCreate() {
        /* nothing to create */
    }

    override fun onDestroy() {
        /* nothing to destroy */
    }

    override fun getLoadingView(): RemoteViews? {
        /* no loadingView needed */
        return null
    }

    override fun getFooter(): RemoteViews {
        /* set refresh button as footer */
        val views = RemoteViews(packageName, R.layout.refesh_item_view)
        /* add fillInIntent */
        views.setOnClickFillInIntent(R.id.refresh_button, refreshFillInIntent)

        return views
    }

    override fun onDataSetChanged() {
        Log.d("CalendarRemoteViewsService", "onDataSetChanged called")
        /* get todays timestamp with 00:00:00:000 */
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val todayTimestamp = todayCal.timeInMillis
        /* calculate today's and tomorrows date to replace by "Today" or "Tomorrow" string */
        val today = dateFormatter.format(Date(todayTimestamp))
        val tomorrow = dateFormatter.format(Date(todayTimestamp + 24 * 60 * 60 * 1000))
        /* use loader to load events */
        val events = loader.loadEventInstances(daysShown)
            /* filter out event from un-selected calendars */
            .filter { selectedCalendars.contains(it.event.calendarId) }
        /* set items on SectionedRemoteViewsFactory */
        setItems(events)
        /* add sections */
        /* map day to timestamp */
        (0..daysShown).map { todayTimestamp + it * 24 * 60 * 60 * 1000 }
            /* find section index in list */
            .map { t ->
                when (val index = events.indexOfFirst { it.begin > t }) {
                    /* no event on last day found */
                    -1 -> Pair(events.size, t)
                    else -> Pair(index, t)
                }
            }
            /* map timestamp to string */
            .map {
                val date = dateFormatter.format(Date(it.second))
                /* replace date by string if today and tomorrow string are available */
                if (todayString != null && tomorrowString != null) {
                    Pair(
                        it.first, when (date) {
                            today -> todayString
                            tomorrow -> tomorrowString
                            else -> date
                        }
                    )
                } else {
                    Pair(it.first, date)
                }

            }
            /* add the sections */
            .forEach { addSection(it.first, it.second) }
    }

    override fun getItemId(item: Event.Instance): Long {
        /* use position as itemId */
        return item.event.id + item.begin
    }

    override fun getViewAt(item: Event.Instance): RemoteViews {
        /* create new RemoteViews instance */
        val views = RemoteViews(packageName, R.layout.event_item_view)
        /* bind Data */
        /* set colordot color */
        views.setInt(R.id.colordot, "setColorFilter", item.event.displayColor)
        /* set event start time */
        views.setTextViewText(
            R.id.event_start_time,
            when (item.event.allDay) {
                false -> timeFormatter.format(Date(item.begin))
                else -> ""
            }
        )
        /* set event title */
        views.setTextViewText(R.id.event_title, item.event.title)

        /* set the fill-intent to pass data back to CalendarAppwidgetProvider */
        /* construct eventUri to open event in calendar app */
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, item.event.id)
        /* create fill-intent */
        val fillInIntent = Intent(CalendarAppWidgetProvider.EVENT_CLICKED_ACTION)
            /* put eventUri as extra */
            .putExtra(CalendarAppWidgetProvider.EVENT_URI_EXTRA, eventUri.toString())
            /* put begin and end extra to open specific instance */
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, item.begin)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, item.end)
        /* set the fill-intent */
        views.setOnClickFillInIntent(R.id.event_card, fillInIntent)

        return views
    }
}