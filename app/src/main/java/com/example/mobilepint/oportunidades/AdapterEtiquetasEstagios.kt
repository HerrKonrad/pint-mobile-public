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

class AdapterEtiquetasEstagios(
    private val mcontext: Context,
    private val EtiquetasList: ArrayList<ItemsEtiquetas>,
    private val EstagiosList: ArrayList<ItemsEstagios>
) :
    RecyclerView.Adapter<AdapterEtiquetasEstagios.EtiquetasEstagiosViewHolder>(),
    Filterable {

    private var listaOriginalEtiquetas: ArrayList<ItemsEtiquetas> = ArrayList(EtiquetasList)
    private var listaAtualEtiquetas: ArrayList<ItemsEtiquetas> = ArrayList(EtiquetasList)
    private var listaOriginalEstagios: ArrayList<ItemsEstagios> = ArrayList(EstagiosList)
    private var listaAtualEstagios: ArrayList<ItemsEstagios> = ArrayList(EstagiosList)
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtiquetasEstagiosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_etiquetas_estagios, parent, false)
        return EtiquetasEstagiosViewHolder(view)
    }

    override fun onBindViewHolder(holder: EtiquetasEstagiosViewHolder, position: Int) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo)

        if (GlobalVariables.verEtiquetas) {
            val currentItem = listaAtualEtiquetas[position]
            val nomeEtiqueta = currentItem.Nome
            val detalhes = "<b>Etiqueta:</b><br>- Nome: $nomeEtiqueta"

            holder.holderNomeEtiquetaEstagio.text = nomeEtiqueta
            holder.cardViewEtiquetasEstagios.setOnClickListener {
                val builder = AlertDialog.Builder(mcontext)
                builder.setTitle("Detalhes")
                builder.setMessage(Html.fromHtml(detalhes))
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                if (havePermissions) {
                    builder.setNegativeButton("Editar") { dialog, _ ->
                        dialog.dismiss()
                        GlobalVariables.criarEtiquetaEstagio = false

                        val bundleEtiqueta = Bundle()
                        bundleEtiqueta.putInt("NumEtiqueta", currentItem.NEtiqueta)
                        bundleEtiqueta.putString("NomeEtiqueta", currentItem.Nome)

                        val fragment = FragmentCriarEtiquetaEstagio()
                        fragment.arguments = bundleEtiqueta

                        val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
                builder.show()
            }
        } else {
            val currentItem = listaAtualEstagios[position]
            val nomeEstagio = currentItem.Nome
            val detalhes = "<b>Estágio:</b><br>- Nome: $nomeEstagio"

            holder.holderNomeEtiquetaEstagio.text = nomeEstagio
            holder.cardViewEtiquetasEstagios.setOnClickListener {
                val builder = AlertDialog.Builder(mcontext)
                builder.setTitle("Detalhes")
                builder.setMessage(Html.fromHtml(detalhes))
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                if (havePermissions) {
                    builder.setNegativeButton("Editar") { dialog, _ ->
                        dialog.dismiss()
                        GlobalVariables.criarEtiquetaEstagio = false

                        val bundleEstagio = Bundle()
                        bundleEstagio.putInt("NumEstagio", currentItem.NEstagio)
                        bundleEstagio.putString("NomeEstagio", currentItem.Nome)

                        val fragment = FragmentCriarEtiquetaEstagio()
                        fragment.arguments = bundleEstagio

                        val transaction = (mcontext as AppCompatActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
                builder.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (GlobalVariables.verEtiquetas) {
            listaAtualEtiquetas.size
        } else {
            listaAtualEstagios.size
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (GlobalVariables.verEtiquetas) {
                    if (charSequence.isNullOrEmpty()) {
                        listaAtualEtiquetas = ArrayList(listaOriginalEtiquetas)
                    } else {
                        val filterPattern = charSequence.toString().toLowerCase(Locale.ROOT).trim()
                        listaAtualEtiquetas.clear()
                        for (item in listaOriginalEtiquetas) {
                            if (item.Nome.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                                listaAtualEtiquetas.add(item)
                            }
                        }
                    }
                    results.values = listaAtualEtiquetas
                    results.count = listaAtualEtiquetas.size
                } else {
                    if (charSequence.isNullOrEmpty()) {
                        listaAtualEstagios = ArrayList(listaOriginalEstagios)
                    } else {
                        val filterPattern = charSequence.toString().toLowerCase(Locale.ROOT).trim()
                        listaAtualEstagios.clear()
                        for (item in listaOriginalEstagios) {
                            if (item.Nome.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                                listaAtualEstagios.add(item)
                            }
                        }
                    }
                    results.values = listaAtualEstagios
                    results.count = listaAtualEstagios.size
                }
                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                if (GlobalVariables.verEtiquetas) {
                    listaAtualEtiquetas = results?.values as? ArrayList<ItemsEtiquetas> ?: ArrayList()
                } else {
                    listaAtualEstagios = results?.values as? ArrayList<ItemsEstagios> ?: ArrayList()
                }
                notifyDataSetChanged()
            }
        }
    }

    class EtiquetasEstagiosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewEtiquetasEstagios: CardView = itemView.findViewById(R.id.CardViewEtiquetasEstagios)
        val holderNomeEtiquetaEstagio: TextView = itemView.findViewById(R.id.nomeEtiquetaEstagio)
    }
}