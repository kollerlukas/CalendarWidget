package us.koller.calendarwidget

import android.content.ContentUris
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.util.*

/**
 *
 * */
class CalendarRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CalendarRemoteViewsFactory(
            applicationContext.packageName, CalendarLoaderImpl.wrap(applicationContext.contentResolver)
        )
    }
}

/**
 *
 * */
class CalendarRemoteViewsFactory(packageName: String, var loader: CalendarLoader) :
    SectionedRemoteViewsFactory<Event>(packageName) {

    override fun getLoadingView(): RemoteViews? {
        /* no loadingView needed */
        return null
    }

    override fun onDataSetChanged() {
        /* use loader to load events */
        val events = loader.loadEvents(7)
        setItems(events)
        /* add sections */
        events
            /* map each event to its date */
            .map { SimpleDateFormat("EEE, dd MMMM").format(Date(it.dtstart)) }
            /* map to Pair of index and date */
            .mapIndexed { i, d -> Pair<Int, String>(i, d) }
            /* group by the date */
            .groupBy { it.second }
            /* find the lowest index with a certain date */
            .map { Pair(it.value.minBy { it.first }!!.first, it.key) }
            /* add the sections */
            .forEach { addSection(it.first, it.second) }
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