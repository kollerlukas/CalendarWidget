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
class CalendarRemoteViewsFactory(var packageName: String, var loader: CalendarLoader) :
    RemoteViewsService.RemoteViewsFactory {

    var events: List<Event> = emptyList()

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        /* no loadingView needed */
        return null
    }

    override fun getItemId(index: Int): Long {
        /* use position as Event.id as itemId */
        return events[index].id
    }

    override fun onDataSetChanged() {
        /* use loader to load events */
        events = loader.loadEvents(7)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(index: Int): RemoteViews {
        /* create new RemoteViews instance */
        val remoteViews = RemoteViews(packageName, R.layout.event_item_view)
        /* bind Data */
        /* set colordot color */
        events[index].displayColor.let { remoteViews.setInt(R.id.colordot, "setColorFilter", it) }
        /* set event start time */
        remoteViews.setTextViewText(
            R.id.event_start_time,
            when (events[index].allDay) {
                false -> SimpleDateFormat("HH:mm").format(Date(events[index].dtstart))
                else -> ""
            }
        )
        /* set event title */
        remoteViews.setTextViewText(R.id.event_title, events[index].title)
        /* set event date */
        remoteViews.setTextViewText(
            R.id.event_date,
            SimpleDateFormat("EEE, dd MMMM").format(Date(events[index].dtstart))
        )

        /* set the fill-intent to pass data back to CalendarAppwidgetProvider */
        /* construct eventUri to open event in calendar app */
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, events[index].id)
        /* create fill-intent */
        val fillInIntent = Intent()
        /* put eventUri as extra */
        fillInIntent.putExtra(CalendarAppWidgetProvider.EVENT_URI_EXTRA, eventUri.toString())
        /* set the fill-intent */
        remoteViews.setOnClickFillInIntent(R.id.event_card, fillInIntent)

        return remoteViews
    }

    override fun getCount(): Int {
        return events.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {
        /* nothing needs to be destroyed */
    }

}