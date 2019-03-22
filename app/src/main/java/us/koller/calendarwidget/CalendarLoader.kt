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

    /** load all instances of events from all calendars that start within the a given amount of days (nextDays)
     * @param nextDays
     * @return a list of all events */
    @Suppress("RedundantVisibilityModifier")
    public fun loadEventInstances(nextDays: Int): List<Event.Instance>

    /** load all calendars
     * @return a list of all found calendars (events attribute is empty) */
    @Suppress("RedundantVisibilityModifier")
    public fun loadCalendars(): List<Calendar>

    /** load all events that start before a given timestamp (maxDtStart) and end after another given timestamp (minDtEnd) in for a given calendar (calendar)
     * @param calendar
     * @param minDtEnd
     * @param maxDtStart
     * @return list containing all loaded events (instances attribute is empty) */
    @Suppress("MemberVisibilityCanBePrivate", "RedundantVisibilityModifier")
    public fun loadEventsForCalendar(calendar: Calendar, minDtEnd: Long, maxDtStart: Long): List<Event>

    /**
     * Function that load all instances of a given event.
     * @param event for which the instances should be loaded
     * @param begin of search range
     * @param end of search range
     * @return list of found instances
     * */
    @Suppress("RedundantVisibilityModifier")
    public fun loadInstancesForEvent(event: Event, begin: Long, end: Long): List<Event.Instance>
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
            CalendarContract.Events.DURATION,
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
        const val EVENT_PROJECTION_DURATION_INDEX = 7
        const val EVENT_PROJECTION_ALL_DAY_INDEX = 8

        /* instances projection array */
        val INSTANCES_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END
        )
        /* instances projection indices */
        const val INSTANCES_PROJECTION_ID_INDEX = 0
        const val INSTANCES_PROJECTION_BEGIN_INDEX = 1
        const val INSTANCES_PROJECTION_END_INDEX = 2
    }

    override fun loadEventInstances(nextDays: Int): List<Event.Instance> {
        val currTimeStamp = System.currentTimeMillis()
        val nextDaysMillis = nextDays * 24 * 60 * 60 * 1000
        return loadCalendars().asSequence()
            /* load all the events for each calendar */
            .map { loadEventsForCalendar(it, currTimeStamp, currTimeStamp + nextDaysMillis) }
            .flatten()
            /* load all instances for each event */
            .map { loadInstancesForEvent(it, currTimeStamp, currTimeStamp + nextDaysMillis) }
            .flatten()
            /* sort the events by starting time */
            .sortedBy { it.begin }
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

    override fun loadEventsForCalendar(calendar: Calendar, minDtEnd: Long, maxDtStart: Long): List<Event> {
        /* read events for the calendar */
        /* build query */
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        /* retrieve all events that start before maxDtStart */
        val selection = "(" +
                "(${CalendarContract.Events.CALENDAR_ID} = ?)" + /* all event from calendar */
                " AND (" +
                /* between minDtEnd and maxDtStart */
                "((${CalendarContract.Events.DTEND} > ?) AND (${CalendarContract.Events.DTSTART} < ?)) OR " +
                /* or is a recurring event */
                "(${CalendarContract.Events.RRULE} != \"\" AND ${CalendarContract.Events.RRULE} IS NOT NULL)" + "))"
        /* pass query parameters */
        val selectionArgs: Array<String> = arrayOf("${calendar.id}", "$minDtEnd", "$maxDtStart")
        /* sort by start time */
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
        var cursor: Cursor? = null
        try {
            /* run query */
            cursor = contentResolver.query(uri, EVENTS_PROJECTION, selection, selectionArgs, sortOrder)
            /* create new list to save found events */
            val events: MutableList<Event> = mutableListOf()
            /* iterate through the cursor */
            while (cursor.moveToNext()) {
                Event(
                    cursor.getLong(EVENT_PROJECTION_ID_INDEX),
                    cursor.getString(EVENT_PROJECTION_TITLE_INDEX),
                    cursor.getInt(EVENT_PROJECTION_COLOR_INDEX),
                    cursor.getString(EVENT_PROJECTION_DESCRIPTION_INDEX),
                    cursor.getString(EVENT_PROJECTION_LOCATION_INDEX),
                    cursor.getLong(EVENT_PROJECTION_CALENDAR_ID_INDEX),
                    cursor.getLong(EVENT_PROJECTION_DTSTART_INDEX),
                    cursor.getString(EVENT_PROJECTION_DURATION_INDEX),
                    cursor.getInt(EVENT_PROJECTION_ALL_DAY_INDEX) == 1
                ).let { events.add(it) }
            }
            /* return events */
            return events
        } catch (e: SecurityException) {
            /* probably READ_CALENDAR permission not granted */
            return emptyList()
        } finally {
            /* finally close cursor */
            cursor?.close()
        }
    }

    override fun loadInstancesForEvent(event: Event, begin: Long, end: Long): List<Event.Instance> {
        /* build query */
        val uri: Uri = CalendarContract.Instances.CONTENT_URI
            .buildUpon()
            /* add range: begin > minEnd && end < maxBegin */
            .appendPath("$begin")
            .appendPath("$end")
            .build()
        /* retrieve all events instances the are in the search window between minEnd & maxStart */
        val selection = "(Events.${CalendarContract.Events._ID} = ?)" /* all instances of an event */
        /* pass query parameters */
        val selectionArgs: Array<String> = arrayOf("${event.id}")
        /* sort by begin time */
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"
        var cursor: Cursor? = null
        try {
            /* run query */
            cursor = contentResolver.query(uri, INSTANCES_PROJECTION, selection, selectionArgs, sortOrder)
            /* create new list to save found events */
            val instances: MutableList<Event.Instance> = mutableListOf()
            /* iterate through the cursor */
            while (cursor.moveToNext()) {
                Event.Instance(
                    cursor.getLong(INSTANCES_PROJECTION_ID_INDEX),
                    cursor.getLong(INSTANCES_PROJECTION_BEGIN_INDEX),
                    cursor.getLong(INSTANCES_PROJECTION_END_INDEX),
                    event
                ).let { instances.add(it) }
            }
            /* return instances */
            return instances
        } catch (e: SecurityException) {
            /* probably READ_CALENDAR permission not granted */
            return emptyList()
        } finally {
            /* finally close cursor */
            cursor?.close()
        }
    }
}