package fr.chsfleury.flux.dto

import fr.chsfleury.flux.domain.generated.tables.records.FeedRecord

data class FeedInput(
        val title: String,
        val url: String,
        val selector: String,
        val prefix: String,
        val suffix: String
) {
    fun toRecord(): FeedRecord {
        return FeedRecord()
                .setTitle(title)
                .setUrl(url)
                .setSelector(selector)
                .setPrefix(prefix)
                .setSuffix(suffix)
    }
}