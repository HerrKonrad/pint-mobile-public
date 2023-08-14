package com.example.mobilepint.ideias

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.R

class AdapterTopicosIdeias(
    private val mcontext: Context,
    private val TopicosIdeiasList: ArrayList<ItemsTopicosIdeiasFull>,
) :
    RecyclerView.Adapter<AdapterTopicosIdeias.TopicosIdeiasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicosIdeiasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_topicos_ideias_checkbox, parent, false)
        return TopicosIdeiasViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicosIdeiasViewHolder, position: Int) {
        val currentItem = TopicosIdeiasList[position]
        holder.holderCheckBoxTopicoIdeia.text = currentItem.NomeTopico
        holder.holderCheckBoxTopicoIdeia.isChecked = currentItem.IsChecked
        holder.holderCheckBoxTopicoIdeia.setOnClickListener {
            currentItem.IsChecked = !currentItem.IsChecked
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return TopicosIdeiasList.size
    }

    class TopicosIdeiasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holderCheckBoxTopicoIdeia: CheckBox = itemView.findViewById(R.id.CheckBoxTopicoIdeia)
    }
}