package com.example.mobilepint.beneficios

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.util.Locale

class AdapterBeneficios(
    private val mcontext: Context,
    private val BeneficiosList: ArrayList<ItemsBeneficios>
) :
    RecyclerView.Adapter<AdapterBeneficios.BeneficiosViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsBeneficios> = ArrayList(BeneficiosList)
    private var listaAtual: ArrayList<ItemsBeneficios> = ArrayList(BeneficiosList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_beneficios, parent, false)
        return BeneficiosViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeneficiosViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeBeneficio = currentItem.NomeBeneficio
        val subtituloBeneficio = currentItem.Subtitulo
        val enderecoImagem = currentItem.EnderecoImagem

        holder.holderNomeBeneficio.text = nomeBeneficio
        holder.holderSubtituloBeneficio.text = subtituloBeneficio
        if (enderecoImagem.isBlank()) {
            holder.holderImagemBeneficio.visibility = View.GONE
        } else {
            holder.holderImagemBeneficio.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(enderecoImagem)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions().fitCenter())
                .into(holder.holderImagemBeneficio)
        }
        holder.cardViewBeneficios.setOnClickListener {
            GlobalVariables.detalhesNumBeneficio = currentItem.NBeneficio
            GlobalVariables.detalhesNomeBeneficio = currentItem.NomeBeneficio
            GlobalVariables.detalhesSubtituloBeneficio = currentItem.Subtitulo
            GlobalVariables.detalhesDescricaoBeneficio = currentItem.Descricao
            GlobalVariables.detalhesEnderecoImagemBeneficio = currentItem.EnderecoImagem

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesBeneficio())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return listaAtual.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence.isNullOrEmpty()) {
                    listaAtual = ArrayList(listaOriginal)
                } else {
                    val filterPattern = charSequence.toString().toLowerCase(Locale.ROOT).trim()

                    listaAtual.clear()
                    for (item in listaOriginal) {
                        if (item.NomeBeneficio.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsBeneficios> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class BeneficiosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewBeneficios: CardView = itemView.findViewById(R.id.CardViewBeneficios)
        val holderNomeBeneficio: TextView = itemView.findViewById(R.id.nomeBeneficio)
        val holderSubtituloBeneficio: TextView = itemView.findViewById(R.id.subtituloBeneficio)
        val holderImagemBeneficio: ImageView = itemView.findViewById(R.id.imagemBeneficio)
    }
}