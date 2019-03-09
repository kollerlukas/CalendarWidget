package us.koller.calendarwidget

/**
 * data class to hold selected columns from a calendar received by CalendarContract.Calendars:
 *      https://developer.android.com/guide/topics/providers/calendar-provider#calendar
 * */
data class Calendar(
    val id: Long = -1L,
    val displayName: String = "",
    val calendarColor: Int = 0,
    val accountName: String = "",
    val ownerName: String = "",
    var events: List<Event> = emptyList()
)

/**
 * data class to hold selected columns from an event received by CalendarContract.Events:
 *      https://developer.android.com/guide/topics/providers/calendar-provider#events
 * */
data class Event (
    var id: Long = -1L,
    var title: String = "",
    var displayColor: Int = 0,
    var description: String = "",
    var location: String = "",
    var calendarId: Long = -1L,
    var dtstart: Long = -1L,
    var dtend: Long = -1L,
    var allDay: Boolean = false
)