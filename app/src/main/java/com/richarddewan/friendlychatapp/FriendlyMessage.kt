package com.richarddewan.friendlychatapp

data class FriendlyMessage(
    var text: String = "",
    val name: String? = null,
    val photoUrl: String? = null
) {
}