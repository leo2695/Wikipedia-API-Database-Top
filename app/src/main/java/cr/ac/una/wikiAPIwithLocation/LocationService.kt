package cr.ac.una.wikiAPIwithLocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import cr.ac.una.wikiAPIwithLocation.helper.UbicacionDataBaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationService : Service() {
    companion object {
        private const val CHANNEL_ID = "locationServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private lateinit var databaseHelper: UbicacionDataBaseHelper
    private lateinit var db: SQLiteDatabase

    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(applicationContext, "AIzaSyBLiFVeg7U_Ugu5bMf7EQ_TBEfPE3vOSF4")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        databaseHelper = UbicacionDataBaseHelper(this)
        db = databaseHelper.writableDatabase

        createNotificationChannel()
        this.startForeground(1, createNotification("Service running"))

        requestLocationUpdates()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                val latitude = location.latitude
                val longitude = location.longitude

                // Check if the location has changed significantly
                if (hasLocationChanged(latitude, longitude)) {
                    lastLatitude = latitude
                    lastLongitude = longitude
                    val currentTime = getCurrentDateTime()
                    getPlaceDetails(latitude, longitude, currentTime)

                    Log.d("LocationService", "Coordenadas, latitud: $latitude, longitud: $longitude, fecha y hora: $currentTime")
                }
            }
        }
    }

    private fun hasLocationChanged(newLatitude: Double, newLongitude: Double): Boolean {
        val threshold = 0.001 // Change this value to adjust sensitivity
        if (lastLatitude == null || lastLongitude == null) {
            return true
        }
        val latDiff = Math.abs(newLatitude - lastLatitude!!)
        val lonDiff = Math.abs(newLongitude - lastLongitude!!)
        return latDiff > threshold || lonDiff > threshold
    }

    @SuppressLint("MissingPermission")
    private fun getPlaceDetails(latitude: Double, longitude: Double, currentTime: String) {
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        val placesClient: PlacesClient = Places.createClient(this)

        // Verificar permisos antes de hacer la solicitud a Places API
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    val topPlaces = response?.placeLikelihoods
                        ?.sortedByDescending { it.likelihood }
                        ?.take(1)

                    topPlaces?.forEach { placeLikelihood ->
                        val placeName = placeLikelihood.place.name ?: "Unknown"
                        Log.d("LocationService", "Lugar: $placeName, Probabilidad: ${placeLikelihood.likelihood}")

                        // Obtener el título y la descripción de Wikipedia para el lugar
                        obtenerArticuloWikipedia(latitude, longitude, currentTime, placeName)
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("LocationService", "Lugar no encontrado: ${exception.statusCode}")
                    }
                    // Si la obtención del lugar falla, guarda los datos sin el nombre del lugar
                    saveLocationData(latitude, longitude, currentTime, "Unknown", null, null, null)
                }
            }
        } else {
            Log.e("LocationService", "Permisos de ubicación no otorgados")
            // Si los permisos no están otorgados, guarda los datos sin el nombre del lugar
            saveLocationData(latitude, longitude, currentTime, "Unknown", null, null, null)
        }
    }

    private fun obtenerArticuloWikipedia(latitude: Double, longitude: Double, currentTime: String, placeName: String) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val url = "https://es.wikipedia.org/w/api.php?action=query&format=json&prop=extracts|pageimages&exintro&piprop=thumbnail&pithumbsize=200&titles=$placeName"
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("LocationService", "Failed to get Wikipedia article for $placeName")
                    saveLocationData(latitude, longitude, currentTime, placeName, null, null, null)
                    return@launch
                }

                val body = response.body?.string()
                val jsonObject = JSONObject(body)

                // Verificar si hay páginas disponibles en la respuesta
                if (jsonObject.has("query")) {
                    val queryObject = jsonObject.getJSONObject("query")
                    if (queryObject.has("pages")) {
                        val pagesObject = queryObject.getJSONObject("pages")
                        // Tomar el primer elemento (debería ser solo uno)
                        val pageId = pagesObject.keys().next()
                        val article = pagesObject.getJSONObject(pageId)

                        // Verificar si contiene el campo "extract"
                        if (article.has("extract")) {
                            val title = article.getString("title")
                            val description = article.getString("extract")
                            val thumbnailUrl = article.optJSONObject("thumbnail")?.getString("source")

                            // Mostrar la notificación personalizada con el artículo de Wikipedia
                            showNotification(title, description)
                            saveLocationData(latitude, longitude, currentTime, placeName, title, description, thumbnailUrl)
                            return@launch
                        }
                    }
                }

                // Si no se encontró extracto, manejar el caso
                Log.e("LocationService", "No se encontró información para: $placeName")
                saveLocationData(latitude, longitude, currentTime, placeName, null, null, null)
            } catch (e: IOException) {
                Log.e("LocationService", "Error al obtener artículo de Wikipedia", e)
                saveLocationData(latitude, longitude, currentTime, placeName, null, null, null)
            } catch (e: Exception) {
                Log.e("LocationService", "Error al procesar respuesta de Wikipedia", e)
                saveLocationData(latitude, longitude, currentTime, placeName, null, null, null)
            }
        }
    }

    private fun saveLocationData(latitude: Double, longitude: Double, dateTime: String, placeName: String, articleTitle: String?, description: String?, thumbnailUrl: String?) {
        val values = ContentValues().apply {
            put(UbicacionDataBaseHelper.COLUMN_LATITUD, latitude)
            put(UbicacionDataBaseHelper.COLUMN_LONGITUD, longitude)
            put(UbicacionDataBaseHelper.COLUMN_DATETIME, dateTime)
            put(UbicacionDataBaseHelper.COLUMN_NOMBRE_LUGAR, placeName)
            put(UbicacionDataBaseHelper.COLUMN_TITULO_ARTICULO_WIKI, articleTitle)
            put(UbicacionDataBaseHelper.COLUMN_DESCRIPCION, description)
            put(UbicacionDataBaseHelper.COLUMN_THUMBNAIL_URL, thumbnailUrl)
        }
        db.insert(UbicacionDataBaseHelper.TABLE_NAME, null, values)
    }
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Método para mostrar la notificación
    private fun showNotification(title: String, description: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir WebViewActivity al hacer clic en la notificación
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("description", description)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Crear y configurar la notificación
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        notificationLayout.setTextViewText(R.id.notification_text, "Artículo de Wikipedia para $title")

        // Configurar el botón "Mostrar"
        val showIntent = Intent(this, WebViewActivity::class.java)
        showIntent.putExtra("title", title)
        showIntent.putExtra("description", description)
        showIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val showPendingIntent = PendingIntent.getActivity(this, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationLayout.setOnClickPendingIntent(R.id.show_button, showPendingIntent)

        // Crear la notificación
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContent(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Mostrar la notificación
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}