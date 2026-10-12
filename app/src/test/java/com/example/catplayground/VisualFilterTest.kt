package com.example.catplayground

import com.example.catplayground.domain.model.VisualFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VisualFilterTest {

    @Test
    fun testAllFiltersHaveValidColorMatrix() {
        for (filter in VisualFilter.entries) {
            val matrix = filter.getColorMatrix()
            assertNotNull(matrix)
            assertEquals("Matrix for ${filter.name} must have 20 elements (4x5)", 20, matrix.size)
        }
    }

    @Test
    fun testOriginalFilterIsIdentity() {
        val identity = VisualFilter.ORIGINAL.getColorMatrix()
        // Diagonal elements should be 1.0f
        assertEquals(1f, identity[0], 0.001f) // R -> R
        assertEquals(1f, identity[6], 0.001f) // G -> G
        assertEquals(1f, identity[12], 0.001f) // B -> B
        assertEquals(1f, identity[18], 0.001f) // A -> A
    }
}
