package com.example.myapplication4

sealed class Destination(val route: String, val icon: Int, val title: String) {
    object Compare : Destination(route = "Compare", icon = R.drawable.icon_ecra01, title = "Compare")
    object History : Destination(route = "History", icon = R.drawable.icon_ecra02, title = "History")
    object Settings : Destination(route = "Settings", icon = R.drawable.icon_ecra03, title = "Settings")
    object ListAll : Destination(route = "List", icon = R.drawable.icon_ecra04, title = "List")
    companion object {
        val toList = listOf(Compare, History, Settings, ListAll)
    }
}