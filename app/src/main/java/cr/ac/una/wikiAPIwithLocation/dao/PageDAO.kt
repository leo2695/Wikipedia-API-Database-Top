package cr.ac.una.wikiAPIwithLocation.dao

import cr.ac.una.wikiAPIwithLocation.clases.pages
import retrofit2.http.GET
import retrofit2.http.Query

interface PageDAO {
        @GET("search/page")
        suspend fun Buscar(
                @Query("q") query: String,
                @Query("limit") limit: Int
        ): pages
}