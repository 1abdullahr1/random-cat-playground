package com.example.catplayground

import com.example.catplayground.domain.model.CaptionColor
import com.example.catplayground.domain.model.CaptionPosition
import com.example.catplayground.domain.model.CaptionPresets
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptionPresetsTest {

    @Test
    fun testPresetListIsNotEmpty() {
        assertTrue(CaptionPresets.presets.isNotEmpty())
        for (preset in CaptionPresets.presets) {
            assertTrue(preset.isNotBlank())
        }
    }

    @Test
    fun testRandomPresetReturnsValidString() {
        val randomPreset = CaptionPresets.getRandomPreset()
        assertTrue(randomPreset.isNotBlank())
        assertTrue(CaptionPresets.presets.contains(randomPreset))
    }

    @Test
    fun testCaptionColorsHaveValidHex() {
        for (color in CaptionColor.entries) {
            assertFalse(color.displayName.isBlank())
            assertTrue(color.hexColor != 0L)
        }
    }

    @Test
    fun testCaptionPositionsCoverAll() {
        assertTrue(CaptionPosition.entries.contains(CaptionPosition.TOP))
        assertTrue(CaptionPosition.entries.contains(CaptionPosition.CENTER))
        assertTrue(CaptionPosition.entries.contains(CaptionPosition.BOTTOM))
    }
}
