package com.example.catplayground.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CataasApi {

    @GET("cat?json=true")
    suspend fun getRandomCat(): CatDto

    @GET("cat/gif?json=true")
    suspend fun getRandomGif(): CatDto

    @GET("cat/{tag}?json=true")
    suspend fun getCatByTag(@Path("tag") tag: String): CatDto

    @GET("api/tags")
    suspend fun getTags(): List<String>

    @GET("api/cats")
    suspend fun getCatalog(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null
    ): List<CatDto>
}
