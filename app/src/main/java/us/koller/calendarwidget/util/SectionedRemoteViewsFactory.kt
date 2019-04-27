package us.koller.calendarwidget.util

import android.content.Intent
import android.widget.RemoteViews
import us.koller.calendarwidget.R

/**
 * RemoteViewsFactory that adds the ability to add sections between items.
 * Extends FooterRemoteViewsFactory to also have the ability to add a footer.
 * */
abstract class SectionedRemoteViewsFactory<T>(var packageName: String) :
    FooterRemoteViewsFactory() {

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
    class Section(val title: String, val fillInIntent: Intent? = null) : ListItem()

    /* items that are displayed in the list view */
    private var items: MutableList<ListItem> = mutableListOf()
    private var sections: MutableList<Pair<Int, Section>> = mutableListOf()

    /**
     * set the items that should be displayed in the ListView. Removes all previously set sections
     * @param items
     * */
    fun setItems(items: List<T>) {
        this.items = items.map { Item(it) }.toMutableList()
        this.sections = mutableListOf()
    }

    /**
     * return the items from the ListView, without sections.
     * @return items
     * */
    fun getItems(): List<T> {
        @Suppress("UNCHECKED_CAST")
        return items.filter { it is Item<*> }.map { (it as Item<T>).t }
    }

    /**
     * add a section to the ListView
     * @param index
     * @param title
     * */
    fun addSection(index: Int, title: String, fillInIntent: Intent? = null) {
        val section = Section(title, fillInIntent)
        /* calculate the index in items */
        val sectionIndex = sections
            .filter { it.first <= index }
            .count() + index
        /* add section to items */
        items.add(sectionIndex, section)
        /* store section */
        sections.add(Pair(index, section))
    }

    /**
     * provide itemId for item
     * @param item
     * */
    abstract fun getItemId(item: T): Long

    /**
     * provide RemoteView for Item
     * @param item
     * */
    abstract fun getViewAt(item: T): RemoteViews

    /* overridden methods from FooterRemoteViewsFactory */

    final override fun getItemViewId(index: Int): Long {
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

    abstract fun isThemeLight(): Boolean

    final override fun getItemViewAt(index: Int): RemoteViews {
        when {
            items[index] is Section -> {
                val section: Section? = items[index] as? Section
                /* create section remoteView */
                val layout = if (isThemeLight()) R.layout.section_item_view_light else R.layout.section_item_view
                val views = RemoteViews(packageName, layout)
                /* bind Data */
                /* set section title */
                views.setTextViewText(R.id.section_title, section?.title)
                /* set the fill-intent */
                views.setOnClickFillInIntent(R.id.section_item, section?.fillInIntent)
                /* return section remoteViews */
                return views
            }
            items[index] is Item<*> ->
                @Suppress("UNCHECKED_CAST")
                return getViewAt((items[index] as Item<T>).t)
            else -> {
                throw IllegalStateException("Unexpected ListItem Type: ${items[index]}")
            }
        }
    }

    final override fun getItemCount(): Int {
        return items.size
    }

    final override fun getItemViewTypeCount(): Int {
        return 2 * 2 /* two viewTypes: regular item & section item; times 2: for light & dark theme */
    }
}