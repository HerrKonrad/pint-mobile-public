package com.example.mobilepint.administracao

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentCriarTopicoIdeiasAdmin: Fragment() {

    private var globalVariables = GlobalVariables()
    private var bundleNumTopicoIdeia: Int = 0
    private var bundleNomeTopico: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_topico_ideias_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strNomeTopico = ""
        var msgErrors = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_topico_ideias_txt_titulo_janela) as TextView
        val edtxt_nome_topico_ideias = view.findViewById(R.id.criar_topico_ideias_edtxt_nome_topico_ideias) as EditText
        val criar_topico_ideias = view.findViewById(R.id.criar_topico_ideias_btn_criar_topico_ideias) as Button

        if (GlobalVariables.criarTopicoIdeias) {
            txt_titulo_janela.text = getString(R.string.str_criar_topico)
            criar_topico_ideias.text = getString(R.string.str_criar_topico)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_topico)
            criar_topico_ideias.text = getString(R.string.str_editar_topico)

            val bundle = arguments
            if (bundle != null) {
                bundleNumTopicoIdeia = bundle.getInt("NTopicoIdeia")
                bundleNomeTopico = bundle.getString("NomeTopico").toString()
                edtxt_nome_topico_ideias.text = Editable.Factory.getInstance().newEditable(bundleNomeTopico)
            }
        }

        criar_topico_ideias.setOnClickListener {
            criar_topico_ideias.isEnabled = false
            Handler().postDelayed({
                criar_topico_ideias.isEnabled = true
            }, 10000)

            if (edtxt_nome_topico_ideias.text.toString().isNotBlank()) {
                strNomeTopico = edtxt_nome_topico_ideias.text.toString()
            } else {
                msgErrors += "O campo \'Nome do tópico das ideias\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarTopicoIdeias) {
                        postTopicoIdeias(
                            requireContext(),
                            strNomeTopico
                        )
                    } else {
                        putTopicoIdeias(
                            requireContext(),
                            strNomeTopico
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage(msgErrors)
                builder.setIcon(R.drawable.ic_information)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    msgErrors = ""
                }
                builder.show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
            fragment.commit()
        }
    }

    private fun postTopicoIdeias(context: Context, nome: String) {
        data class Post(val NomeTopico: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Builder()
            .url(GlobalVariables.serverUrl + "/api/topicoideias")
            .post(requestBody)
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
                            val message = jsonObject.getJSONObject("message")
                            val NTopicoIdeia = message.getInt("NTopicoIdeia")
                            val NomeTopico = message.getString("NomeTopico")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O tópico \'$NomeTopico\' foi criado com sucesso.")
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

    private fun putTopicoIdeias(context: Context, nome: String) {
        data class Post(val NomeTopico: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Builder()
            .url(GlobalVariables.serverUrl + "/api/topicoideias/" + bundleNumTopicoIdeia)
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

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O tópico \'$nome\' foi alterado com sucesso.")
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