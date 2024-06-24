package cr.ac.una.wikiAPIwithLocation.controller

import cr.ac.una.wikiAPIwithLocation.clases.Page
import cr.ac.una.wikiAPIwithLocation.service.PagesService

class PageController {
    private val pagesService = PagesService()

    suspend fun Buscar(terminoBusqueda: String, limit: Int): List<Page> {
        return pagesService.apiWikiService.Buscar(terminoBusqueda, limit).pages ?: emptyList()
    }
}