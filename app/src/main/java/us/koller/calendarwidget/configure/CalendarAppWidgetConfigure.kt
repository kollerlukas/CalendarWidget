package us.koller.calendarwidget.configure

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_widget_configure.*
import us.koller.calendarwidget.CalendarLoaderImpl
import us.koller.calendarwidget.R
import us.koller.calendarwidget.util.CalendarWidgetPrefs
import us.koller.calendarwidget.util.Prefs

/**
 * WidgetConfigure Activity: User has the option to exclude calendars from showing up in the widget.
 * */
class CalendarAppWidgetConfigure : AppCompatActivity(), View.OnClickListener {

    companion object {
        /* requestCode for the permission call */
        private const val PERM_REQUEST_CODE: Int = 1
    }

    /* store id of associated widget  */
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    /* store which calendars are selected and which not: Map CalendarId to selected state */
    private lateinit var calendarSelected: MutableMap<Long, Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configure)

        /* set RecyclerView LayoutManager */
        recycler_view.layoutManager = LinearLayoutManager(this)

        /* retrieve the widgetId from the intent */
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        /* permission needed to read the calendar */
        val perm: String = Manifest.permission.READ_CALENDAR

        /* check if READ_CALENDAR permission is granted */
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            /* permission is not granted => request permission */
            ActivityCompat.requestPermissions(
                this, arrayOf(perm),
                PERM_REQUEST_CODE
            )
            return
        } else {
            /* load and display available calendars */
            loadCalendarsIntoRecyclerView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        /* inflate menu */
        menuInflater.inflate(R.menu.menu_calendar_appwidget_configure, menu)
        /* display menu => return true */
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.done -> {
            /* user is done configuring the widget => add it to the home screen */
            setResultAndFinish(
                Activity.RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            )
            /* action was consumed => return true */
            true
        }
        else -> {
            /* user action was not recognized => call super */
            super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.checkbox -> {
                /* retrieve calendar id from view tag (was set in CalendarAdapter in CalendarHolder.bind() */
                val calendarId = view.tag as? Long
                /* find out whether checkbox is checked or not */
                val checked = (view as? CheckBox)?.isChecked
                /* modify calendar selection state */
                checked?.let { calendarId?.let { cId -> calendarSelected.replace(cId, it) } }
            }
            else -> {
                return
            }
        }
    }

    /**
     * populate the RecyclerView with calendars
     * */
    private fun loadCalendarsIntoRecyclerView() {
        /* load available calendars */
        val calendars = CalendarLoaderImpl.wrap(contentResolver).loadCalendars()
        Log.d("CalendarAppWidgetConfigure", "calendars: $calendars")
        /* create new Map to track each calendar selection state */
        calendarSelected = calendars.map { it.id to true }.toMap().toMutableMap()
        /* set Calendars */
        recycler_view.adapter = CalendarAdapter(calendars)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_REQUEST_CODE -> {
                /* result arrays empty, when request was canceled */
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    /* permission was granted => load calendars */
                    loadCalendarsIntoRecyclerView()
                } else {
                    /* permission was denied => cancel widget */
                    setResultAndFinish(Activity.RESULT_CANCELED)
                }
                return
            }
            else -> {
                /* other request */
                setResultAndFinish(Activity.RESULT_CANCELED)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /* user clicked back => cancel */
        setResultAndFinish(Activity.RESULT_CANCELED)
    }

    private fun setResultAndFinish(resultCode: Int, data: Intent? = null) {
        if (resultCode == Activity.RESULT_OK) {
            /* store widget preferences */
            /* filter selected calendars */
            val selectedCalendars = calendarSelected.toList().filter { it.second }.map { it.first }
            /* store in Preferences */
            Prefs(this).storeWidgetPrefs(CalendarWidgetPrefs(widgetId, selectedCalendars))
        }

        if (data != null) {
            setResult(resultCode, data)
        } else {
            setResult(resultCode)
        }
        finish()
    }
}