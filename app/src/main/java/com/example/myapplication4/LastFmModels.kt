package com.example.myapplication4

data class LastFmResponse(
    val artist: ArtistInfo?
)

data class ArtistInfo(
    val name: String,
    val stats: ArtistStats
)

data class ArtistStats(
    val listeners: String,
    val playcount: String
)


//para las graficas
data class TopTracksResponse(
    val toptracks: TopTracksData?
)

data class TopTracksData(
    val track: List<TrackInfo>
)

data class TrackInfo(
    val name: String,
    val playcount: String
)


//para los generos
data class TopTagsResponse(
    val toptags: TopTagsData?
)

data class TopTagsData(
    val tag: List<TagInfo>
)

data class TagInfo(
    val name: String,
    val count: Int
)