package us.koller.calendarwidget

import android.database.Cursor
import android.provider.CalendarContract
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * test class for CalendarLoaderImpl
 * */
@RunWith(MockitoJUnitRunner::class)
class CalendarLoaderImplTest {

    companion object {
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
        val dtend = dtstart + 60 * 1000
        const val allDay = 0

        /* helper functions to populate mocks */
        /**
         *
         * */
        fun populateCalendarResolver(mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper, mockCursor: Cursor) {
            /* return mock cursor on query */
            Mockito.`when`(
                mockContentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI, CalendarLoaderImpl.CALENDAR_PROJECTION, "", emptyArray(),
                    null
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
            val selection = "((${CalendarContract.Events.DTEND} > ?) AND (${CalendarContract.Events.DTSTART} < ?))"
            Mockito.`when`(
                mockContentResolver.query(
                    eq(CalendarContract.Events.CONTENT_URI), eq(CalendarLoaderImpl.EVENTS_PROJECTION), eq(selection),
                    any(Array<String>::class.java), eq(null)
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
            Mockito.`when`(mockCursor.getString(CalendarLoaderImpl.EVENT_PROJECTION_LOCATION_INDEX)).thenReturn(location)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_CALENDAR_ID_INDEX)).thenReturn(calendarId)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_DTSTART_INDEX)).thenReturn(dtstart)
            Mockito.`when`(mockCursor.getLong(CalendarLoaderImpl.EVENT_PROJECTION_DTEND_INDEX)).thenReturn(dtend)
            Mockito.`when`(mockCursor.getInt(CalendarLoaderImpl.EVENT_PROJECTION_ALL_DAY_INDEX)).thenReturn(allDay)
        }
    }

    /* mocks */
    @Mock
    private lateinit var mockContentResolver: CalendarLoaderImpl.ContentResolverWrapper
    @Mock
    private lateinit var mockCalendarCursor: Cursor
    @Mock
    private lateinit var mockEventCursor: Cursor

    /* class under test */
    private lateinit var loader: CalendarLoader

    @Before
    fun setUp() {
        /* populate mocks with mock data */
        populateCalendarResolver(mockContentResolver, mockCalendarCursor)
        populateEventResolver(mockContentResolver, mockEventCursor)
        /* return mock calendar data */
        populateCalendarCursor(mockCalendarCursor)
        /* return mock event data */
        populateEventCursor(mockEventCursor)

        /* instantiate class under test */
        loader = CalendarLoaderImpl(mockContentResolver)
    }

    @Test
    fun testLoadEvents() {
        /* mock output-data */
        val mockOutputEvents = listOf(
            Event(eventId, title, displayColor, description, location, calendarId, dtstart, dtend, allDay != 0)
        )

        /* call method to test */
        val events = loader.loadEvents(1)
        /* verify output */
        if (events != mockOutputEvents) {
            fail("Wrong output:\n\texpected: $mockOutputEvents\n\tactual:$events")
        }
    }

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

    @Test
    fun testLoadEventsForCalendar() {
        /* mock input-data */
        val mockInputCalendar = Calendar(calendarId, displayName, calendarColor, accountName, ownerName, emptyList())
        /* params */
        val minDtEnd = System.currentTimeMillis()
        val maxDtStart = minDtEnd + 60 * 1000
        /* mock output-data */
        val mockOutputCalendar = Calendar(
            mockInputCalendar.id, mockInputCalendar.displayName, mockInputCalendar.calendarColor,
            mockInputCalendar.accountName, mockInputCalendar.ownerName,
            listOf(
                Event(eventId, title, displayColor, description, location, calendarId, dtstart, dtend, allDay != 0)
            )
        )

        /* call method to test */
        val calendar = loader.loadEventsForCalendar(mockInputCalendar, minDtEnd, maxDtStart)

        /* verify output */
        if (calendar != mockOutputCalendar) {
            fail("Wrong output:\n\texpected: $mockOutputCalendar\n\tactual:$calendar")
        }
    }
}