package us.koller.calendarwidget

/**
 * data class to hold selected columns from a calendar received by CalendarContract.Calendars:
 *      https://developer.android.com/guide/topics/providers/calendar-provider#calendar
 * */
data class Calendar(
    val id: Long = -1L,
    val displayName: String = "Error",
    val calendarColor: Int = 0,
    val accountName: String = "Error",
    val ownerName: String = "Error",
    var events: List<Event> = emptyList()
)

/**
 * data class to hold selected columns from an event received by CalendarContract.Events:
 *      https://developer.android.com/guide/topics/providers/calendar-provider#events
 * */
data class Event (
    var id: Long = -1L,
    var title: String = "Error",
    var displayColor: Int = 0,
    var description: String = "Error",
    var location: String = "Error",
    var calendarId: Long = -1L,
    var dtstart: Long = -1L,
    var dtend: Long = -1L,
    var allDay: Boolean = false
)