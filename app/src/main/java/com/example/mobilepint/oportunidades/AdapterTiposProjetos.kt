package com.example.mobilepint.oportunidades

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.util.Locale

class AdapterTiposProjetos(
    private val mcontext: Context,
    private val TiposProjetosList: ArrayList<ItemsTiposProjetos>
) :
    RecyclerView.Adapter<AdapterTiposProjetos.TiposProjetosViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsTiposProjetos> = ArrayList(TiposProjetosList)
    private var listaAtual: ArrayList<ItemsTiposProjetos> = ArrayList(TiposProjetosList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiposProjetosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_tipos_projetos, parent, false)
        return TiposProjetosViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiposProjetosViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val nomeTipoProjeto = currentItem.Nome
        val numCargo = GlobalVariables.idCargoUtilizadorAutenticado
        val detalhes = "<b>Tipo de projeto:</b><br>- Nome: $nomeTipoProjeto"

        holder.holderNomeTipoProjeto.text = nomeTipoProjeto
        holder.cardViewTiposProjetos.setOnClickListener {
            val builder = AlertDialog.Builder(mcontext)
            builder.setTitle("Detalhes")
            builder.setMessage(Html.fromHtml(detalhes))
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo) {
                builder.setNegativeButton("Editar") { dialog, _ ->
                    dialog.dismiss()
                    GlobalVariables.criarTipoProjeto = false

                    val bundleTipoProjeto = Bundle()
                    bundleTipoProjeto.putInt("NumTipoProjeto", currentItem.NTipoProjeto)
                    bundleTipoProjeto.putString("NomeTipoProjeto", currentItem.Nome)

                    val fragment = FragmentCriarTipoProjeto()
                    fragment.arguments = bundleTipoProjeto

                    val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
            builder.show()
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
                        if (item.Nome.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsTiposProjetos> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class TiposProjetosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewTiposProjetos: CardView = itemView.findViewById(R.id.CardViewTiposProjetos)
        val holderNomeTipoProjeto: TextView = itemView.findViewById(R.id.nomeTipoProjeto)
    }
}