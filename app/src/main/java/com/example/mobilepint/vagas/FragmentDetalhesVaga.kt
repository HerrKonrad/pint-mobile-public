package com.example.mobilepint.vagas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentDetalhesVaga : Fragment() {

    private lateinit var candidaturasVagasList: ArrayList<ItemsCandidaturas>
    private lateinit var btn_candidatar_me: Button
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_vaga, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        val txt_nome_vaga_label = view.findViewById(R.id.detalhes_vaga_txt_nome_vaga_label) as TextView
        val txt_subtitulo_vaga_label = view.findViewById(R.id.detalhes_vaga_txt_subtitulo_vaga_label) as TextView
        val txt_localidade_vaga_label = view.findViewById(R.id.detalhes_vaga_txt_localidade_vaga_label) as TextView
        val txt_tipo_vaga_label = view.findViewById(R.id.detalhes_vaga_txt_tipo_vaga_label) as TextView
        val txt_descricao_vaga_label = view.findViewById(R.id.detalhes_vaga_txt_descricao_vaga_label) as TextView
        btn_candidatar_me = view.findViewById(R.id.detalhes_vaga_btn_candidatar_me)
        val btn_recomendar_vaga = view.findViewById(R.id.detalhes_vaga_btn_recomendar_vaga) as Button
        val btn_editar_vaga = view.findViewById(R.id.detalhes_vaga_btn_editar_vaga) as Button
        val layout_ver_vaga = view.findViewById(R.id.detalhes_vaga_linearLayout_ver_vaga) as LinearLayout
        val layout_editar_vaga = view.findViewById(R.id.detalhes_vaga_linearLayout_editar_vaga) as LinearLayout

        txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVaga
        txt_subtitulo_vaga_label.text = GlobalVariables.detalhesSubtituloVaga
        txt_localidade_vaga_label.text = GlobalVariables.detalhesLocalidadeVaga
        txt_tipo_vaga_label.text = GlobalVariables.detalhesTipoVaga
        txt_descricao_vaga_label.text = GlobalVariables.detalhesDescricaoVaga

        candidaturasVagasList = ArrayList()
        btn_candidatar_me.visibility = View.GONE

        if (globalVariables.checkForInternet(requireContext())) {
            getCandidaturaVaga(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        if (GlobalVariables.verVagas) {
            layout_ver_vaga.visibility = View.VISIBLE
            layout_editar_vaga.visibility = View.GONE
        } else {
            layout_ver_vaga.visibility = View.GONE
            layout_editar_vaga.visibility = View.VISIBLE
        }

        btn_candidatar_me.setOnClickListener {
            GlobalVariables.criarCandidatura = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarCandidaturaVaga())
            fragment.commit()
        }

        btn_recomendar_vaga.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentRecomendarVaga())
            fragment.commit()
        }

        btn_editar_vaga.setOnClickListener {
            if (havePermissions) {
                GlobalVariables.criarVaga = false
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentCriarVaga())
                fragment.commit()
            } else {
                Toast.makeText(requireContext(), "Esta funcionalidade requer privilégios adicionais.", Toast.LENGTH_LONG).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (GlobalVariables.verVagas) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentGerirVagas())
                fragment.commit()
            }
        }
    }

    private fun getCandidaturaVaga(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url("${GlobalVariables.serverUrl}/api/candidaturas?nusuario=${GlobalVariables.idUtilizadorAutenticado}&nvaga=${GlobalVariables.detalhesNumVaga}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")

                try {
                    requireActivity().runOnUiThread {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Erro")
                        builder.setMessage(e.message)
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                        builder.setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body.string()
                println("API Data: $responseBody")

                try {
                    requireActivity().runOnUiThread {
                        val jsonObject = JSONObject(responseBody)
                        val success = jsonObject.getBoolean("success")

                        if (success) {
                            val jsonArray = jsonObject.getJSONArray("message")
                            for (i in 0 until jsonArray.length()) {
                                val message = jsonArray.getJSONObject(i)
                                val NCandidatura = message.getInt("NCandidatura")
                                val NVaga = message.getInt("NVaga")
                                val NUsuario = message.getInt("NUsuario")
                                val Estado = message.getInt("Estado")
                                var Estagio = message.getString("Estagio")
                                var DataCandidatura = message.getString("DataCandidatura")
                                var PretencaoSalarial = message.getString("PretencaoSalarial")
                                var Mensagem = message.getString("Mensagem")
                                var NomeUsuario = message.getString("NomeUsuario")
                                var NomeVaga = message.getString("NomeVaga")
                                var SubtituloVaga = message.getString("Subtitulo")
                                var EmailUsuario = message.getString("EmailUsuario")

                                if (Estagio.isNullOrBlank() || Estagio == "null") {
                                    Estagio = ""
                                }
                                if (DataCandidatura.isNullOrBlank() || DataCandidatura == "null") {
                                    DataCandidatura = ""
                                }
                                if (PretencaoSalarial.isNullOrBlank() || PretencaoSalarial == "null") {
                                    PretencaoSalarial = ""
                                }
                                if (Mensagem.isNullOrBlank() || Mensagem == "null") {
                                    Mensagem = ""
                                }
                                if (NomeUsuario.isNullOrBlank() || NomeUsuario == "null") {
                                    NomeUsuario = ""
                                }
                                if (NomeVaga.isNullOrBlank() || NomeVaga == "null") {
                                    NomeVaga = ""
                                }
                                if (SubtituloVaga.isNullOrBlank() || SubtituloVaga == "null") {
                                    SubtituloVaga = ""
                                }
                                if (EmailUsuario.isNullOrBlank() || EmailUsuario == "null") {
                                    EmailUsuario = ""
                                }

                                candidaturasVagasList.add(
                                    ItemsCandidaturas(
                                        NCandidatura,
                                        NVaga,
                                        NUsuario,
                                        DataCandidatura,
                                        PretencaoSalarial,
                                        Mensagem,
                                        Estado,
                                        Estagio,
                                        NomeUsuario,
                                        NomeVaga,
                                        SubtituloVaga,
                                        EmailUsuario
                                    )
                                )
                            }

                            if (candidaturasVagasList.isEmpty()) {
                                btn_candidatar_me.visibility = View.VISIBLE
                            } else {
                                btn_candidatar_me.visibility = View.GONE
                            }
                        } else {
                            val message = jsonObject.getString("message")
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(message)
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            builder.show()
                        }
                    }
                } catch (jsonException: JSONException) {
                    Toast.makeText(context, "JSON error: ${jsonException.message}", Toast.LENGTH_LONG).show()
                    jsonException.printStackTrace()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        })
    }
}