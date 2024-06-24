package cr.ac.una.wikiAPIwithLocation.clases

import java.io.Serializable

data class Page(
    val id: Int,
    val key: String?,
    val title: String?,
    val excerpt: String?,
    val description: String?,
    val thumbnail: Thumbnail?
): Serializable
