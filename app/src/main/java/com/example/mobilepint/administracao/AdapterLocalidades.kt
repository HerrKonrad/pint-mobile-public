package com.example.mobilepint.administracao

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.beneficios.FragmentCriarBeneficio
import java.util.Locale

class AdapterLocalidades(
    private val mcontext: Context,
    private val LocalidadesList: ArrayList<ItemsLocalidades>,
) :
    RecyclerView.Adapter<AdapterLocalidades.LocalidadesViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsLocalidades> = ArrayList(LocalidadesList)
    private var listaAtual: ArrayList<ItemsLocalidades> = ArrayList(LocalidadesList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalidadesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_localidades, parent, false)
        return LocalidadesViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalidadesViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val idLocalidade = currentItem.NLocalidade
        val nomeLocalidade = currentItem.Localidade

        val detalhes = "<b>Localidade:</b><br>" +
                "- Nome: $nomeLocalidade"

        holder.holderNomeLocalidade.text = nomeLocalidade
        holder.cardViewLocalidades.setOnClickListener {
            val builder = AlertDialog.Builder(mcontext)
            builder.setTitle("Detalhes")
            builder.setMessage(Html.fromHtml(detalhes))
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton("Editar") { dialog, _ ->
                dialog.dismiss()
                GlobalVariables.criarLocalidade = false

                val bundle = Bundle()
                bundle.putInt("NumLocalidade", idLocalidade)
                bundle.putString("NomeLocalidade", nomeLocalidade)

                val fragment = FragmentCriarLocalidadeAdmin()
                fragment.arguments = bundle

                val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
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
                        if (item.Localidade.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsLocalidades> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class LocalidadesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewLocalidades: CardView = itemView.findViewById(R.id.CardViewLocalidades)
        val holderNomeLocalidade: TextView = itemView.findViewById(R.id.nomeLocalidade)
    }
}