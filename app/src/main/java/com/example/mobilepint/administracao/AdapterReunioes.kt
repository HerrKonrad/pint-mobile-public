package com.example.mobilepint.administracao

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
import java.time.format.DateTimeParseException
import java.util.Locale

class AdapterReunioes(
    private val mcontext: Context,
    private val ReunioesList: ArrayList<ItemsReunioes>
) :
    RecyclerView.Adapter<AdapterReunioes.ReunioesViewHolder>(),
    Filterable {

    private var listaOriginal: ArrayList<ItemsReunioes> = ArrayList(ReunioesList)
    private var listaAtual: ArrayList<ItemsReunioes> = ArrayList(ReunioesList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReunioesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_reunioes, parent, false)
        return ReunioesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReunioesViewHolder, position: Int) {
        val currentItem = listaAtual[position]
        val tituloReuniao = currentItem.Titulo
        var horarioInicio = currentItem.DataHoraInicio
        var horarioFim = currentItem.DataHoraFim

        if (horarioInicio.isNotBlank() && horarioFim.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(horarioInicio, inputFormatter)
                horarioInicio = startDateTime.format(outputFormatter)
                val endDateTime = LocalDateTime.parse(horarioFim, inputFormatter)
                horarioFim = endDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        holder.holderTituloReuniao.text = tituloReuniao
        holder.holderHorarioInicioReuniao.text = horarioInicio
        holder.holderHorarioFimReuniao.text = horarioFim
        holder.cardViewReunioes.setOnClickListener {
            GlobalVariables.detalhesNumReuniao = currentItem.NReunioes
            GlobalVariables.detalhesTituloReuniao = currentItem.Titulo
            GlobalVariables.detalhesTipoReuniao = currentItem.Tipo
            GlobalVariables.detalhesDescricaoReuniao = currentItem.Descricao
            GlobalVariables.detalhesDataHoraInicioReuniao = currentItem.DataHoraInicio
            GlobalVariables.detalhesDataHoraFimReuniao = currentItem.DataHoraFim
            GlobalVariables.detalhesDataHoraNotificacaoReuniao = currentItem.DataHoraNotificacao
            GlobalVariables.detalhesNumUsuarioCriadorReuniao = currentItem.NUsuarioCriador
            GlobalVariables.detalhesNomeUsuarioCriadorReuniao = currentItem.NomeUsuarioCriador

            val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, FragmentDetalhesReuniaoAdmin())
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
                listaAtual = results?.values as? ArrayList<ItemsReunioes> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    class ReunioesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewReunioes: CardView = itemView.findViewById(R.id.CardViewReunioes)
        val holderTituloReuniao: TextView = itemView.findViewById(R.id.tituloReuniao)
        val holderHorarioInicioReuniao: TextView = itemView.findViewById(R.id.horarioInicioReuniao)
        val holderHorarioFimReuniao: TextView = itemView.findViewById(R.id.horarioFimReuniao)
    }
}