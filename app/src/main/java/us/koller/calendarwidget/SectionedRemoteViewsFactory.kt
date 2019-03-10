package us.koller.calendarwidget

import android.widget.RemoteViews
import android.widget.RemoteViewsService
import java.lang.IllegalStateException

/**
 * RemoteViewsFactory that adds the ability to add sections between items
 * */
abstract class SectionedRemoteViewsFactory<T>(var packageName: String) : RemoteViewsService.RemoteViewsFactory {

    /**
     * abstract class to contain Items and Sections in the same list
     * */
    abstract class ListItem

    /**
     * class to encapsulate items
     * */
    class Item<T>(val t: T) : ListItem()

    /**
     * sections are listItems that are inserted into items
     * */
    class Section(val title: String) : ListItem()

    /* items that are displayed in the list view */
    private var items: MutableList<ListItem> = mutableListOf()
    private var sections: MutableList<Pair<Int, Section>> = mutableListOf()

    /**
     * set the items that should be displayed in the ListView. Removes all previously set sections
     * */
    fun setItems(items: List<T>) {
        this.items = items.map { Item(it) }.toMutableList()
        this.sections = mutableListOf()
    }

    /**
     * return the items from the ListView, without sections
     * */
    fun getItems(): List<T> {
        @Suppress("UNCHECKED_CAST")
        return items.filter { it is Item<*> }.map { (it as Item<T>).t }
    }

    /**
     * add a section to the ListView
     * */
    fun addSection(index: Int, title: String) {
        val section = Section(title)
        /* calculate the index in items */
        val sectionIndex = sections
            .filter { it.first < index }
            .count() + index
        /* add section to items */
        items.add(sectionIndex, section)
        /* store section */
        sections.add(Pair(index, section))
    }

    /**
     * provide itemId for item
     * */
    abstract fun getItemId(item: T): Long

    /**
     * provide RemoteView for Item
     * */
    abstract fun getViewAt(item: T): RemoteViews

    /* overridden methods from RemoteViewsService.RemoteViewsFactory */

    final override fun getItemId(index: Int): Long {
        return if (items[index] is Section) {
            index * 2L + 1L /* map to the uneven numbers */
        } else {
            @Suppress("UNCHECKED_CAST")
            getItemId((items[index] as Item<T>).t) * 2 /* map into the even numbers */
        }
    }

    final override fun hasStableIds(): Boolean {
        return true
    }

    final override fun getViewAt(index: Int): RemoteViews {
        when {
            items[index] is Section -> {
                val section: Section? = items[index] as? Section
                /* create section remoteView */
                val remoteViews = RemoteViews(packageName, R.layout.section_item_view)
                /* bind Data */
                /* set section title */
                remoteViews.setTextViewText(R.id.section_title, section?.title)
                /* return section remoteViews */
                return remoteViews
            }
            items[index] is Item<*> ->
                @Suppress("UNCHECKED_CAST")
                return getViewAt((items[index] as Item<T>).t)
            else -> {
                throw IllegalStateException("Unexpected ListItem Type: ${items[index]}")
            }
        }
    }

    final override fun getCount(): Int {
        return items.size
    }

    final override fun getViewTypeCount(): Int {
        return 2 /* two viewTypes: regular item & section item */
    }
}