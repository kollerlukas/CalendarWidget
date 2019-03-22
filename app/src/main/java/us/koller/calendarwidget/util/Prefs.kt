package us.koller.calendarwidget.util

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import us.koller.calendarwidget.R

/**
 * Helper class to easily load and store CalendarWidgetPrefs in SharedPreferences.
 * */
class Prefs(private val context: Context) {

    companion object {
        private var DAYS_SHOWN_IN_WIDGET: Int = 14

        private fun getSharedPrefs(context: Context): SharedPreferences {
            /* load preferences file key */
            val prefFileKey = context.getString(R.string.preference_file_key)
            /* retrieve SharedPreferences instance */
            return context.getSharedPreferences(prefFileKey, Context.MODE_PRIVATE)
        }
    }

    /**
     * @return the amount of days the widget shows calendar events for.
     * */
    fun getDaysShownInWidget(): Int {
        return DAYS_SHOWN_IN_WIDGET
    }

    /**
     * Load the currently stored CalendarWidgetPrefs for a given widgetId.
     * @param widgetId
     * @return stored CalendarWidgetPrefs for given widgetId
     * */
    fun loadWidgetPrefs(widgetId: Int): CalendarWidgetPrefs {
        /* retrieve shared prefs instance */
        val sharedPrefs = getSharedPrefs(context)
        /* load shared prefs key and format with widgetId */
        val key = String.format(context.getString(R.string.calendar_widget_prefs_key), widgetId)
        /* load json from shared prefs */
        val json = sharedPrefs.getString(key, Gson().toJson(CalendarWidgetPrefs()) /* default value empty */)
        /* convert json back to CalendarWidgetPrefs instance */
        return Gson().fromJson(json, CalendarWidgetPrefs::class.java)
    }

    /**
     * Store CalendarWidgetPrefs for a given widgetId.
     * @param prefs
     * */
    fun storeWidgetPrefs(prefs: CalendarWidgetPrefs) {
        /* retrieve shared prefs instance */
        val sharedPrefs = getSharedPrefs(context)
        /* load shared prefs key and format with widgetId */
        val key = String.format(context.getString(R.string.calendar_widget_prefs_key), prefs.widgetId)
        with(sharedPrefs.edit()) {
            /* convert to json and write to shared prefs */
            putString(key, Gson().toJson(prefs))
            apply()
        }
    }
}

/**
 * Data class to encapsulate preferences for a calendar widget.
 * */
data class CalendarWidgetPrefs(
    val widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    var calendarIds: List<Long> = emptyList()
)