package com.example.mobilepint.utilizadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.R

class AdapterUtilizadoresReuniao(
    private val mcontext: Context,
    private val UtilizadoresReuniaoList: ArrayList<ItemsUtilizadoresReuniao>,
) :
    RecyclerView.Adapter<AdapterUtilizadoresReuniao.UtilizadoresReuniaoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtilizadoresReuniaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerviewmodel_utilizadores_reuniao, parent, false)
        return UtilizadoresReuniaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: UtilizadoresReuniaoViewHolder, position: Int) {
        val currentItem = UtilizadoresReuniaoList[position]
        holder.holderCheckBoxUtilizadorReuniao.text = currentItem.NomeUsuario
        holder.holderCheckBoxUtilizadorReuniao.isChecked = currentItem.IsChecked
        holder.holderCheckBoxUtilizadorReuniao.isEnabled = !currentItem.IsChecked
        holder.holderCheckBoxUtilizadorReuniao.setOnClickListener {
            currentItem.IsChecked = currentItem.IsChecked
            notifyItemChanged(position)
            val builder = AlertDialog.Builder(mcontext)
            builder.setTitle("Agregar utilizador à reunião")
            builder.setMessage("Tem a certeza que pretende adicionar o utilizador \'${currentItem.NomeUsuario}\' à reunião?")
            builder.setIcon(R.drawable.ic_information)
            builder.setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                currentItem.IsChecked = !currentItem.IsChecked
                notifyItemChanged(position)
            }
            builder.setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return UtilizadoresReuniaoList.size
    }

    class UtilizadoresReuniaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holderCheckBoxUtilizadorReuniao: CheckBox = itemView.findViewById(R.id.CheckBoxUtilizadorReuniao)
    }
}