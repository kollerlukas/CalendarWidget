package us.koller.calendarwidget.util

import android.widget.RemoteViews
import android.widget.RemoteViewsService

/**
 * RemoteViewsFactory that add the functionality to add a persistent footer at the end of the listview.
 * */
abstract class FooterRemoteViewsFactory : RemoteViewsService.RemoteViewsFactory {

    /**
     * Provide the remote-footer-view that should be displayed at the end of the list.
     * */
    abstract fun getFooter(): RemoteViews

    /**
     * Delegating function for @see RemoteViewsService.RemoteViewsFactory.getItemId().
     * @param index
     * */
    abstract fun getItemViewId(index: Int): Long

    final override fun getItemId(index: Int): Long {
        if (index == getItemCount()) {
            return 1 /* return a uneven number */
        }
        return getItemViewId(index) * 2 /* map item ids bijective to even numbers */
    }

    /**
     * Delegating function for @see RemoteViewsService.RemoteViewsFactory.getViewAt().
     * @param index
     * */
    abstract fun getItemViewAt(index: Int): RemoteViews

    final override fun getViewAt(index: Int): RemoteViews {
        if (index == getItemCount()) {
            /* last list item */
            return getFooter()
        }
        return getItemViewAt(index)
    }

    /**
     * Return the number of items the list is displaying. (without the footer)
     * Delegating function for @see RemoteViewsService.RemoteViewsFactory.getCount().
     * */
    abstract fun getItemCount(): Int

    final override fun getCount(): Int = getItemCount() + 1

    /**
     * Delegating function for @see RemoteViewsService.RemoteViewsFactory.getViewTypeCount().
     * */
    abstract fun getItemViewTypeCount(): Int

    final override fun getViewTypeCount(): Int = getItemViewTypeCount() + 1 /* adds 1 for footer view type */
}