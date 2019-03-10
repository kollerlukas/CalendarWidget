package us.koller.calendarwidget

import android.content.ContentUris
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.util.Date

/**
 *
 * */
class CalendarRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val factory = CalendarRemoteViewsFactory(
            applicationContext.packageName, CalendarLoaderImpl.wrap(applicationContext.contentResolver)
        )
        factory.todayString = applicationContext.getString(R.string.today)
        factory.tomorrowString = applicationContext.getString(R.string.tomorrow)
        return factory
    }
}

/**
 *
 * */
class CalendarRemoteViewsFactory(packageName: String, var loader: CalendarLoader) :
    SectionedRemoteViewsFactory<Event>(packageName) {

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
        val events = loader.loadEvents(7)
        setItems(events)
        /* add sections */
        events
            /* map each event to its date */
            .map {
                val date = formatter.format(Date(it.dtstart))
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

    override fun getItemId(item: Event): Long {
        /* use position as Event.id as itemId */
        return item.id
    }

    override fun getViewAt(item: Event): RemoteViews {
        /* create new RemoteViews instance */
        val remoteViews = RemoteViews(packageName, R.layout.event_item_view)
        /* bind Data */
        /* set colordot color */
        remoteViews.setInt(R.id.colordot, "setColorFilter", item.displayColor)
        /* set event start time */
        remoteViews.setTextViewText(
            R.id.event_start_time,
            when (item.allDay) {
                false -> SimpleDateFormat("HH:mm").format(Date(item.dtstart))
                else -> ""
            }
        )
        /* set event title */
        remoteViews.setTextViewText(R.id.event_title, item.title)

        /* set the fill-intent to pass data back to CalendarAppwidgetProvider */
        /* construct eventUri to open event in calendar app */
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, item.id)
        /* create fill-intent */
        val fillInIntent = Intent()
        /* put eventUri as extra */
        fillInIntent.putExtra(CalendarAppWidgetProvider.EVENT_URI_EXTRA, eventUri.toString())
        /* set the fill-intent */
        remoteViews.setOnClickFillInIntent(R.id.event_card, fillInIntent)

        return remoteViews
    }
}