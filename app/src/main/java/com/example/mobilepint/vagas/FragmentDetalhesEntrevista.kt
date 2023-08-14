package com.example.mobilepint.vagas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R

class FragmentDetalhesEntrevista : Fragment() {

    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_entrevista, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        val txt_nome_vaga_label = view.findViewById(R.id.detalhes_entrevista_txt_nome_vaga_label) as TextView
        val txt_nome_candidato_label = view.findViewById(R.id.detalhes_entrevista_txt_nome_candidato_label) as TextView
        val txt_estado_label = view.findViewById(R.id.detalhes_entrevista_txt_estado_label) as TextView
        val txt_descricao_label = view.findViewById(R.id.detalhes_entrevista_txt_descricao_label) as TextView
        val layout_ver_descricao = view.findViewById(R.id.detalhes_entrevista_linearLayout_ver_descricao) as LinearLayout
        val layout_btns_permissoes = view.findViewById(R.id.detalhes_entrevista_linearLayout_btns_permissoes) as LinearLayout
        val btn_editar_entrevista = view.findViewById(R.id.detalhes_entrevista_btn_editar_entrevista) as Button
        val btn_ver_notas_entrevista = view.findViewById(R.id.detalhes_entrevista_btn_ver_notas_entrevista) as Button

        txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVagaCandidatura
        txt_nome_candidato_label.text = GlobalVariables.detalhesNomeUsuarioCandidatura
        txt_estado_label.text = GlobalVariables.detalhesEstadoEntrevista
        txt_descricao_label.text = GlobalVariables.detalhesDescricaoEntrevista

        if (havePermissions) {
            layout_ver_descricao.visibility = View.VISIBLE
            layout_btns_permissoes.visibility = View.VISIBLE
        } else {
            layout_ver_descricao.visibility = View.GONE
            layout_btns_permissoes.visibility = View.GONE
        }

        btn_editar_entrevista.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentEditarEntrevista())
            fragment.commit()
        }

        btn_ver_notas_entrevista.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerNotasEntrevista())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerEntrevistasVagas())
            fragment.commit()
        }
    }
}