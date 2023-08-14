package com.example.mobilepint.oportunidades

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
import java.util.Locale

class AdapterOportunidades(
    private val mcontext: Context,
    private val OportunidadesList: ArrayList<ItemsOportunidades>
) :
    RecyclerView.Adapter<AdapterOportunidades.OportunidadesViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsOportunidades> = ArrayList(OportunidadesList)
    private var listaAtual: ArrayList<ItemsOportunidades> = ArrayList(OportunidadesList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OportunidadesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_oportunidades, parent, false)
        return OportunidadesViewHolder(view)
    }

    override fun onBindViewHolder(holder: OportunidadesViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val tituloOportunidade = currentItem.Titulo
        val estagioOportunidade = currentItem.NomeEstagio
        val nomeClienteOportunidade = currentItem.NomeCliente

        holder.holderTituloOportunidade.text = tituloOportunidade
        holder.holderEstagioOportunidade.text = estagioOportunidade
        holder.holderClienteOportunidade.text = nomeClienteOportunidade
        holder.cardViewOportunidades.setOnClickListener {
            GlobalVariables.detalhesNumOportunidade = currentItem.NOportunidade
            GlobalVariables.detalhesTituloOportunidade = currentItem.Titulo
            GlobalVariables.detalhesValorOportunidade = currentItem.Valor
            GlobalVariables.detalhesDescricaoOportunidade = currentItem.Descricao
            GlobalVariables.detalhesDataCriacaoOportunidade = currentItem.DataHoraCriacao
            GlobalVariables.detalhesNumEtiquetaOportunidade = currentItem.NEtiqueta
            GlobalVariables.detalhesNomeEtiquetaOportunidade = currentItem.NomeEtiqueta
            GlobalVariables.detalhesNumTipoProjetoOportunidade = currentItem.NTipoProjeto
            GlobalVariables.detalhesNomeTipoProjetoOportunidade = currentItem.TipoProjeto
            GlobalVariables.detalhesNumEstagioOportunidade = currentItem.NEstagio
            GlobalVariables.detalhesNomeEstagioOportunidade = currentItem.NomeEstagio
            GlobalVariables.detalhesNumUsuarioCriadorOportunidade = currentItem.NUsuario
            GlobalVariables.detalhesNomeUsuarioCriadorOportunidade = currentItem.NomeUsuarioCriador
            GlobalVariables.detalhesNumCliente = currentItem.NCliente
            GlobalVariables.detalhesNomeCliente = currentItem.NomeCliente
            GlobalVariables.detalhesTelefoneCliente = currentItem.TelefoneCliente
            GlobalVariables.detalhesEmailCliente = currentItem.EmailCliente

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesOportunidade())
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
                listaAtual = results?.values as? ArrayList<ItemsOportunidades> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class OportunidadesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewOportunidades: CardView = itemView.findViewById(R.id.CardViewOportunidades)
        val holderTituloOportunidade: TextView = itemView.findViewById(R.id.tituloOportunidade)
        val holderEstagioOportunidade: TextView = itemView.findViewById(R.id.estagioOportunidade)
        val holderClienteOportunidade: TextView = itemView.findViewById(R.id.clienteOportunidade)
    }
}