package us.koller.calendarwidget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 *
 * */
class CalendarAppWidgetConfigure : AppCompatActivity() {

    companion object {
        private const val PERM_REQUEST_CODE: Int = 1
    }

    var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configure)

        /* retrieve the appWidget id from the intent */
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        /* permission needed to read the calendar */
        val perm: String = Manifest.permission.READ_CALENDAR

        /* check if READ_CALENDAR permission is granted */
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            /* permission is not granted => request permission */
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                /* asking for explanation => display explanation */
                // TODO: display explanation
                Log.d("CalendarAppWidgetConfig", "asking for explanation")
                setResult(Activity.RESULT_CANCELED)
                finish()
            } else {
                /* no explanation required => request permission */
                ActivityCompat.requestPermissions(this, arrayOf(perm), PERM_REQUEST_CODE)
            }
        } else {
            /* permission is already granted => add widget */
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_REQUEST_CODE -> {
                /* result arrays empty, when request was canceled */
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    /* permission was granted => add widget */
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(RESULT_OK, resultValue)
                    finish()
                } else {
                    /* permission was denied => cancel widget */
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                return
            }
            else -> {
                /* other request */
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
}