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

class AdapterTopicosIdeiasAdmin(
    private val mcontext: Context,
    private val TopicosIdeiasList: ArrayList<ItemsTopicosIdeiasAdmin>,
) :
    RecyclerView.Adapter<AdapterTopicosIdeiasAdmin.TopicosIdeiasViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsTopicosIdeiasAdmin> = ArrayList(TopicosIdeiasList)
    private var listaAtual: ArrayList<ItemsTopicosIdeiasAdmin> = ArrayList(TopicosIdeiasList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicosIdeiasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_topicos_ideias_admin, parent, false)
        return TopicosIdeiasViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicosIdeiasViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val idTopico = currentItem.NTopicoIdeia
        val nomeTopico = currentItem.NomeTopico

        val detalhes = "<b>Tópico das ideias:</b><br>" +
                "- Nome: $nomeTopico"

        holder.holderNomeTopico.text = nomeTopico
        holder.cardViewTopicosIdeias.setOnClickListener {
            val builder = AlertDialog.Builder(mcontext)
            builder.setTitle("Detalhes")
            builder.setMessage(Html.fromHtml(detalhes))
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton("Editar") { dialog, _ ->
                dialog.dismiss()
                GlobalVariables.criarTopicoIdeias = false

                val bundle = Bundle()
                bundle.putInt("NTopicoIdeia", idTopico)
                bundle.putString("NomeTopico", nomeTopico)

                val fragment = FragmentCriarTopicoIdeiasAdmin()
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
                        if (item.NomeTopico.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            listaAtual.add(item)
                        }
                    }
                }
                results.values = listaAtual
                results.count = listaAtual.size
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                listaAtual = results?.values as? ArrayList<ItemsTopicosIdeiasAdmin> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class TopicosIdeiasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewTopicosIdeias: CardView = itemView.findViewById(R.id.CardViewTopicosIdeias)
        val holderNomeTopico: TextView = itemView.findViewById(R.id.nomeTopico)
    }
}