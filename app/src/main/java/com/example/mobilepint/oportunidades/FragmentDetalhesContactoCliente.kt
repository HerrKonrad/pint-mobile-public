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

class FragmentDetalhesContactoCliente : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_contacto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_contacto_eletronico_label = view.findViewById(R.id.detalhes_contacto_txt_contacto_eletronico_label) as TextView
        val txt_contacto_telefonico_label = view.findViewById(R.id.detalhes_contacto_txt_contacto_telefonico_label) as TextView
        val txt_nome_cliente_label = view.findViewById(R.id.detalhes_contacto_txt_nome_cliente_label) as TextView
        val btn_editar_contacto = view.findViewById(R.id.detalhes_contacto_btn_editar_contacto) as Button

        txt_contacto_eletronico_label.text = GlobalVariables.detalhesEmailContacto
        txt_contacto_telefonico_label.text = GlobalVariables.detalhesTelefoneContacto
        txt_nome_cliente_label.text = GlobalVariables.detalhesNomeCliente

        btn_editar_contacto.setOnClickListener {
            GlobalVariables.criarContactoCliente = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarContactoCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerContactosCliente())
            fragment.commit()
        }
    }
}