package cr.ac.una.wikiAPIwithLocation.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cr.ac.una.wikiAPIwithLocation.R
import cr.ac.una.wikiAPIwithLocation.entity.UbicacionData

class UbicacionAdapter(
    private val locations: List<UbicacionData>,
    private val onItemClick: (UbicacionData) -> Unit
) : RecyclerView.Adapter<UbicacionAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreLugarTextView: TextView = view.findViewById(R.id.placeNameTextView)
        val dateTimeTextView: TextView = view.findViewById(R.id.dateTimeTextView)
        val descripcionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
        val contadorVisitasTextView: TextView = view.findViewById(R.id.visitCountTextView)

        init {
            view.setOnClickListener {
                onItemClick(locations[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.items_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.nombreLugarTextView.text = location.nombreLugar
        holder.dateTimeTextView.text = location.dateTime

        val description = location.descripcion ?: "No hay una descripción disponible"
        holder.descripcionTextView.text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY).toString()

        holder.contadorVisitasTextView.text = "Visitas: ${location.contadorVisitas ?:0}"

        // Load thumbnail image using Glide
        if (location.thumbnail != null) {
            Glide.with(holder.itemView.context)
                .load(location.thumbnail)
                .into(holder.thumbnailImageView)
        } else {
            holder.thumbnailImageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount() = locations.size
}