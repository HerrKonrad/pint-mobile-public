package com.example.mobilepint.vagas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FragmentDetalhesNotaEntrevista : Fragment() {

    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_nota_entrevista, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        val txt_data_criacao_label = view.findViewById(R.id.detalhes_nota_txt_data_criacao_label) as TextView
        val txt_autor_label = view.findViewById(R.id.detalhes_nota_txt_autor_label) as TextView
        val txt_texto_label = view.findViewById(R.id.detalhes_nota_txt_texto_label) as TextView
        val txt_estado_label = view.findViewById(R.id.detalhes_nota_txt_estado_label) as TextView
        val txt_nome_candidato_label = view.findViewById(R.id.detalhes_nota_txt_nome_candidato_label) as TextView
        val txt_nome_vaga_label = view.findViewById(R.id.detalhes_nota_txt_nome_vaga_label) as TextView
        val btn_editar_nota = view.findViewById(R.id.detalhes_nota_btn_editar_nota) as Button

        val strFormattedDate = if (GlobalVariables.detalhesDataNota.isNotBlank()) {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataNota, inputFormatter)
            startDateTime.format(outputFormatter)
        } else {
            ""
        }

        txt_data_criacao_label.text = strFormattedDate
        txt_autor_label.text = GlobalVariables.detalhesAutorNota
        txt_texto_label.text = GlobalVariables.detalhesTextoNota
        txt_estado_label.text = GlobalVariables.detalhesEstadoEntrevista
        txt_nome_candidato_label.text = GlobalVariables.detalhesNomeUsuarioCandidatura
        txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVagaCandidatura

        if (havePermissions) {
            btn_editar_nota.visibility = View.VISIBLE
        } else {
            btn_editar_nota.visibility = View.GONE
        }

        btn_editar_nota.setOnClickListener {
            GlobalVariables.criarNotaEntrevista = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarNotaEntrevista())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerNotasEntrevista())
            fragment.commit()
        }
    }
}