package com.example.mobilepint.oportunidades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FragmentDetalhesCliente : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_nome_cliente_label = view.findViewById(R.id.detalhes_cliente_txt_nome_cliente_label) as TextView
        val txt_data_criacao_label = view.findViewById(R.id.detalhes_cliente_txt_data_criacao_label) as TextView
        val txt_telefone_cliente_label = view.findViewById(R.id.detalhes_cliente_txt_telefone_cliente_label) as TextView
        val txt_email_cliente_label = view.findViewById(R.id.detalhes_cliente_txt_email_cliente_label) as TextView
        val txt_autor_cliente_label = view.findViewById(R.id.detalhes_cliente_txt_autor_cliente_label) as TextView
        val txt_descricao_cliente_label = view.findViewById(R.id.detalhes_cliente_txt_descricao_cliente_label) as TextView
        val btn_editar_cliente = view.findViewById(R.id.detalhes_cliente_btn_editar_cliente) as Button
        val btn_visualizar_contactos = view.findViewById(R.id.detalhes_cliente_btn_visualizar_contactos) as Button

        val strFormattedDate = if (GlobalVariables.detalhesDataCriacaoCliente.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataCriacaoCliente, inputFormatter)
                startDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                GlobalVariables.detalhesDataCriacaoCliente
            } catch (exception: Exception) {
                exception.printStackTrace()
                GlobalVariables.detalhesDataCriacaoCliente
            }
        } else {
            GlobalVariables.detalhesDataCriacaoCliente
        }

        txt_nome_cliente_label.text = GlobalVariables.detalhesNomeCliente
        txt_data_criacao_label.text = strFormattedDate
        txt_telefone_cliente_label.text = GlobalVariables.detalhesTelefoneCliente
        txt_email_cliente_label.text = GlobalVariables.detalhesEmailCliente
        txt_autor_cliente_label.text = GlobalVariables.detalhesNomeUsuarioCriadorCliente
        txt_descricao_cliente_label.text = GlobalVariables.detalhesDescricaoCliente

        btn_editar_cliente.setOnClickListener {
            GlobalVariables.criarCliente = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarCliente())
            fragment.commit()
        }

        btn_visualizar_contactos.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerContactosCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerClientes())
            fragment.commit()
        }
    }
}