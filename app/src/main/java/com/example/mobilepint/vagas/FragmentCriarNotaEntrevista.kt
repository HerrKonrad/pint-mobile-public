package com.example.mobilepint.vagas

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

class FragmentCriarNotaEntrevista: Fragment() {

    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_nota_entrevista, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strObservacoes = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_nota_txt_titulo_janela) as TextView
        val txt_subtitulo_janela = view.findViewById(R.id.criar_nota_txt_subtitulo_janela) as TextView
        val edtxt_observacoes_entrevista = view.findViewById(R.id.criar_nota_edtxt_observacoes_entrevista) as EditText
        val btn_criar_nota = view.findViewById(R.id.criar_nota_btn_criar_nota) as Button

        if (GlobalVariables.criarNotaEntrevista) {
            txt_titulo_janela.text = getString(R.string.str_criar_nota)
            txt_subtitulo_janela.text = getString(R.string.str_adicionar_nota_entrevista)
            btn_criar_nota.text = getString(R.string.str_criar_nota)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_nota)
            txt_subtitulo_janela.text = getString(R.string.str_editar_nota_selecionada_entrevista)
            btn_criar_nota.text = getString(R.string.str_editar_nota)

            edtxt_observacoes_entrevista.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTextoNota)
        }

        btn_criar_nota.setOnClickListener {
            btn_criar_nota.isEnabled = false
            Handler().postDelayed({
                btn_criar_nota.isEnabled = true
            }, 10000)

            if (edtxt_observacoes_entrevista.text.toString().isNotBlank()) {
                strObservacoes = edtxt_observacoes_entrevista.text.toString()
            } else {
                msgErrors += "O campo \'Observações da entrevista\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarNotaEntrevista) {
                        postNota(
                            requireContext(),
                            GlobalVariables.detalhesNumEntrevista,
                            strObservacoes,
                            GlobalVariables.idUtilizadorAutenticado
                        )
                    } else {
                        putNota(requireContext(), strObservacoes)
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
            if (GlobalVariables.criarNotaEntrevista) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerNotasEntrevista())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesNotaEntrevista())
                fragment.commit()
            }
        }
    }

    private fun postNota(
        context: Context,
        numEntrevista: Int,
        observacoes: String,
        numUtilizadorRH: Int
    ) {
        data class Post(val NEntrevista: Int, val Texto: String, val NUsuarioRH: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numEntrevista, observacoes, numUtilizadorRH)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/nota")
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
                            val NNota = message.getInt("NNota")
                            val NEntrevista = message.getInt("NEntrevista")
                            val Texto = message.getString("Texto")
                            val NUsuarioRH = message.getInt("NUsuarioRH")
                            val DataHora = message.getString("DataHora")
                            val NomeRH = message.getString("NomeRH")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A nota foi adicionada à entrevista com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerNotasEntrevista())
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

    private fun putNota(context: Context, observacoes: String) {
        data class Post(val Texto: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(observacoes)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/nota/" + GlobalVariables.detalhesNumNota)
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
                            builder.setMessage("A nota foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerNotasEntrevista())
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