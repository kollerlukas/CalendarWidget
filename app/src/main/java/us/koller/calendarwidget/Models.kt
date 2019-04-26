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
data class Event(
    val id: Long = -1L,
    val title: String? = "",
    val displayColor: Int = 0,
    val description: String? = "",
    val location: String? = "",
    val calendarId: Long = -1L,
    val dtstart: Long = -1L,
    val duration: String? = null,
    val allDay: Boolean = false,
    var instances: List<Instance> = emptyList()
) {
    /**
     * data class to hold selected columns from an event instance received by CalendarContract.Instances:
     *      https://developer.android.com/reference/android/provider/CalendarContract.Instances.html
     * */
    data class Instance(
        val id: Long = -1L,
        val begin: Long = -1L,
        val end: Long = -1L,
        val event: Event = Event()
    )
}