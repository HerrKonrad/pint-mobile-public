package com.example.mobilepint.oportunidades

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FragmentDetalhesRelacaoCliente : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_relacao_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_titulo_interacao_label = view.findViewById(R.id.detalhes_interacao_txt_titulo_label) as TextView
        val txt_data_criacao_label = view.findViewById(R.id.detalhes_interacao_txt_data_criacao_label) as TextView
        val txt_descricao_label = view.findViewById(R.id.detalhes_interacao_txt_descricao_label) as TextView
        val txt_titulo_negocio_label = view.findViewById(R.id.detalhes_interacao_txt_titulo_negocio_label) as TextView
        val txt_nome_cliente_label = view.findViewById(R.id.detalhes_interacao_txt_nome_cliente_label) as TextView
        val edtxt_endereco_anexo = view.findViewById(R.id.detalhes_interacao_edtxt_endereco_anexo) as EditText
        val imgbtn_download_ficheiro = view.findViewById(R.id.detalhes_interacao_imgbtn_download_ficheiro) as ImageButton
        val btn_editar_interacao = view.findViewById(R.id.detalhes_interacao_btn_editar_interacao) as Button

        val strFormattedDate = if (GlobalVariables.detalhesDataHoraCriacaoStatus.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataHoraCriacaoStatus, inputFormatter)
                startDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                GlobalVariables.detalhesDataHoraCriacaoStatus
            } catch (exception: Exception) {
                exception.printStackTrace()
                GlobalVariables.detalhesDataHoraCriacaoStatus
            }
        } else {
            GlobalVariables.detalhesDataHoraCriacaoStatus
        }

        txt_titulo_interacao_label.text = GlobalVariables.detalhesTituloStatus
        txt_data_criacao_label.text = strFormattedDate
        txt_descricao_label.text = GlobalVariables.detalhesDescricaoStatus
        txt_titulo_negocio_label.text = GlobalVariables.detalhesTituloOportunidade
        txt_nome_cliente_label.text = GlobalVariables.detalhesNomeCliente
        edtxt_endereco_anexo.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEnderecoAnexoStatus)

        imgbtn_download_ficheiro.setOnClickListener {
            val urlCurriculo = edtxt_endereco_anexo.text.toString()
            if (urlCurriculo.isNotBlank()) {
                val fileName = urlCurriculo.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlCurriculo, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
            }
        }

        btn_editar_interacao.setOnClickListener {
            GlobalVariables.criarRelacaoCliente = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarRelacaoCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerRelacoesEstabelecidas())
            fragment.commit()
        }
    }

    private fun downloadAndSaveFile(context: Context, fileUrl: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(fileUrl))
                .setTitle(fileName)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            Toast.makeText(context, "A transferência foi concluída com sucesso.", Toast.LENGTH_LONG).show()
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}