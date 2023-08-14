package com.example.mobilepint.utilizadores

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.administracao.FragmentVerAdministracao
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class FragmentDetalhesUtilizador : Fragment() {

    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_utilizador, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val txt_nome_label = view.findViewById(R.id.detalhes_utilizador_txt_nome_label) as TextView
        val txt_email_label = view.findViewById(R.id.detalhes_utilizador_txt_email_label) as TextView
        val txt_cargo_label = view.findViewById(R.id.detalhes_utilizador_txt_cargo_label) as TextView
        val txt_data_nascimento_label = view.findViewById(R.id.detalhes_utilizador_txt_data_nascimento_label) as TextView
        val txt_telefone_label = view.findViewById(R.id.detalhes_utilizador_txt_telefone_label) as TextView
        val txt_linkedin_label = view.findViewById(R.id.detalhes_utilizador_txt_linkedin_label) as TextView
        val txt_genero_label = view.findViewById(R.id.detalhes_utilizador_txt_genero_label) as TextView
        val txt_localidade_label = view.findViewById(R.id.detalhes_utilizador_txt_localidade_label) as TextView
        val txt_estado_label = view.findViewById(R.id.detalhes_utilizador_txt_estado_label) as TextView
        val txt_data_registo_label = view.findViewById(R.id.detalhes_utilizador_txt_data_registo_label) as TextView
        val btn_editar_utilizador = view.findViewById(R.id.detalhes_utilizador_btn_editar_utilizador) as Button
        val btn_ativar_utilizador = view.findViewById(R.id.detalhes_utilizador_btn_ativar_utilizador) as Button
        val btn_desativar_utilizador = view.findViewById(R.id.detalhes_utilizador_btn_desativar_utilizador) as Button

        val numCargoUtilizador = GlobalVariables.detalhesNumCargoUtilizador
        val strCargoUtilizador = EnumCargos.values().find { it.numCargo == numCargoUtilizador }?.nomeCargo

        val strEstadoUtilizador = if (GlobalVariables.detalhesEstadoUtilizador == 1) {
            "Ativo"
        } else {
            "Inativo"
        }

        val strFormattedBirthDate = if (GlobalVariables.detalhesDataNascimentoUtilizador.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataNascimentoUtilizador, inputFormatter)
                startDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                GlobalVariables.detalhesDataNascimentoUtilizador
            } catch (exception: Exception) {
                exception.printStackTrace()
                GlobalVariables.detalhesDataNascimentoUtilizador
            }
        } else {
            GlobalVariables.detalhesDataNascimentoUtilizador
        }

        val strFormattedRegistrationDate = if (GlobalVariables.detalhesDataCriacaoUtilizador.isNotBlank()) {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataCriacaoUtilizador, inputFormatter)
                startDateTime.format(outputFormatter)
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                GlobalVariables.detalhesDataCriacaoUtilizador
            } catch (exception: Exception) {
                exception.printStackTrace()
                GlobalVariables.detalhesDataCriacaoUtilizador
            }
        } else {
            GlobalVariables.detalhesDataCriacaoUtilizador
        }

        txt_nome_label.text = GlobalVariables.detalhesNomeUtilizador
        txt_email_label.text = GlobalVariables.detalhesEmailUtilizador
        txt_cargo_label.text = strCargoUtilizador
        txt_data_nascimento_label.text = strFormattedBirthDate
        txt_telefone_label.text = GlobalVariables.detalhesTelefoneUtilizador
        txt_linkedin_label.text = GlobalVariables.detalhesLinkedinUtilizador
        txt_genero_label.text = GlobalVariables.detalhesGeneroUtilizador
        txt_localidade_label.text = GlobalVariables.detalhesLocalidadeUtilizador
        txt_estado_label.text = strEstadoUtilizador
        txt_data_registo_label.text = strFormattedRegistrationDate

        if (GlobalVariables.detalhesNumUtilizador == GlobalVariables.idUtilizadorAutenticado) {
            btn_editar_utilizador.visibility = View.GONE
            btn_ativar_utilizador.visibility = View.GONE
            btn_desativar_utilizador.visibility = View.GONE
        } else {
            btn_editar_utilizador.visibility = View.VISIBLE
            btn_ativar_utilizador.visibility = View.VISIBLE
            btn_desativar_utilizador.visibility = View.VISIBLE
        }

        btn_editar_utilizador.setOnClickListener {
            GlobalVariables.criarUtilizador = false
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarUtilizador())
            fragment.commit()
        }

        btn_ativar_utilizador.setOnClickListener {
            if (GlobalVariables.detalhesEstadoUtilizador == 1) {
                Toast.makeText(requireContext(), "O utilizador \'${GlobalVariables.detalhesNomeUtilizador}\' está ativado.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Ativar utilizador")
                    builder.setMessage("Tem a certeza que pretende ativar o utilizador \'${GlobalVariables.detalhesNomeUtilizador}\'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        putDisableUser(requireContext(), 1)
                    }
                    builder.setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            }
        }

        btn_desativar_utilizador.setOnClickListener {
            if (GlobalVariables.detalhesEstadoUtilizador == 0) {
                Toast.makeText(requireContext(), "O utilizador \'${GlobalVariables.detalhesNomeUtilizador}\' está desativado.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Desativar utilizador")
                    builder.setMessage("Tem a certeza que pretende desativar o utilizador \'${GlobalVariables.detalhesNomeUtilizador}\'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        putDisableUser(requireContext(), 0)
                    }
                    builder.setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
            fragment.commit()
        }
    }

    private fun putDisableUser(context: Context, estado: Int) {
        data class Post(val Estado: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(estado)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/disableuser/" + GlobalVariables.detalhesNumUtilizador)
            .put(requestBody)
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
                            val message = jsonObject.getString("message")
                            println("Response body message: $message")

                            val outputMessage = when (estado) {
                                0 -> {
                                    "O utilizador \'${GlobalVariables.detalhesEmailUtilizador}\' foi desativado com sucesso."
                                }

                                1 -> {
                                    "O utilizador \'${GlobalVariables.detalhesEmailUtilizador}\' foi ativado com sucesso."
                                }

                                else -> {
                                    "O utilizador \'${GlobalVariables.detalhesEmailUtilizador}\' foi alterado com sucesso."
                                }
                            }

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(outputMessage)
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
                                fragment.commit()
                            }
                            builder.show()
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