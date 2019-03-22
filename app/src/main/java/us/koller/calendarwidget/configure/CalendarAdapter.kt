package us.koller.calendarwidget.configure

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.calendar_item_view.view.*
import us.koller.calendarwidget.Calendar
import us.koller.calendarwidget.R

/**
 * RecyclerView Adapter for the RecyclerView of CalendarAppWidgetConfigure
 * */
class CalendarAdapter(private val calendars: List<Calendar>) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun getItemCount() = calendars.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        /* inflate itemView */
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_item_view, parent, false)
        /* create new ViewHolder */
        return CalendarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        /* bind ViewHolder */
        holder.bind(calendars[position])
    }

    /**
     * Calendar ViewHolder class for the RecyclerView of CalendarAppWidgetConfigure
     * */
    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * helper method to easily bind a viewHolder
         * */
        fun bind(calendar: Calendar) {
            /* set checkbox color */
            itemView.checkbox.buttonTintList = ColorStateList.valueOf(calendar.calendarColor)
            /* set calendar id as tag */
            itemView.checkbox.tag = calendar.id
            /* set calendar name */
            itemView.calendar_name.text = calendar.displayName
        }
    }
}