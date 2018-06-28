package fr.chsfleury.flux.dto

data class FeedInput(
        var name: String? = null,
        var url: String? = null,
        var selector: String? = null,
        var prefix: String? = null,
        var suffix: String? = null
)