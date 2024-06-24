package cr.ac.una.wikiAPIwithLocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class DetalleArticuloDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_TITULO = "titulo"
        private const val ARG_CONTENIDO = "contenido"
        private const val ARG_IMAGEN_URL = "imagen_url"

        fun newInstance(titulo: String?, contenido: String?, imagenUrl: String?): DetalleArticuloDialogFragment {
            val fragment = DetalleArticuloDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITULO, titulo)
            args.putString(ARG_CONTENIDO, contenido)
            args.putString(ARG_IMAGEN_URL, imagenUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_articulo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titulo = arguments?.getString(ARG_TITULO) ?: ""
        val contenido = arguments?.getString(ARG_CONTENIDO) ?: ""
        val imagenUrl = arguments?.getString(ARG_IMAGEN_URL) ?: ""

        // Configurar la vista con el título y el contenido del artículo
        // Por ejemplo:
        view.findViewById<TextView>(R.id.tituloTextView).text = titulo
        view.findViewById<TextView>(R.id.contenidoTextView).text = contenido

        // Cargar la imagen del artículo
        if (imagenUrl.isNotEmpty()) {
            val imageView = view.findViewById<ImageView>(R.id.imagenImageView)
            Glide.with(this)
                .load(imagenUrl)
                .into(imageView)
        }
    }
}
