package cr.ac.una.wikiAPIwithLocation.helper

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UbicacionDataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lugaresData.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "ubicaciones"
        const val COLUMN_ID = "id"
        const val COLUMN_LATITUD = "latitud"
        const val COLUMN_LONGITUD = "longitud"
        const val COLUMN_DATETIME = "dateTime"
        const val COLUMN_NOMBRE_LUGAR = "nombreLugar"
        const val COLUMN_TITULO_ARTICULO_WIKI = "tituloArticuloWiki"
        const val COLUMN_DESCRIPCION = "descripcion"
        const val COLUMN_THUMBNAIL_URL = "thumbnailUrl"

        const val TABLE_CANTIDAD = "table_cantidad"
        const val COLUMN_CANTIDAD_MAXIMA = "cantidad_maxima"

        private const val TABLE_CREATE =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_LATITUD REAL, " +
                    "$COLUMN_LONGITUD REAL, " +
                    "$COLUMN_DATETIME TEXT, " +
                    "$COLUMN_NOMBRE_LUGAR TEXT, " +
                    "$COLUMN_TITULO_ARTICULO_WIKI TEXT, " +
                    "$COLUMN_DESCRIPCION TEXT, " +
                    "$COLUMN_THUMBNAIL_URL TEXT" +
                    ")"

        private const val TABLE_LIMITE_CREATE =
            "CREATE TABLE $TABLE_CANTIDAD (" +
                    "$COLUMN_CANTIDAD_MAXIMA INTEGER PRIMARY KEY" +
                    ")"
    }

    fun obtenerUbicacionUnica(db: SQLiteDatabase, limit: Int): Cursor {
        val query = """
        SELECT $COLUMN_NOMBRE_LUGAR, $COLUMN_DATETIME, $COLUMN_TITULO_ARTICULO_WIKI, $COLUMN_DESCRIPCION, $COLUMN_THUMBNAIL_URL, COUNT(*) as contadorVisitas
        FROM $TABLE_NAME
        GROUP BY $COLUMN_NOMBRE_LUGAR
        ORDER BY contadorVisitas DESC
        LIMIT $limit
    """
        return db.rawQuery(query, null)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
        db.execSQL(TABLE_LIMITE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CANTIDAD")
        onCreate(db)
    }
}
