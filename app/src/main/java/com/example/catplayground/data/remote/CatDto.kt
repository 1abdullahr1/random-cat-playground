package com.example.catplayground.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatDto(
    @SerialName("_id") val underscoreId: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("mimetype") val mimetype: String? = null,
    @SerialName("tags") val tags: List<String> = emptyList()
) {
    val finalId: String
        get() = underscoreId ?: id ?: ""
}
