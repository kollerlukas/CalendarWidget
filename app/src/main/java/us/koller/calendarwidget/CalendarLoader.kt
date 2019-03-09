package us.koller.calendarwidget

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract

/**
 * CalendarLoader-interface to allow for mocking in testing
 * CalendarLoader: helper to load calendar & events synchronously
 * */
interface CalendarLoader {

    /** load all events from all calendars that start within the a given amount of days (nextDays)
     * @param nextDays
     * @return a list of all events */
    @Suppress("RedundantVisibilityModifier")
    public fun loadEvents(nextDays: Int): List<Event>

    /** load all calendars
     * @return a list of all found calendars (events attribute is empty) */
    @Suppress("RedundantVisibilityModifier")
    public fun loadCalendars(): List<Calendar>

    /** load all events that start before a given timestamp (maxDtStart) and end after another given timestamp (minDtEnd) in for a given calendar (calendar)
     * @param calendar
     * @param minDtEnd
     * @param maxDtStart
     * @return modified calendar, containing all loaded events */
    @Suppress("MemberVisibilityCanBePrivate", "RedundantVisibilityModifier")
    public fun loadEventsForCalendar(calendar: Calendar, minDtEnd: Long, maxDtStart: Long): Calendar
}

/**
 * implementation of the CalendarLoader interface
 *  */
class CalendarLoaderImpl(private val contentResolver: ContentResolverWrapper) : CalendarLoader {

    /**
     * simple wrapper interface to allow mocking for testing
     * */
    interface ContentResolverWrapper {
        /**
         * wrapper function for ContentResolver.query()
         * */
        fun query(
            uri: Uri?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor
    }

    companion object {
        /**
         * simple function to wrap contentResolver
         * */
        fun wrap(contentResolver: ContentResolver): CalendarLoader {
            return CalendarLoaderImpl(object : ContentResolverWrapper {
                override fun query(
                    uri: Uri?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
                    sortOrder: String?
                ): Cursor {
                    return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                }
            })
        }

        /* calendar projection array */
        val CALENDAR_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )
        /* calendar projection indices */
        const val CALENDAR_PROJECTION_ID_INDEX = 0
        const val CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX = 1
        const val CALENDAR_PROJECTION_CALENDAR_COLOR_INDEX = 2
        const val CALENDAR_PROJECTION_DISPLAY_NAME_INDEX = 3
        const val CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX = 4

        /* event projection array */
        val EVENTS_PROJECTION: Array<String> = arrayOf(
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
        const val EVENT_PROJECTION_ID_INDEX = 0
        const val EVENT_PROJECTION_TITLE_INDEX = 1
        const val EVENT_PROJECTION_COLOR_INDEX = 2
        const val EVENT_PROJECTION_DESCRIPTION_INDEX = 3
        const val EVENT_PROJECTION_LOCATION_INDEX = 4
        const val EVENT_PROJECTION_CALENDAR_ID_INDEX = 5
        const val EVENT_PROJECTION_DTSTART_INDEX = 6
        const val EVENT_PROJECTION_DTEND_INDEX = 7
        const val EVENT_PROJECTION_ALL_DAY_INDEX = 8
    }

    override fun loadEvents(nextDays: Int): List<Event> {
        val currTimeStamp = System.currentTimeMillis()
        val nextDaysMillis = nextDays * 24 * 60 * 60 * 1000
        return loadCalendars().asSequence()
            /* load all the events for each calendar */
            .map { c -> loadEventsForCalendar(c, currTimeStamp, currTimeStamp + nextDaysMillis) }
            /* map each calendar onto its events */
            .map { c -> c.events.filter { e -> e.dtstart - currTimeStamp < nextDaysMillis } }
            .flatten()
            /* sort the events by starting time */
            .sortedBy { it.dtstart }
            /* remove duplicate events */
            .distinctBy { it.id }
            .toList()
    }

    override fun loadCalendars(): List<Calendar> {
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
                /* instantiate calendar + add to the list */
                Calendar(
                    cursor.getLong(CALENDAR_PROJECTION_ID_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX),
                    cursor.getInt(CALENDAR_PROJECTION_CALENDAR_COLOR_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX),
                    cursor.getString(CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX)
                ).let { calendars.add(it) }
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

    override fun loadEventsForCalendar(calendar: Calendar, minDtEnd: Long, maxDtStart: Long): Calendar {
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
                Event(
                    cursor.getLong(EVENT_PROJECTION_ID_INDEX),
                    cursor.getString(EVENT_PROJECTION_TITLE_INDEX),
                    cursor.getInt(EVENT_PROJECTION_COLOR_INDEX),
                    cursor.getString(EVENT_PROJECTION_DESCRIPTION_INDEX),
                    cursor.getString(EVENT_PROJECTION_LOCATION_INDEX),
                    cursor.getLong(EVENT_PROJECTION_CALENDAR_ID_INDEX),
                    cursor.getLong(EVENT_PROJECTION_DTSTART_INDEX),
                    cursor.getLong(EVENT_PROJECTION_DTEND_INDEX),
                    cursor.getInt(EVENT_PROJECTION_ALL_DAY_INDEX) != 0
                ).let { events.add(it) }
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