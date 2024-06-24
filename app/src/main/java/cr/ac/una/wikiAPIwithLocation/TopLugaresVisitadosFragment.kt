package cr.ac.una.wikiAPIwithLocation

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cr.ac.una.wikiAPIwithLocation.adapter.UbicacionAdapter
import cr.ac.una.wikiAPIwithLocation.entity.UbicacionData
import cr.ac.una.wikiAPIwithLocation.helper.UbicacionDataBaseHelper

class TopLugaresVisitadosFragment : Fragment() {

    private lateinit var databaseHelper: UbicacionDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UbicacionAdapter
    private val locations = mutableListOf<UbicacionData>()
    private var limit = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_lugares_visitados, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UbicacionAdapter(locations) { location ->
            showLocationDetails(location)
        }
        recyclerView.adapter = adapter

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseHelper = UbicacionDataBaseHelper(context)
        db = databaseHelper.readableDatabase
    }

    override fun onResume() {
        super.onResume()
        obtenerCantidadMaximaDB()
        mostrarTopLugaresVisitados()
    }

    private fun obtenerCantidadMaximaDB() {
        val cursor = db.rawQuery("SELECT ${UbicacionDataBaseHelper.COLUMN_CANTIDAD_MAXIMA} FROM ${UbicacionDataBaseHelper.TABLE_CANTIDAD}", null)
        if (cursor.moveToFirst()) {
            limit = cursor.getInt(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_CANTIDAD_MAXIMA))
        }
        cursor.close()
    }

    private fun mostrarTopLugaresVisitados() {
        locations.clear()
        val cursor: Cursor = db.query(
            UbicacionDataBaseHelper.TABLE_CANTIDAD,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            val limit = cursor.getInt(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_CANTIDAD_MAXIMA))
            val topVisitedCursor = databaseHelper.obtenerUbicacionUnica(db, limit)

            while (topVisitedCursor.moveToNext()) {
                val placeName = topVisitedCursor.getString(topVisitedCursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_NOMBRE_LUGAR))
                val dateTime = topVisitedCursor.getString(topVisitedCursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_DATETIME))
                val articleTitle = topVisitedCursor.getString(topVisitedCursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_TITULO_ARTICULO_WIKI))
                val description = topVisitedCursor.getString(topVisitedCursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_DESCRIPCION))
                val thumbnailUrl = topVisitedCursor.getString(topVisitedCursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_THUMBNAIL_URL))
                val contadorVisitas = topVisitedCursor.getInt(topVisitedCursor.getColumnIndexOrThrow("contadorVisitas"))

                val ubicacionData = UbicacionData(0.0, 0.0, dateTime, placeName, articleTitle, description, thumbnailUrl, contadorVisitas)
                locations.add(ubicacionData)
            }
            topVisitedCursor.close()
        }

        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showLocationDetails(location: UbicacionData) {
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("title", location.tituloArticuloWiki)
            putExtra("description", location.descripcion)
        }
        startActivity(intent)
    }
}
