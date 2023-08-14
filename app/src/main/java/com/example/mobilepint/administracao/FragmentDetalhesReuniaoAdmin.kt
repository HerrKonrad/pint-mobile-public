package com.example.mobilepint.administracao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.utilizadores.FragmentVerUtilizadoresReuniao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FragmentDetalhesReuniaoAdmin : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_reuniao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_titulo_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_titulo_reuniao_label) as TextView
        val txt_autor_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_autor_reuniao_label) as TextView
        val txt_tipo_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_tipo_reuniao_label) as TextView
        val txt_inicio_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_inicio_reuniao_label) as TextView
        val txt_fim_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_fim_reuniao_label) as TextView
        val txt_descricao_reuniao_label = view.findViewById(R.id.detalhes_reuniao_txt_descricao_reuniao_label) as TextView
        val btn_editar_reuniao = view.findViewById(R.id.detalhes_reuniao_btn_editar_reuniao) as Button
        val btn_agregar_utilizadores = view.findViewById(R.id.detalhes_reuniao_btn_agregar_utilizadores) as Button
        val layout_btns_permissoes = view.findViewById(R.id.detalhes_reuniao_linearLayout_btns_permissoes) as LinearLayout

        val strTipoReuniao = when (GlobalVariables.detalhesTipoReuniao) {
            0 -> {
                "Reunião sobre entrevista"
            }

            1 -> {
                "Reunião sobre oportunidade"
            }

            2 -> {
                "Outros"
            }

            else -> {
                "Outros"
            }
        }

        var strHorarioInicio = GlobalVariables.detalhesDataHoraInicioReuniao
        var strHorarioFim = GlobalVariables.detalhesDataHoraFimReuniao

        if (strHorarioInicio.isNotBlank() && strHorarioFim.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(strHorarioInicio, inputFormatter)
                strHorarioInicio = startDateTime.format(outputFormatter)
                val endDateTime = LocalDateTime.parse(strHorarioFim, inputFormatter)
                strHorarioFim = endDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        txt_titulo_reuniao_label.text = GlobalVariables.detalhesTituloReuniao
        txt_autor_reuniao_label.text = GlobalVariables.detalhesNomeUsuarioCriadorReuniao
        txt_tipo_reuniao_label.text = strTipoReuniao
        txt_inicio_reuniao_label.text = strHorarioInicio
        txt_fim_reuniao_label.text = strHorarioFim
        txt_descricao_reuniao_label.text = GlobalVariables.detalhesDescricaoReuniao
        layout_btns_permissoes.visibility = View.VISIBLE

        btn_editar_reuniao.setOnClickListener {
            GlobalVariables.criarReuniaoOutros = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarReuniaoOutrosAdmin())
            fragment.commit()
        }

        btn_agregar_utilizadores.setOnClickListener {
            GlobalVariables.verReuniaoAdministracao = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerUtilizadoresReuniao())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
            fragment.commit()
        }
    }
}