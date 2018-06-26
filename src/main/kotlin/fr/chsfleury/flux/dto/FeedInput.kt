package fr.chsfleury.flux.dto

data class FeedInput(
        val title: String,
        val url: String,
        val selector: String,
        val prefix: String,
        val suffix: String
)