package com.example.mobilepint.ideias

import android.app.AlertDialog
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
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
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

class FragmentDetalhesIdeia : Fragment() {

    private lateinit var txt_topicos_ideia_label: TextView
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false
    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_ideia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_IDEIAS.numCargo)

        val txt_titulo_ideia_label = view.findViewById(R.id.detalhes_ideia_txt_titulo_ideia_label) as TextView
        val txt_data_criacao_label = view.findViewById(R.id.detalhes_ideia_txt_data_criacao_label) as TextView
        val txt_autor_ideia_label = view.findViewById(R.id.detalhes_ideia_txt_autor_ideia_label) as TextView
        val txt_estado_ideia_label = view.findViewById(R.id.detalhes_ideia_txt_estado_ideia_label) as TextView
        val txt_descricao_ideia_label = view.findViewById(R.id.detalhes_ideia_txt_descricao_ideia_label) as TextView
        txt_topicos_ideia_label = view.findViewById(R.id.detalhes_ideia_txt_topicos_ideia_label)
        val layout_btns_permissoes = view.findViewById(R.id.detalhes_ideia_linearLayout_btns_permissoes) as LinearLayout
        val btn_editar_ideia = view.findViewById(R.id.detalhes_ideia_btn_editar_ideia) as Button
        val btn_aceitar_ideia = view.findViewById(R.id.detalhes_ideia_btn_aceitar_ideia) as Button
        val btn_rejeitar_ideia = view.findViewById(R.id.detalhes_ideia_btn_rejeitar_ideia) as Button
        val btn_arquivar_ideia = view.findViewById(R.id.detalhes_ideia_btn_arquivar_ideia) as Button

        val strFormattedDate = if (GlobalVariables.detalhesDataIdeia.isNotBlank()) {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataIdeia, inputFormatter)
            startDateTime.format(outputFormatter)
        } else {
            ""
        }

        txt_titulo_ideia_label.text = GlobalVariables.detalhesTituloIdeia
        txt_data_criacao_label.text = strFormattedDate
        txt_autor_ideia_label.text = GlobalVariables.detalhesNomeUsuarioIdeia
        txt_estado_ideia_label.text = GlobalVariables.detalhesEstadoIdeia
        txt_descricao_ideia_label.text = GlobalVariables.detalhesDescricaoIdeia
        txt_topicos_ideia_label.text = ""

        if (globalVariables.checkForInternet(requireContext())) {
            getTopicosIdeia(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        if (havePermissions) {
            layout_btns_permissoes.visibility = View.VISIBLE
        } else {
            layout_btns_permissoes.visibility = View.GONE
        }

        btn_editar_ideia.setOnClickListener {
            if (GlobalVariables.detalhesNumUsuarioIdeia == GlobalVariables.idUtilizadorAutenticado) {
                GlobalVariables.criarIdeia = false
                if (GlobalVariables.detalhesEstadoIdeia == "Pendente") {
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarIdeia())
                    fragment.commit()
                } else {
                    val builderMessage = AlertDialog.Builder(requireContext())
                    builderMessage.setTitle("Aviso")
                    builderMessage.setMessage("Apenas as ideias pendentes podem ser editadas.")
                    builderMessage.setIcon(R.drawable.ic_information)
                    builderMessage.setPositiveButton("OK") { dialogMessage, _ ->
                        dialogMessage.dismiss()
                    }
                    builderMessage.show()
                }
            } else {
                val builderMessage = AlertDialog.Builder(requireContext())
                builderMessage.setTitle("Aviso")
                builderMessage.setMessage("Apenas o autor da ideia pode editar a ideia \'${GlobalVariables.detalhesTituloIdeia}\'.")
                builderMessage.setIcon(R.drawable.ic_information)
                builderMessage.setPositiveButton("OK") { dialogMessage, _ ->
                    dialogMessage.dismiss()
                }
                builderMessage.show()
            }
        }

        btn_aceitar_ideia.setOnClickListener {
            if (GlobalVariables.detalhesEstadoIdeia == "Aceite") {
                Toast.makeText(requireContext(), "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi aceite.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Aceitar ideia")
                    builder.setMessage("Tem a certeza que pretende aceitar a ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                        putIdeia(requireContext(), "Aceite")
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

        btn_rejeitar_ideia.setOnClickListener {
            if (GlobalVariables.detalhesEstadoIdeia == "Rejeitada") {
                Toast.makeText(requireContext(), "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi rejeitada.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Rejeitar ideia")
                    builder.setMessage("Tem a certeza que pretende rejeitar a ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                        putIdeia(requireContext(), "Rejeitada")
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

        btn_arquivar_ideia.setOnClickListener {
            if (GlobalVariables.detalhesEstadoIdeia == "Arquivada") {
                Toast.makeText(requireContext(), "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi arquivada.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Arquivar ideia")
                    builder.setMessage("Tem a certeza que pretende arquivar a ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                        putIdeia(requireContext(), "Arquivada")
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
            fragment.replace(R.id.fragment_container, FragmentVerIdeias())
            fragment.commit()
        }
    }

    private fun getTopicosIdeia(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/topicosdasideias?nideia=" + GlobalVariables.detalhesNumIdeia)
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
                            var strTopicosIdeia = ""

                            for (i in 0 until jsonArray.length()) {
                                val message = jsonArray.getJSONObject(i)
                                val NTopicoIdeia = message.getInt("NTopicoIdeia")
                                val NIdeia = message.getInt("NIdeia")
                                val NomeTopico = message.getString("NomeTopico")
                                strTopicosIdeia += "- $NomeTopico\n"
                            }

                            if (strTopicosIdeia.isNotBlank()) {
                                if (strTopicosIdeia.last() == '\n') {
                                    strTopicosIdeia = strTopicosIdeia.trimEnd('\n')
                                }
                            }

                            txt_topicos_ideia_label.text = strTopicosIdeia
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

    private fun putIdeia(context: Context, estado: String) {
        data class Post(val Estado: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(estado)
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

                            val outputMessage = when (estado) {
                                "Aceite" -> {
                                    "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi aceite."
                                }

                                "Rejeitada" -> {
                                    "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi rejeitada."
                                }

                                "Arquivada" -> {
                                    "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi arquivada."
                                }

                                else -> {
                                    "A ideia do utilizador \'${GlobalVariables.detalhesNomeUsuarioIdeia}\' foi alterada com sucesso."
                                }
                            }

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(outputMessage)
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                if (estado == GlobalVariables.detalhesEstadoIdeia) {
                                    val fragment = parentFragmentManager.beginTransaction()
                                    fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                                    fragment.commit()
                                } else {
                                    if (estado == "Aceite" || estado == "Rejeitada") {
                                        val bundle = Bundle()
                                        bundle.putInt("NumIdeia", GlobalVariables.detalhesNumIdeia)
                                        bundle.putString("TituloIdeia", GlobalVariables.detalhesTituloIdeia)
                                        bundle.putString("EstadoIdeia", estado)

                                        val fragmentBundle = FragmentEnviarRelatorioIdeia()
                                        fragmentBundle.arguments = bundle

                                        val fragment = parentFragmentManager.beginTransaction()
                                        fragment.replace(R.id.fragment_container, fragmentBundle)
                                        fragment.commit()
                                    } else {
                                        val fragment = parentFragmentManager.beginTransaction()
                                        fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                                        fragment.commit()
                                    }
                                }
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