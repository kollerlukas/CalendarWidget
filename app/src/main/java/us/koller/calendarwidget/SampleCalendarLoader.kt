package us.koller.calendarwidget

import android.graphics.Color

/**
 * Show sample data in the widget
 * */
@Suppress("unused")
class SampleCalendarLoaderMock : CalendarLoader {

    companion object {
        private val calendars = listOf(
            Calendar(
                1, "", 0, "", "",
                listOf(
                    Event(
                        2,
                        "Linear algebra Lecture",
                        Color.parseColor("#64B5F6"),
                        "",
                        "",
                        1,
                        1552288500000,
                        1552293900000,
                        null,
                        false
                    ),
                    Event(
                        3,
                        "Lunch",
                        Color.parseColor("#F06292"),
                        "",
                        "",
                        1,
                        1552305600000,
                        1552311000000,
                        null,
                        false
                    ),
                    Event(
                        4,
                        "Afternoon Run",
                        Color.parseColor("#9575CD"),
                        "",
                        "",
                        1,
                        1552318200000,
                        1552323600000,
                        null,
                        false
                    ),
                    Event(
                        5,
                        "Algorithm and Data Structures Lecture",
                        Color.parseColor("#81C784"),
                        "",
                        "",
                        1,
                        1552381200000,
                        1552386600000,
                        null,
                        false
                    ),
                    Event(
                        6,
                        "Functional Programming Lecture",
                        Color.parseColor("#DCE775"),
                        "",
                        "",
                        1,
                        1552395600000,
                        1552401000000,
                        null,
                        false
                    ),
                    Event(
                        7,
                        "Linear algebra Lecture",
                        Color.parseColor("#64B5F6"),
                        "",
                        "",
                        1,
                        1552461300000,
                        1552466700000,
                        null,
                        false
                    )
                )
            )
        )
    }

    override fun loadEventInstances(nextDays: Int): List<Event.Instance> {
        return calendars
            .asSequence()
            .map { it.events }
            .flatten()
            .map { it.instances }
            .flatten()
            .sortedBy { it.begin }
            .toList()
    }

    override fun loadCalendars(): List<Calendar> {
        return calendars
            .map { c -> Calendar(c.id, c.displayName, c.calendarColor, c.accountName, c.ownerName) }
    }

    override fun loadEventsForCalendar(
        calendar: Calendar,
        minDtEnd: Long,
        maxDtStart: Long
    ): List<Event> {
        return calendars.find { it.id == calendar.id }?.events!!
    }

    override fun loadInstancesForEvent(event: Event, begin: Long, end: Long): List<Event.Instance> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}