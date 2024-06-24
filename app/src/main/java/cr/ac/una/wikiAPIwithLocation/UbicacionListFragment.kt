package cr.ac.una.wikiAPIwithLocation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.database.sqlite.SQLiteDatabase
import cr.ac.una.wikiAPIwithLocation.adapter.UbicacionAdapter
import cr.ac.una.wikiAPIwithLocation.entity.UbicacionData
import cr.ac.una.wikiAPIwithLocation.helper.UbicacionDataBaseHelper

class UbicacionListFragment : Fragment() {

    private lateinit var databaseHelper: UbicacionDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UbicacionAdapter
    private val locations = mutableListOf<UbicacionData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ubicaciones_list, container, false)

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
        loadLocationsFromDatabase()
    }

    private fun loadLocationsFromDatabase() {
        locations.clear()
        val cursor = databaseHelper.obtenerUbicacionUnica(db, limit = 100)

        while (cursor.moveToNext()) {
            val placeName = cursor.getString(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_NOMBRE_LUGAR))
            val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_DATETIME))
            val articleTitle = cursor.getString(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_TITULO_ARTICULO_WIKI))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_DESCRIPCION))
            val thumbnailUrl = cursor.getString(cursor.getColumnIndexOrThrow(UbicacionDataBaseHelper.COLUMN_THUMBNAIL_URL))
            val contadorVisitas = cursor.getInt(cursor.getColumnIndexOrThrow("contadorVisitas"))

            val ubicacionData = UbicacionData(
                latitud = null,
                longitud = null,
                dateTime = dateTime,
                nombreLugar = placeName,
                tituloArticuloWiki = articleTitle,
                descripcion = description,
                thumbnail = thumbnailUrl,
                contadorVisitas = contadorVisitas
            )
            locations.add(ubicacionData)
        }

        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showLocationDetails(location: UbicacionData) {
        //Log.d("LocationListFragment", "Location clicked: ${location.nombreLugar}")
        val intent = Intent(context, WebViewActivity::class.java).apply {
        putExtra("title", location.tituloArticuloWiki)
        putExtra("description", location.descripcion)
    }
    startActivity(intent)
}
}