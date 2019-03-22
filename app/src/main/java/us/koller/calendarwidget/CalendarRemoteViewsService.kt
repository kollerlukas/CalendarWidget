package us.koller.calendarwidget

import android.content.ContentUris
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.util.*

/**
 * CalendarRemoteViewsService to create an instance of CalendarRemoteViewsFactory
 * */
class CalendarRemoteViewsService : RemoteViewsService() {

    companion object {
        public var DAYS_SHOWN_IN_WIDGET: Int = 7
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        /* create new factory */
        val factory = CalendarRemoteViewsFactory(
            applicationContext.packageName,
            CalendarLoaderImpl.wrap(applicationContext.contentResolver),
            DAYS_SHOWN_IN_WIDGET
        )
        /* loading string resources here, because access to applicationContext */
        factory.todayString = applicationContext.getString(R.string.today)
        factory.tomorrowString = applicationContext.getString(R.string.tomorrow)
        /* return factory */
        return factory
    }
}

/**
 * CalendarRemoteViewsFactory: provide RemoteView for the ListView in the Widget
 * */
class CalendarRemoteViewsFactory(packageName: String, private val loader: CalendarLoader, private val daysShown: Int) :
    SectionedRemoteViewsFactory<Event.Instance>(packageName) {

    var todayString = ""
    var tomorrowString = ""

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

    override fun onDataSetChanged() {
        /* date formatter */
        val formatter = SimpleDateFormat("EEE, dd MMMM")
        /* calculate today's and tomorrows date to replace by "Today" or "Tomorrow" string */
        val currTimeStamp = System.currentTimeMillis()
        val today = formatter.format(Date(currTimeStamp))
        val tomorrow = formatter.format(Date(currTimeStamp + 24 * 60 * 60 * 1000))
        /* use loader to load events */
        val events = loader.loadEventInstances(daysShown)
        setItems(events)
        /* add sections */
        events
            /* map each event to its date */
            .map {
                val date = formatter.format(Date(it.begin))
                when (date) {
                    today -> todayString
                    tomorrow -> tomorrowString
                    else -> date
                }
            }
            /* map to Pair of index and date */
            .mapIndexed { i, d -> Pair<Int, String>(i, d) }
            /* group by the date */
            .groupBy { it.second }
            /* find the lowest index with a certain date */
            .map { Pair(it.value.minBy { it.first }?.first, it.key) }
            /* add the sections */
            .forEach { it.first?.let { it1 -> addSection(it1, it.second) } }
    }

    override fun getItemId(item: Event.Instance): Long {
        /* use position as itemId */
        return item.event.id + item.begin
    }

    override fun getViewAt(item: Event.Instance): RemoteViews {
        /* create new RemoteViews instance */
        val remoteViews = RemoteViews(packageName, R.layout.event_item_view)
        /* bind Data */
        /* set colordot color */
        remoteViews.setInt(R.id.colordot, "setColorFilter", item.event.displayColor)
        /* set event start time */
        remoteViews.setTextViewText(
            R.id.event_start_time,
            when (item.event.allDay) {
                false -> SimpleDateFormat("HH:mm").format(Date(item.begin))
                else -> ""
            }
        )
        /* set event title */
        remoteViews.setTextViewText(R.id.event_title, item.event.title)

        /* set the fill-intent to pass data back to CalendarAppwidgetProvider */
        /* construct eventUri to open event in calendar app */
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, item.event.id)
        /* create fill-intent */
        val fillInIntent = Intent()
            /* put eventUri as extra */
            .putExtra(CalendarAppWidgetProvider.EVENT_URI_EXTRA, eventUri.toString())
            /* put begin and end extra to open specific instance */
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, item.begin)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, item.end)
        /* set the fill-intent */
        remoteViews.setOnClickFillInIntent(R.id.event_card, fillInIntent)

        return remoteViews
    }
}