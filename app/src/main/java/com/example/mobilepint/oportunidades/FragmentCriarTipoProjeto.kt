package com.example.mobilepint.oportunidades

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentCriarTipoProjeto: Fragment() {

    private var globalVariables = GlobalVariables()
    private var bundleNumTipoProjeto: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_tipo_projeto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strNome = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_tipo_projeto_txt_titulo_janela) as TextView
        val edtxt_nome_tipo_projeto = view.findViewById(R.id.criar_tipo_projeto_edtxt_nome_tipo_projeto) as EditText
        val btn_criar_tipo_projeto = view.findViewById(R.id.criar_tipo_projeto_btn_criar_tipo_projeto) as Button

        if (GlobalVariables.criarTipoProjeto) {
            txt_titulo_janela.text = getString(R.string.str_criar_tipo_projeto)
            btn_criar_tipo_projeto.text = getString(R.string.str_criar_tipo_projeto)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_tipo_projeto)
            btn_criar_tipo_projeto.text = getString(R.string.str_editar_tipo_projeto)

            val bundleTipoProjeto = arguments
            if (bundleTipoProjeto != null) {
                bundleNumTipoProjeto = bundleTipoProjeto.getInt("NumTipoProjeto")
                val bundleNomeTipoProjeto = bundleTipoProjeto.getString("NomeTipoProjeto")
                edtxt_nome_tipo_projeto.text = Editable.Factory.getInstance().newEditable(bundleNomeTipoProjeto)
            }
        }

        btn_criar_tipo_projeto.setOnClickListener {
            btn_criar_tipo_projeto.isEnabled = false
            Handler().postDelayed({
                btn_criar_tipo_projeto.isEnabled = true
            }, 10000)

            if (edtxt_nome_tipo_projeto.text.toString().isNotBlank()) {
                strNome = edtxt_nome_tipo_projeto.text.toString()
            } else {
                msgErrors += "O campo \'Nome do tipo de projeto\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarTipoProjeto) {
                        postTipoProjeto(requireContext(), strNome)
                    } else {
                        putTipoProjeto(requireContext(), strNome)
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
            fragment.replace(R.id.fragment_container, FragmentVerTiposProjetos())
            fragment.commit()
        }
    }

    private fun postTipoProjeto(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/tipoprojetos")
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
                            val NTipoProjeto = message.getInt("NTipoProjeto")
                            val Nome = message.getString("Nome")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O tipo de projeto \'$Nome\' foi criado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerTiposProjetos())
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

    private fun putTipoProjeto(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/tipoprojetos/" + bundleNumTipoProjeto)
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
                            builder.setMessage("O tipo de projeto \'$nome\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerTiposProjetos())
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