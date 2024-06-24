package cr.ac.una.wikiAPIwithLocation.entity

data class UbicacionData(
    val latitud: Double? = null,
    val longitud: Double? = null,
    val dateTime: String,
    val nombreLugar: String,
    val tituloArticuloWiki: String?,
    val descripcion: String?,
    val thumbnail: String?,
    val contadorVisitas: Int
)
