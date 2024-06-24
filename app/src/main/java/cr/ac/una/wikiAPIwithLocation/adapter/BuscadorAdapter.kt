package cr.ac.una.wikiAPIwithLocation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import cr.ac.una.wikiAPIwithLocation.R
import cr.ac.una.wikiAPIwithLocation.clases.Page

class BuscadorAdapter(
    context: Context,
    pages: List<Page>,
    private val listener: OnItemClickListener
) : ArrayAdapter<Page>(context, 0, pages) {

    interface OnItemClickListener {
        fun onItemClick(page: Page)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_busqueda, parent, false)

        val title = view.findViewById<TextView>(R.id.titleView)
        val extract = view.findViewById<TextView>(R.id.extractView)
        val imageView = view.findViewById<ImageView>(R.id.image_view)

        val pageItem = getItem(position)

        title.text = pageItem?.title ?: "Sin título"
        extract.text = pageItem?.description ?: "Sin descripción"

        pageItem?.thumbnail?.url?.let { url ->
            Glide.with(context)
                .load("https:$url")  // Añadimos "https:" porque la URL de la imagen es relativa
                .into(imageView)
        } ?: imageView.setImageResource(R.drawable.placeholder)  // Placeholder en caso de no haber imagen

        view.setOnClickListener {
            pageItem?.let { item ->
                listener.onItemClick(item)
            }
        }

        return view
    }
}
