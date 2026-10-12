package com.example.catplayground

import com.example.catplayground.ui.navigation.NavScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavScreenTest {

    @Test
    fun testBottomNavItemsAreNotNullAndInitialized() {
        val items = NavScreen.bottomNavItems
        assertNotNull(items)
        assertEquals(3, items.size)
        for (screen in items) {
            assertNotNull("NavScreen item must not be null", screen)
            assertTrue("Route must not be blank", screen.route.isNotBlank())
            assertTrue("Title must not be blank", screen.title.isNotBlank())
            assertNotNull("Icon must not be null", screen.icon)
        }
    }
}
