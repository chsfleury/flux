package fr.chsfleury.flux.dto

data class FeedInput(
        var title: String? = null,
        var url: String? = null,
        var selector: String? = null,
        var prefix: String? = null,
        var suffix: String? = null
)