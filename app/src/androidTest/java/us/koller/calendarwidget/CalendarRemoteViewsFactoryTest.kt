package us.koller.calendarwidget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * test class for CalendarRemoteViewsFactory
 * */
@RunWith(MockitoJUnitRunner::class)
class CalendarRemoteViewsFactoryTest {

    companion object {
        /* mock input-data */
        val mockEvents = listOf(
            Event(
                1L, "title1", 16777215 /* = #FFFFFF */, "description1",
                "location1", 0L, 1L, 1000L, false
            ),
            Event(
                2L, "title2", 16777215 /* = #FFFFFF */, "description2",
                "location2", 0L, 1000L, 1000L, true
            )
        )
    }

    /* mocks */
    @Mock
    private lateinit var mockLoader: CalendarLoader

    /* class under test */
    private lateinit var factory: CalendarRemoteViewsFactory

    /**
     * setup mocks and class under test
     * */
    @Before
    fun setUp() {
        /* create mocks */
        val context = ApplicationProvider.getApplicationContext<Context>()

        /* populate mocks */
        Mockito.`when`(mockLoader.loadEvents(any(Int::class.java))).thenReturn(mockEvents)

        /* instantiate class under test */
        factory = CalendarRemoteViewsFactory(context.packageName, mockLoader)
    }

    /**
     * test CalendarRemoteViewsFactory.onCreate()
     * */
    @Test
    fun testOnCreate() {
        /* onCreate() empty => nothing to test */
    }

    /**
     * test CalendarRemoteViewsFactory.getLoadingView()
     * */
    @Test
    fun testGetLoadingView() {
        /* no real meaningful tests possible */
    }

    /**
     * test CalendarRemoteViewsFactory.getItemId()
     * */
    @Test
    fun testGetItemId() {
        /* no real meaningful tests possible */
    }

    /**
     * test CalendarRemoteViewsFactory.onDataSetChanged()
     * */
    @Test
    fun testOnDataSetChanged() {
        /* call method to test */
        factory.onDataSetChanged()

        /* verify output */
        if (factory.getItems() != mockEvents) {
            Assert.fail("Wrong output:\n\texpected: $mockEvents\n\tactual:${factory.getItems()}")
        }
    }

    /**
     * test CalendarRemoteViewsFactory.hasStableIds()
     * */
    @Test
    fun testHasStableIds() {
        /* no real meaningful tests possible */
    }

    /**
     * test CalendarRemoteViewsFactory.getViewAt()
     * */
    @Test
    fun testGetViewAt() {
        /* set data */
        factory.setItems(mockEvents)

        /* call method to test */
        val remoteViews = factory.getViewAt(1)

        /* verify output */
        Assert.assertNotNull(remoteViews)
        /* sadly remoteViews doesn't provide interface to check properties */
    }

    /**
     * test CalendarRemoteViewsFactory.getCount()
     * */
    @Test
    fun testGetCount() {
        /* set data */
        factory.setItems(mockEvents)

        /* call method to test */
        val count = factory.count
        /* verify output */

        if (count != 2) {
            Assert.fail("Wrong output:\n\texpected: 2\n\tactual:$count")
        }
    }

    /**
     * test CalendarRemoteViewsFactory.getViewTypeCount()
     * */
    @Test
    fun testGetViewTypeCount() {
        /* no real meaningful tests possible */
    }

    /**
     * test CalendarRemoteViewsFactory.onDestroy()
     * */
    @Test
    fun testOnDestroy() {
        /* onDestroy() empty => nothing to test */
    }
}