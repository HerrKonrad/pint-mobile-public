package com.example.mobilepint.ideias

import android.content.Context
import android.os.Bundle
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FragmentCriarIdeia: Fragment() {

    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_ideia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strTituloIdeia = ""
        var strDescricaoIdeia = ""
        var msgErrors = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_ideia_txt_titulo_janela) as TextView
        val edtxt_titulo_ideia = view.findViewById(R.id.criar_ideia_edtxt_titulo_ideia) as EditText
        val edtxt_descricao_ideia = view.findViewById(R.id.criar_ideia_edtxt_descricao_ideia) as EditText
        val linearLayout_topicos_ideia = view.findViewById(R.id.criar_ideia_linearLayout_topicos_ideia) as LinearLayout
        val txt_associar_topicos_ideia = view.findViewById(R.id.criar_ideia_txt_associar_topicos_ideia) as TextView
        val btn_criar_ideia = view.findViewById(R.id.criar_ideia_btn_criar_ideia) as Button

        if (GlobalVariables.criarIdeia) {
            txt_titulo_janela.text = getString(R.string.str_criar_ideia)
            btn_criar_ideia.text = getString(R.string.str_criar_ideia)
            linearLayout_topicos_ideia.visibility = View.GONE
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_ideia)
            btn_criar_ideia.text = getString(R.string.str_editar_ideia)
            linearLayout_topicos_ideia.visibility = View.VISIBLE

            edtxt_titulo_ideia.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTituloIdeia)
            edtxt_descricao_ideia.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoIdeia)
        }

        txt_associar_topicos_ideia.setOnClickListener {
            val tituloAtual = edtxt_titulo_ideia.text.toString()
            val descricaoAtual = edtxt_descricao_ideia.text.toString()

            if (tituloAtual != GlobalVariables.detalhesTituloIdeia || descricaoAtual != GlobalVariables.detalhesDescricaoIdeia) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage("Ao mudar de janela para associar os tópicos à ideia atual, as alterações não guardadas serão descartadas. Deseja continuar?")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("Sim") { dialog, _ ->
                    dialog.dismiss()
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentVerTopicosIdeias())
                    fragment.commit()
                }
                builder.setNegativeButton("Não") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerTopicosIdeias())
                fragment.commit()
            }
        }

        btn_criar_ideia.setOnClickListener {
            if (edtxt_titulo_ideia.text.toString().isNotBlank()) {
                strTituloIdeia = edtxt_titulo_ideia.text.toString()
            } else {
                msgErrors += "O campo \'Título da ideia\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_ideia.text.toString().isNotBlank()) {
                strDescricaoIdeia = edtxt_descricao_ideia.text.toString()
            } else {
                msgErrors += "O campo \'Descrição da ideia\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarIdeia) {
                        postIdeia(
                            requireContext(),
                            strTituloIdeia,
                            GlobalVariables.idUtilizadorAutenticado,
                            strDescricaoIdeia
                        )
                    } else {
                        putIdeia(
                            requireContext(),
                            strTituloIdeia,
                            strDescricaoIdeia
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
            if (GlobalVariables.criarIdeia) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesIdeia())
                fragment.commit()
            }
        }
    }

    private fun postIdeia(
        context: Context,
        titulo: String,
        numUtilizador: Int,
        descricao: String
    ) {
        data class Post(
            val Titulo: String,
            val NUsuario: Int,
            val Estado: String,
            val Descricao: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, numUtilizador, "Pendente", descricao)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/ideias")
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
                            val NIdeia = message.getInt("NIdeia")
                            val NUsuario = message.getInt("NUsuario")
                            var Titulo = message.getString("Titulo")
                            var Data = message.getString("Data")
                            var Estado = message.getString("Estado")
                            var Descricao = message.getString("Descricao")
                            var NomeUsuario = message.getString("NomeUsuario")

                            if (Titulo.isNullOrBlank() || Titulo == "null") {
                                Titulo = ""
                            }
                            Data = if (Data.isNullOrBlank() || Data == "null") {
                                ""
                            } else {
                                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val dateTime = LocalDateTime.parse(Data, inputFormatter)
                                dateTime.format(outputFormatter)
                            }
                            if (Estado.isNullOrBlank() || Estado == "null") {
                                Estado = ""
                            }
                            if (Descricao.isNullOrBlank() || Descricao == "null") {
                                Descricao = ""
                            }
                            if (NomeUsuario.isNullOrBlank() || NomeUsuario == "null") {
                                NomeUsuario = ""
                            }

                            GlobalVariables.detalhesNumIdeia = NIdeia
                            GlobalVariables.detalhesNumUsuarioIdeia = NUsuario
                            GlobalVariables.detalhesTituloIdeia = Titulo
                            GlobalVariables.detalhesDataIdeia = Data
                            GlobalVariables.detalhesEstadoIdeia = Estado
                            GlobalVariables.detalhesDescricaoIdeia = Descricao
                            GlobalVariables.detalhesNomeUsuarioIdeia = NomeUsuario

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A ideia \'$Titulo\' foi criada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerTopicosIdeias())
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

    private fun putIdeia(context: Context, titulo: String, descricao: String) {
        data class Post(val Titulo: String, val Descricao: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, descricao)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/ideias/" + GlobalVariables.detalhesNumIdeia)
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
                            builder.setMessage("A ideia \'$titulo\' foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerIdeias())
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