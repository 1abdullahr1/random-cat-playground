package com.example.catplayground

import com.example.catplayground.data.remote.CatDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun testCatDtoWithUnderscoreId() {
        val jsonString = """{"_id":"abc12345","mimetype":"image/jpeg","tags":["cute","orange"]}"""
        val dto = json.decodeFromString<CatDto>(jsonString)
        assertEquals("abc12345", dto.finalId)
        assertEquals("image/jpeg", dto.mimetype)
        assertEquals(2, dto.tags.size)
        assertTrue(dto.tags.contains("cute"))
        assertTrue(dto.tags.contains("orange"))
    }

    @Test
    fun testCatDtoWithRegularId() {
        val jsonString = """{"id":"xyz999","mimetype":"image/gif","tags":["sleepy"]}"""
        val dto = json.decodeFromString<CatDto>(jsonString)
        assertEquals("xyz999", dto.finalId)
        assertEquals("image/gif", dto.mimetype)
        assertEquals(1, dto.tags.size)
    }

    @Test
    fun testCatDtoWithBothPrefersUnderscore() {
        val jsonString = """{"_id":"preferred","_id_old":"old","id":"secondary"}"""
        val dto = json.decodeFromString<CatDto>(jsonString)
        assertEquals("preferred", dto.finalId)
    }
}
