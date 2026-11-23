package com.example.myapplication4

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApi {
    @GET("2.0/")
    suspend fun getArtistInfo(
        @Query("artist") artistName: String,
        @Query("method") method: String = "artist.getInfo",
        @Query("api_key") apiKey: String = "4cbf91e73f79688429f475bda1ab71b6",
        @Query("format") format: String = "json"
    ): LastFmResponse


    //para las graficas
    @GET("2.0/")
    suspend fun getTopTracks(
        @Query("artist") artistName: String,
        @Query("method") method: String = "artist.getTopTracks",
        @Query("limit") limit: Int = 5,
        @Query("api_key") apiKey: String = "4cbf91e73f79688429f475bda1ab71b6",
        @Query("format") format: String = "json"
    ): TopTracksResponse

    //para los albums
    @GET("2.0/")
    suspend fun getTopAlbums(
        @Query("artist") artistName: String,
        @Query("method") method: String = "artist.getTopAlbums",
        @Query("limit") limit: Int = 5,
        @Query("api_key") apiKey: String = "4cbf91e73f79688429f475bda1ab71b6",
        @Query("format") format: String = "json"
    ): TopAlbumsResponse

    //para los generos
    @GET("2.0/")
    suspend fun getTopTags(
        @Query("artist") artistName: String,
        @Query("method") method: String = "artist.getTopTags",
        @Query("api_key") apiKey: String = "4cbf91e73f79688429f475bda1ab71b6",
        @Query("format") format: String = "json"
    ): TopTagsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://ws.audioscrobbler.com/"

    val api: LastFmApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LastFmApi::class.java)
    }
}

