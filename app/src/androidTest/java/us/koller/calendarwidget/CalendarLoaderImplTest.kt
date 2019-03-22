package us.koller.calendarwidget

import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * test class for CalendarLoaderImpl
 * */
@RunWith(MockitoJUnitRunner::class)
class CalendarLoaderImplTest {

    companion object {
        /* query params */
        /* calendars query params */
        private const val nextDaysQueryParam = 1
        /* event query params */
        private val minDtEndQueryParam = System.currentTimeMillis()
        private val maxDtStartQueryParam = minDtEndQueryParam + 60 * 1000
        /* event instance query params */
        private val beginQueryParam = System.currentTimeMillis()
        private val endQueryParam = minDtEndQueryParam + 60 * 1000

        /* mock input-data */
        /* calendar mock-data */
        const val calendarId = 1L
        const val displayName = "TestDisplayName"
        const val calendarColor = 16777215 /* = #FFFFFF */
        const val accountName = "TestAccountName"
        const val ownerName = "TestOwnerName"
        /* event mock-data */
        const val eventId = 2L
        const val title = "TestDisplayName"
        const val displayColor = 16777215 /* = #FFFFFF */
        const val description = "TestDescription"
        const val location = "TestLocation"
        /* val calendarId = 1L // already in calendar mock-data*/
        val dtstart = System.currentTimeMillis() + 1000
        const val duration = ""
        const val allDay = 0
        /* event instance mock data */
        const val instanceId = 3L
        val begin = System.currentTimeMillis() + 1000
        val end = begin + 60 * 1000

        /* helper functions to populate mocks */
        /**
         *
         * */
        fun populateCalendarResolver(
            mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper,
            mockCursor: Cursor
        ) {
            /* return mock cursor on query */
            Mockito.`when`(
                mockContentResolver.query(
                    eq(CalendarContract.Calendars.CONTENT_URI),
                    eq(CalendarLoaderImpl.CALENDAR_PROJECTION),
                    eq(""),
                    eq(emptyArray()),
                    eq(null)
                )
            ).thenReturn(mockCursor)
        }

        /**
         *
         * */
        fun populateCalendarCursor(mockCursor: Cursor) {
            Mockito.`when`(mockCursor.moveToNext()).thenReturn(true, false)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.CALENDAR_PROJECTION_ID_INDEX)).thenReturn(calendarId)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.CALENDAR_PROJECTION_DISPLAY_NAME_INDEX))
                .thenReturn(displayName)
            Mockito.`when`(mockCursor.getInt(CalendarLoaderImpl.CALENDAR_PROJECTION_CALENDAR_COLOR_INDEX))
                .thenReturn(calendarColor)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX))
                .thenReturn(accountName)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX))
                .thenReturn(ownerName)
        }

        /**
         *
         * */
        fun populateEventResolver(mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper, mockCursor: Cursor) {
            Mockito.`when`(
                mockContentResolver.query(
                    eq(CalendarContract.Events.CONTENT_URI),
                    eq(CalendarLoaderImpl.EVENTS_PROJECTION),
                    eq(
                        "((${CalendarContract.Events.CALENDAR_ID} = ?) AND (((${CalendarContract.Events.DTEND} > ?) " +
                                "AND (${CalendarContract.Events.DTSTART} < ?)) OR (${CalendarContract.Events.RRULE} != \"\" A" +
                                "ND ${CalendarContract.Events.RRULE} IS NOT NULL)))"
                    ),
                    any(Array<String>::class.java),
                    eq("${CalendarContract.Events.DTSTART} ASC")
                )
            ).thenReturn(mockCursor)
        }

        /**
         *
         * */
        fun populateEventCursor(mockCursor: Cursor) {
            Mockito.`when`(mockCursor.moveToNext()).thenReturn(true, false)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_ID_INDEX)).thenReturn(eventId)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.EVENT_PROJECTION_TITLE_INDEX)).thenReturn(title)
            Mockito.`when`(mockCursor.getInt(CalendarLoaderImpl.EVENT_PROJECTION_COLOR_INDEX)).thenReturn(displayColor)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.EVENT_PROJECTION_DESCRIPTION_INDEX))
                .thenReturn(description)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.EVENT_PROJECTION_LOCATION_INDEX))
                .thenReturn(location)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_CALENDAR_ID_INDEX))
                .thenReturn(calendarId)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_DTSTART_INDEX)).thenReturn(dtstart)
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.EVENT_PROJECTION_DURATION_INDEX))
                .thenReturn(duration)
            Mockito.`when`(mockCursor.getInt(CalendarLoaderImpl.EVENT_PROJECTION_ALL_DAY_INDEX)).thenReturn(allDay)
        }

        /**
         *
         * */
        fun populateEventInstanceResolver(
            mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper,
            mockCursor: Cursor
        ) {
            val uri: Uri = CalendarContract.Instances.CONTENT_URI
                .buildUpon()
                /* add range: begin > minEnd && end < maxBegin */
                .appendPath("$beginQueryParam")
                .appendPath("$endQueryParam")
                .build()
            /* retrieve all events instances the are in the search window between minEnd & maxStart */
            Mockito.`when`(
                mockContentResolver.query(
                    any(Uri::class.java), /* don't know how to check uri, because it contains params */
                    eq(CalendarLoaderImpl.INSTANCES_PROJECTION),
                    eq("(Events.${CalendarContract.Events._ID} = ?)"),
                    any(Array<String>::class.java),
                    eq("${CalendarContract.Instances.BEGIN} ASC")
                )
            ).thenReturn(mockCursor)
        }

        /**
         *
         * */
        fun populateEventInstanceCursor(mockCursor: Cursor) {
            Mockito.`when`(mockCursor.moveToNext()).thenReturn(true, false)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.INSTANCES_PROJECTION_ID_INDEX)).thenReturn(instanceId)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.INSTANCES_PROJECTION_BEGIN_INDEX)).thenReturn(begin)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.INSTANCES_PROJECTION_END_INDEX)).thenReturn(end)
        }
    }

    /* mocks */
    @Mock
    private lateinit var mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper
    @Mock
    private lateinit var mockCalendarCursor: Cursor
    @Mock
    private lateinit var mockEventCursor: Cursor
    @Mock
    private lateinit var mockEventInstanceCursor: Cursor

    /* class under test */
    private lateinit var loader: CalendarLoader

    /**
     * setup mocks and class under test
     * */
    @Before
    fun setUp() {
        /* populate mocks with mock data */
        populateCalendarResolver(mockContentResolver, mockCalendarCursor)
        populateEventResolver(mockContentResolver, mockEventCursor)
        populateEventInstanceResolver(mockContentResolver, mockEventInstanceCursor)
        /* return mock calendar data */
        populateCalendarCursor(mockCalendarCursor)
        /* return mock event data */
        populateEventCursor(mockEventCursor)
        /* return mock event instance data */
        populateEventInstanceCursor(mockEventInstanceCursor)

        /* instantiate class under test */
        loader = CalendarLoaderImpl(mockContentResolver)
    }

    /**
     * test CalendarLoaderImpl.loadEvents()
     * */
    @Test
    fun testLoadEvents() {
        /* mock output-data */
        val mockOutputEvents = listOf(
            Event.Instance(
                instanceId, begin, end,
                Event(eventId, title, displayColor, description, location, calendarId, dtstart, duration, allDay == 1)
            )
        )

        /* call method to test */
        val events = loader.loadEventInstances(nextDaysQueryParam)
        /* verify output */
        if (events != mockOutputEvents) {
            fail("Wrong output:\n\texpected: $mockOutputEvents\n\tactual:$events")
        }
    }

    /**
     * test CalendarLoaderImpl.loadCalendars()
     * */
    @Test
    fun testLoadCalendars() {
        /* mock output-data */
        val mockOutputCalendars = listOf(
            Calendar(calendarId, displayName, calendarColor, accountName, ownerName, emptyList())
        )

        /* call method to test */
        val calendars = loader.loadCalendars()

        /* verify output */
        if (calendars != mockOutputCalendars) {
            fail("Wrong output:\n\texpected: $mockOutputCalendars\n\tactual:$calendars")
        }
    }

    /**
     * test CalendarLoaderImpl.loadEventsForCalendar()
     * */
    @Test
    fun testLoadEventsForCalendar() {
        /* mock input-data */
        val mockInputCalendar = Calendar(calendarId, displayName, calendarColor, accountName, ownerName, emptyList())
        /* mock output-data */
        val mockOutputEvents = listOf(
            Event(eventId, title, displayColor, description, location, calendarId, dtstart, duration, allDay == 1)
        )

        /* call method to test */
        val events = loader.loadEventsForCalendar(mockInputCalendar, minDtEndQueryParam, maxDtStartQueryParam)

        /* verify output */
        if (events != mockOutputEvents) {
            fail("Wrong output:\n\texpected: $mockOutputEvents\n\tactual:$events")
        }
    }

    /**
     * test CalendarLoaderImpl.loadInstancesForEvent()
     * */
    @Test
    fun testLoadInstancesForEvent() {
        /* mock input-data */
        val mockInputEvent =
            Event(eventId, title, displayColor, description, location, calendarId, dtstart, duration, allDay == 1)
        /* mock output-data */
        val mockOutputInstances = listOf(
            Event.Instance(instanceId, begin, end, mockInputEvent)
        )

        /* call method to test */
        val instances = loader.loadInstancesForEvent(mockInputEvent, beginQueryParam, endQueryParam)

        /* verify output */
        if (instances != mockOutputInstances) {
            fail("Wrong output:\n\texpected: $mockOutputInstances\n\tactual:$instances")
        }
    }
}