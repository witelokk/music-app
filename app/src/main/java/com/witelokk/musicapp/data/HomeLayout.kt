package com.witelokk.musicapp.data

data class HomeLayout(
    val playlists: List<Entity>,
    val sections: List<Pair<String, List<Entity>>>,
)