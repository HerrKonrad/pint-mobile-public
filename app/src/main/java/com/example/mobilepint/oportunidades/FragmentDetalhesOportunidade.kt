package com.example.mobilepint.oportunidades

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FragmentDetalhesOportunidade : Fragment() {

    private val REQUEST_PHONE_CALL = 1
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_oportunidade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo)

        val txt_titulo_negocio_label = view.findViewById(R.id.detalhes_negocio_txt_titulo_negocio_label) as TextView
        val txt_data_criacao_label = view.findViewById(R.id.detalhes_negocio_txt_data_criacao_label) as TextView
        val txt_valor_negocio_label = view.findViewById(R.id.detalhes_negocio_txt_valor_negocio_label) as TextView
        val txt_nome_area_negocio_label = view.findViewById(R.id.detalhes_negocio_txt_nome_area_negocio_label) as TextView
        val txt_nome_tipo_projeto_label = view.findViewById(R.id.detalhes_negocio_txt_nome_tipo_projeto_label) as TextView
        val txt_nome_estagio_label = view.findViewById(R.id.detalhes_negocio_txt_nome_estagio_label) as TextView
        val txt_nome_cliente_label = view.findViewById(R.id.detalhes_negocio_txt_nome_cliente_label) as TextView
        val txt_telefone_cliente_label = view.findViewById(R.id.detalhes_negocio_txt_telefone_cliente_label) as TextView
        val txt_email_cliente_label = view.findViewById(R.id.detalhes_negocio_txt_email_cliente_label) as TextView
        val txt_autor_negocio_label = view.findViewById(R.id.detalhes_negocio_txt_autor_negocio_label) as TextView
        val txt_descricao_negocio_label = view.findViewById(R.id.detalhes_negocio_txt_descricao_negocio_label) as TextView
        val btn_editar_negocio = view.findViewById(R.id.detalhes_negocio_btn_editar_negocio) as Button
        val btn_criar_reuniao_oportunidade = view.findViewById(R.id.detalhes_negocio_btn_criar_reuniao_oportunidade) as Button
        val btn_ver_relacoes_estabelecidas = view.findViewById(R.id.detalhes_negocio_btn_ver_relacoes_estabelecidas) as Button
        val btn_contactar_cliente = view.findViewById(R.id.detalhes_negocio_btn_contactar_cliente) as Button

        val strFormattedDate = if (GlobalVariables.detalhesDataCriacaoOportunidade.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataCriacaoOportunidade, inputFormatter)
                startDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                GlobalVariables.detalhesDataCriacaoOportunidade
            } catch (exception: Exception) {
                exception.printStackTrace()
                GlobalVariables.detalhesDataCriacaoOportunidade
            }
        } else {
            GlobalVariables.detalhesDataCriacaoOportunidade
        }

        txt_titulo_negocio_label.text = GlobalVariables.detalhesTituloOportunidade
        txt_data_criacao_label.text = strFormattedDate
        txt_valor_negocio_label.text = getString(R.string.str_valor_negocio_label,GlobalVariables.detalhesValorOportunidade)
        txt_nome_area_negocio_label.text = GlobalVariables.detalhesNomeEtiquetaOportunidade
        txt_nome_tipo_projeto_label.text = GlobalVariables.detalhesNomeTipoProjetoOportunidade
        txt_nome_estagio_label.text = GlobalVariables.detalhesNomeEstagioOportunidade
        txt_nome_cliente_label.text = GlobalVariables.detalhesNomeCliente
        txt_telefone_cliente_label.text = GlobalVariables.detalhesTelefoneCliente
        txt_email_cliente_label.text = GlobalVariables.detalhesEmailCliente
        txt_autor_negocio_label.text = GlobalVariables.detalhesNomeUsuarioCriadorOportunidade
        txt_descricao_negocio_label.text = GlobalVariables.detalhesDescricaoOportunidade

        if (havePermissions) {
            btn_criar_reuniao_oportunidade.visibility = View.VISIBLE
            btn_contactar_cliente.visibility = View.VISIBLE
        } else {
            btn_criar_reuniao_oportunidade.visibility = View.GONE
            btn_contactar_cliente.visibility = View.GONE
        }

        btn_editar_negocio.setOnClickListener {
            GlobalVariables.criarOportunidade = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarOportunidade())
            fragment.commit()
        }

        btn_criar_reuniao_oportunidade.setOnClickListener {
            GlobalVariables.criarReuniaoOportunidade = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarReuniaoOportunidade())
            fragment.commit()
        }

        btn_ver_relacoes_estabelecidas.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerRelacoesEstabelecidas())
            fragment.commit()
        }

        btn_contactar_cliente.setOnClickListener {
            val phoneNumber = GlobalVariables.detalhesTelefoneCliente
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Aviso")
            builder.setMessage("Tem a certeza que pretende ligar para \'$phoneNumber\'?")
            builder.setIcon(R.drawable.ic_information)
            builder.setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                if (isTelephonyAvailable(requireContext())) {
                    makePhoneCall(phoneNumber)
                } else {
                    Toast.makeText(requireContext(), "Não é possível fazer chamadas telefónicas neste dispositivo.", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
            fragment.commit()
        }
    }

    private fun isTelephonyAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(requireActivity() as AppCompatActivity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
        }
    }
}