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
        val mockInstances = listOf(
            Event.Instance(2L, 1L, 1000L),
            Event.Instance(3L, 1001L, 2000L)
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
        Mockito.`when`(mockLoader.loadEventInstances(any(Int::class.java))).thenReturn(mockInstances)

        /* instantiate class under test */
        factory = CalendarRemoteViewsFactory(context.packageName, mockLoader, 7)
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
        if (factory.getItems() != mockInstances) {
            Assert.fail("Wrong output:\n\texpected: $mockInstances\n\tactual:${factory.getItems()}")
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
        factory.setItems(mockInstances)

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
        factory.setItems(mockInstances)

        /* call method to test */
        val count = factory.count
        /* verify output */

        if (count != mockInstances.size) {
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