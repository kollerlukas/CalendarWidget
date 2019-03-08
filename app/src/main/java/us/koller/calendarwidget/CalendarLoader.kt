package us.koller.calendarwidget

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract

/**
 *  helper class to load calendar & events synchronously
 *  */
class CalendarLoader(context: Context) {

    companion object {
        /* calendar projection array */
        private val CALENDAR_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )
        /* calendar projection indices */
        private const val CALENDAR_PROJECTION_ID_INDEX = 0
        private const val CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX = 1
        private const val CALENDAR_PROJECTION_CALENDAR_COLOR_INDEX = 2
        private const val CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 3
        private const val CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX = 4

        /* event projection array */
        private val EVENTS_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DISPLAY_COLOR,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
        )
        /* event projection indices */
        private const val EVENT_PROJECTION_ID_INDEX = 0
        private const val EVENT_PROJECTION_TITLE_INDEX = 1
        private const val EVENT_PROJECTION_COLOR_INDEX = 2
        private const val EVENT_PROJECTION_DESCRIPTION_INDEX = 3
        private const val EVENT_PROJECTION_LOCATION_INDEX = 4
        private const val EVENT_PROJECTION_CALENDAR_ID_INDEX = 5
        private const val EVENT_PROJECTION_DTSTART_INDEX = 6
        private const val EVENT_PROJECTION_DTEND_INDEX = 7
        private const val EVENT_PROJECTION_ALL_DAY_INDEX = 8
    }

    /* retrieve contentResolver from provided context */
    var contentResolver: ContentResolver = context.contentResolver

    /** load all events from all calendars that start within the a given amount of days (nextDays)
     * @param nextDays
     * @return a list of all events */
    @Suppress("RedundantVisibilityModifier")
    public fun loadEvents(nextDays: Int): List<Event> {
        val currTimeStamp = System.currentTimeMillis()
        val nextDaysMillis = nextDays * 24 * 60 * 60 * 1000
        return loadCalendars().asSequence()
            /* load all the events for each calendar */
            .map { c -> loadEventsForCalendar(c, currTimeStamp, currTimeStamp + nextDaysMillis) }
            /* map each calendar onto its events */
            .map { c -> c.events!!.filter { e -> e.dtstart!! - currTimeStamp < nextDaysMillis } }
            .flatten()
            /* sort the events by starting time */
            .sortedBy { it.dtstart }
            /* remove duplicate events */
            .distinctBy { it.id }
            .toList()
    }

    /** load all calendars
     * @return a list of all found calendars (events attribute is empty) */
    @Suppress("RedundantVisibilityModifier")
    public fun loadCalendars(): List<Calendar> {
        /* build query */
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        /* retrieve all calendars */
        val selection = ""
        val selectionArgs: Array<String> = arrayOf()

        var cursor: Cursor? = null
        try {
            /* run query */
            cursor = contentResolver.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null)
            /* create new list to save found calendars */
            val calendars: MutableList<Calendar> = mutableListOf()
            /* iterate through the cursor */
            while (cursor.moveToNext()) {
                /* instantiate calendar */
                val calendar = Calendar(
                    cursor.getLong(CALENDAR_PROJECTION_ID_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX),
                    cursor.getInt(CALENDAR_PROJECTION_CALENDAR_COLOR_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX),
                    emptyList()
                )
                /* load events + add to the list */
                calendars.add(calendar)
            }
            /* return all found calendars */
            return calendars
        } catch (e: SecurityException) {
            /* probably READ_CALENDAR permission not granted */
            return emptyList()
        } finally {
            /* finally close cursor */
            cursor?.close()
        }
    }

    /** load all events that start before a given timestamp (maxDtStart) and end after another given timestamp (minDtEnd) in for a given calendar (calendar)
     * @param calendar
     * @param minDtEnd
     * @param maxDtStart
     * @return modified calendar, containing all loaded events */
    @Suppress("MemberVisibilityCanBePrivate", "RedundantVisibilityModifier")
    public fun loadEventsForCalendar(calendar: Calendar, minDtEnd: Long, maxDtStart: Long): Calendar {
        /* read events for the calendar */
        /* build query */
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        /* retrieve all events that start before maxDtStart */
        val selection = "((${CalendarContract.Events.DTEND} > ?) AND (${CalendarContract.Events.DTSTART} < ?))"
        val selectionArgs: Array<String> = arrayOf("$minDtEnd", "$maxDtStart")

        var cursor: Cursor? = null
        try {
            /* run query */
            cursor = contentResolver.query(uri, EVENTS_PROJECTION, selection, selectionArgs, null)
            /* create new list to save found events */
            val events: MutableList<Event> = mutableListOf()
            /* iterate through the cursor */
            while (cursor.moveToNext()) {
                /* instantiate new event */
                val event = Event(
                    cursor.getLong(EVENT_PROJECTION_ID_INDEX),
                    cursor.getString(EVENT_PROJECTION_TITLE_INDEX),
                    cursor.getInt(EVENT_PROJECTION_COLOR_INDEX),
                    cursor.getString(EVENT_PROJECTION_DESCRIPTION_INDEX),
                    cursor.getString(EVENT_PROJECTION_LOCATION_INDEX),
                    cursor.getLong(EVENT_PROJECTION_CALENDAR_ID_INDEX),
                    cursor.getLong(EVENT_PROJECTION_DTSTART_INDEX),
                    cursor.getLong(EVENT_PROJECTION_DTEND_INDEX),
                    cursor.getInt(EVENT_PROJECTION_ALL_DAY_INDEX) != 0
                )
                events.add(event)
            }
            /* overwrite empty events list */
            calendar.events = events
            return calendar
        } catch (e: SecurityException) {
            /* probably READ_CALENDAR permission not granted */
            return calendar
        } finally {
            /* finally close cursor */
            cursor?.close()
        }
    }
}