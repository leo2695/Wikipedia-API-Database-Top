package cr.ac.una.wikiAPIwithLocation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import cr.ac.una.wikiAPIwithLocation.helper.UbicacionDataBaseHelper
import android.database.sqlite.SQLiteDatabase

class CantidadMaximaFragment : Fragment() {

    private lateinit var databaseHelper: UbicacionDataBaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var limitEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cantidad_maxima, container, false)

        limitEditText = view.findViewById(R.id.cantidadMaximaEditText)
        saveButton = view.findViewById(R.id.botonGuardar)

        saveButton.setOnClickListener {
            saveLimit()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseHelper = UbicacionDataBaseHelper(context)
        db = databaseHelper.writableDatabase
    }

    private fun saveLimit() {
        val numeroMaximo = limitEditText.text.toString().toIntOrNull()
        if (numeroMaximo != null && numeroMaximo > 0) {
            db.execSQL("DELETE FROM ${UbicacionDataBaseHelper.TABLE_CANTIDAD}") // Limpiar tabla
            db.execSQL("INSERT INTO ${UbicacionDataBaseHelper.TABLE_CANTIDAD} (${UbicacionDataBaseHelper.COLUMN_CANTIDAD_MAXIMA}) VALUES ($numeroMaximo)")
            Toast.makeText(context, "Mostrar: $numeroMaximo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Ingrese un dato válido", Toast.LENGTH_SHORT).show()
        }
    }
}
