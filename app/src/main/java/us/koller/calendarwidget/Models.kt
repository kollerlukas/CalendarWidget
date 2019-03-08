package us.koller.calendarwidget

/* data class to hold selected columns from a calendar received by CalendarContract.Calendars:
*  https://developer.android.com/guide/topics/providers/calendar-provider#calendar */
data class Calendar(
    var id: Long?,
    var displayName: String?,
    var calendarColor: Int?,
    var accountName: String?,
    var ownerName: String?,
    var events: List<Event>?)

/* data class to hold selected columns from an event received by CalendarContract.Events:
*  https://developer.android.com/guide/topics/providers/calendar-provider#events */
data class Event(
    var id: Long,
    var title: String?,
    var displayColor: Int?,
    var description: String?,
    var location: String?,
    var calendarId: Long?,
    var dtstart: Long?,
    var dtend: Long?,
    var allDay: Boolean?)