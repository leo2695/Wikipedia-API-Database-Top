package cr.ac.una.wikiAPIwithLocation.clases

import java.io.Serializable

data class Thumbnail(
    val url: String?,
    val width: Int?,
    val height: Int?
): Serializable