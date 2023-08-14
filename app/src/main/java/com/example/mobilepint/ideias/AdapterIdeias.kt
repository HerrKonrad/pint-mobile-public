package com.example.mobilepint.ideias

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdapterIdeias(
    private val mcontext: Context,
    private val IdeiasList: ArrayList<ItemsIdeias>
) :
    RecyclerView.Adapter<AdapterIdeias.IdeiasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsIdeias> = ArrayList(IdeiasList)
    private var listaAtual: ArrayList<ItemsIdeias> = ArrayList(IdeiasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeiasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_ideias, parent, false)
        return IdeiasViewHolder(view)
    }

    override fun onBindViewHolder(holder: IdeiasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val tituloIdeia = currentItem.Titulo
        val utilizadorIdeia = currentItem.NomeUsuario
        var dataCriacao = currentItem.Data

        if (dataCriacao.isNotBlank()) {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(dataCriacao, inputFormatter)
            dataCriacao = dateTime.format(outputFormatter)
        }

        holder.holderTituloIdeia.text = tituloIdeia
        holder.holderDataIdeia.text = dataCriacao
        holder.holderUtilizadorIdeia.text = utilizadorIdeia
        holder.cardViewIdeias.setOnClickListener {
            GlobalVariables.detalhesNumIdeia = currentItem.NIdeia
            GlobalVariables.detalhesNumUsuarioIdeia = currentItem.NUsuario
            GlobalVariables.detalhesTituloIdeia = currentItem.Titulo
            GlobalVariables.detalhesDataIdeia = currentItem.Data
            GlobalVariables.detalhesEstadoIdeia = currentItem.Estado
            GlobalVariables.detalhesDescricaoIdeia = currentItem.Descricao
            GlobalVariables.detalhesNomeUsuarioIdeia = currentItem.NomeUsuario

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesIdeia())
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
                        if (item.Titulo.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsIdeias> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class IdeiasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewIdeias: CardView = itemView.findViewById(R.id.CardViewIdeias)
        val holderTituloIdeia: TextView = itemView.findViewById(R.id.tituloIdeia)
        val holderDataIdeia: TextView = itemView.findViewById(R.id.dataIdeia)
        val holderUtilizadorIdeia: TextView = itemView.findViewById(R.id.utilizadorIdeia)
    }
}