package com.example.mobilepint.utilizadores

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.administracao.FragmentDetalhesReuniaoAdmin
import com.example.mobilepint.oportunidades.FragmentDetalhesReuniaoOportunidade
import com.example.mobilepint.vagas.FragmentDetalhesReuniaoEntrevista
import com.example.mobilepint.vagas.FragmentVerVagasDisponiveis
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerUtilizadoresReuniao : Fragment() {

    data class ItemsUtilizadoresAtuaisReuniao(val NUsuario: Int, val NomeUsuario: String)

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var btn_guardar: Button
    private lateinit var utilizadoresAtuaisReuniaoList: ArrayList<ItemsUtilizadoresAtuaisReuniao>
    private lateinit var utilizadoresAdmissiveisList: ArrayList<ItemsUtilizadores>
    private lateinit var utilizadoresReuniaoAdapter: AdapterUtilizadoresReuniao
    private lateinit var utilizadoresReuniaoList: ArrayList<ItemsUtilizadoresReuniao>
    private var globalVariables = GlobalVariables()
    private var loopCounter: Int = 0
    private var postCounter: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_utilizadores_reuniao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.utilizadores_reuniao_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        btn_guardar = view.findViewById(R.id.utilizadores_reuniao_btn_guardar)

        utilizadoresAtuaisReuniaoList = ArrayList()
        utilizadoresAdmissiveisList = ArrayList()
        utilizadoresReuniaoList = ArrayList()
        utilizadoresReuniaoAdapter = AdapterUtilizadoresReuniao(requireContext(), utilizadoresReuniaoList)

        if (globalVariables.checkForInternet(requireContext())) {
            Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show()
            getUtilizadoresAtuaisReuniao(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        btn_guardar.setOnClickListener {
            btn_guardar.isEnabled = false
            Handler().postDelayed({
                btn_guardar.isEnabled = true
            }, 10000)

            for (item in utilizadoresReuniaoList) {
                if (item.IsChecked) {
                    loopCounter++
                }
            }

            if (loopCounter == 0) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage("Não foi selecionado nenhum utilizador novo para a reunião.")
                builder.setIcon(R.drawable.ic_information)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    verDetalhesReuniao(GlobalVariables.detalhesTipoReuniao)
                }
                builder.show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    for (item in utilizadoresReuniaoList) {
                        if (item.IsChecked && !utilizadoresAtuaisReuniaoList.any { it.NUsuario == item.NUsuario }) {
                            postUtilizadoresReuniao(requireContext(), item.NUsuario, GlobalVariables.detalhesNumReuniao)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            verDetalhesReuniao(GlobalVariables.detalhesTipoReuniao)
        }
    }

    private fun verDetalhesReuniao(tipoReuniao: Int) {
        when (tipoReuniao) {
            0 -> { // Entrevistas
                if (GlobalVariables.verReuniaoAdministracao) {
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoAdmin())
                    fragment.commit()
                } else {
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoEntrevista())
                    fragment.commit()
                }
            }

            1 -> { // Oportunidades
                if (GlobalVariables.verReuniaoAdministracao) {
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoAdmin())
                    fragment.commit()
                } else {
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoOportunidade())
                    fragment.commit()
                }
            }

            2 -> { // Outros
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoAdmin())
                fragment.commit()
            }

            else -> {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
                fragment.commit()
            }
        }
    }

    private fun JSONObject.getIntOrNull(key: String): Int? {
        return if (has(key) && !isNull(key)) {
            getInt(key)
        } else {
            null
        }
    }

    private fun getUtilizadoresAtuaisReuniao(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarioreunioes?nreuniao=" + GlobalVariables.detalhesNumReuniao)
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
                                val get = jsonArray.getJSONObject(i)
                                val NUsuario = get.getInt("NUsuario")
                                val NReunioes = get.getInt("NReunioes")
                                val NUsuarioCriador = get.getInt("NUsuarioCriador")
                                val Titulo = get.getString("Titulo")
                                val Descricao = get.getString("Descricao")
                                val Tipo = get.getInt("Tipo")
                                val DataHoraInicio = get.getString("DataHoraInicio")
                                val DataHoraFim = get.getString("DataHoraFim")
                                val NOportunidade = get.getIntOrNull("NOportunidade")
                                val NEntrevista = get.getIntOrNull("NEntrevista")
                                val DataHoraNotificacao = get.getString("DataHoraNotificacao")
                                val NotificacaoEnviada = get.getInt("NotificacaoEnviada")
                                val NomeUsuario = get.getString("NomeUsuario")
                                val NomeUsuarioCriador = get.getString("NomeUsuarioCriador")

                                utilizadoresAtuaisReuniaoList.add(
                                    ItemsUtilizadoresAtuaisReuniao(
                                        NUsuario,
                                        NomeUsuario
                                    )
                                )
                            }

                            if (globalVariables.checkForInternet(context)) {
                                getUtilizadoresAdmissiveis(context)
                            } else {
                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
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

    private fun getUtilizadoresAdmissiveis(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios")
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
                                val NUsuario = message.getInt("NUsuario")
                                val NCargo = message.getInt("NCargo")
                                val Estado = message.getInt("Estado")
                                var Nome = message.getString("Nome")
                                var Email = message.getString("Email")
                                var Telefone = message.getString("Telefone")
                                var Linkedin = message.getString("Linkedin")
                                var CV = message.getString("CV")
                                var Foto = message.getString("Foto")
                                var DataNascimento = message.getString("DataNascimento")
                                var Genero = message.getString("Genero")
                                var Localidade = message.getString("Localidade")
                                var DataHoraRegisto = message.getString("DataHoraRegisto")

                                if (Nome.isNullOrBlank() || Nome == "null") {
                                    Nome = ""
                                }
                                if (Email.isNullOrBlank() || Email == "null") {
                                    Email = ""
                                }
                                if (Telefone.isNullOrBlank() || Telefone == "null") {
                                    Telefone = ""
                                }
                                if (Linkedin.isNullOrBlank() || Linkedin == "null") {
                                    Linkedin = ""
                                }
                                if (CV.isNullOrBlank() || CV == "null") {
                                    CV = ""
                                }
                                if (Foto.isNullOrBlank() || Foto == "null") {
                                    Foto = ""
                                }
                                if (DataNascimento.isNullOrBlank() || DataNascimento == "null") {
                                    DataNascimento = ""
                                }
                                if (Genero.isNullOrBlank() || Genero == "null") {
                                    Genero = ""
                                }
                                if (Localidade.isNullOrBlank() || Localidade == "null") {
                                    Localidade = ""
                                }
                                if (DataHoraRegisto.isNullOrBlank() || DataHoraRegisto == "null") {
                                    DataHoraRegisto = ""
                                }

                                if (GlobalVariables.detalhesTipoReuniao == 0) { // Entrevistas
                                    if (NCargo == 0 || NCargo == 5) {
                                        utilizadoresAdmissiveisList.add(
                                            ItemsUtilizadores(
                                                NUsuario,
                                                Nome,
                                                Email,
                                                NCargo,
                                                Telefone,
                                                Linkedin,
                                                CV,
                                                Foto,
                                                DataNascimento,
                                                Genero,
                                                Estado,
                                                Localidade,
                                                DataHoraRegisto
                                            )
                                        )
                                    }
                                } else if (GlobalVariables.detalhesTipoReuniao == 1) { // Oportunidades
                                    if (NCargo == 0 || NCargo == 4) {
                                        utilizadoresAdmissiveisList.add(
                                            ItemsUtilizadores(
                                                NUsuario,
                                                Nome,
                                                Email,
                                                NCargo,
                                                Telefone,
                                                Linkedin,
                                                CV,
                                                Foto,
                                                DataNascimento,
                                                Genero,
                                                Estado,
                                                Localidade,
                                                DataHoraRegisto
                                            )
                                        )
                                    }
                                } else if (GlobalVariables.detalhesTipoReuniao == 2) { // Outros
                                    if (NCargo == 0 || NCargo == 4 || NCargo == 5) {
                                        utilizadoresAdmissiveisList.add(
                                            ItemsUtilizadores(
                                                NUsuario,
                                                Nome,
                                                Email,
                                                NCargo,
                                                Telefone,
                                                Linkedin,
                                                CV,
                                                Foto,
                                                DataNascimento,
                                                Genero,
                                                Estado,
                                                Localidade,
                                                DataHoraRegisto
                                            )
                                        )
                                    }
                                } else {
                                    utilizadoresAdmissiveisList.add(
                                        ItemsUtilizadores(
                                            NUsuario,
                                            Nome,
                                            Email,
                                            NCargo,
                                            Telefone,
                                            Linkedin,
                                            CV,
                                            Foto,
                                            DataNascimento,
                                            Genero,
                                            Estado,
                                            Localidade,
                                            DataHoraRegisto
                                        )
                                    )
                                }
                            }

                            var checkCount = 0
                            for (admisiveisItem in utilizadoresAdmissiveisList) {
                                val matchingItem = utilizadoresAtuaisReuniaoList.find { it.NUsuario == admisiveisItem.NUsuario }
                                val isChecked = matchingItem != null
                                if (isChecked) {
                                    checkCount++
                                }
                                val utilizadorReuniao = ItemsUtilizadoresReuniao(admisiveisItem.NUsuario, admisiveisItem.Nome, isChecked)
                                utilizadoresReuniaoList.add(utilizadorReuniao)
                            }

                            loopCounter = 0
                            postCounter = 0
                            loopCounter -= checkCount

                            utilizadoresReuniaoAdapter = AdapterUtilizadoresReuniao(requireContext(), utilizadoresReuniaoList)
                            recyclerView.adapter = utilizadoresReuniaoAdapter
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

    private fun postUtilizadoresReuniao(context: Context, numUtilizador: Int, numReuniao: Int) {
        data class Post(val NUsuario: Int, val NReunioes: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numUtilizador, numReuniao)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarioreunioes")
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
                            val NUsuario = message.getInt("NUsuario")
                            val NReunioes = message.getInt("NReunioes")
                            val NUsuarioCriador = message.getInt("NUsuarioCriador")
                            val Titulo = message.getString("Titulo")
                            val Descricao = message.getString("Descricao")
                            val Tipo = message.getInt("Tipo")
                            val DataHoraInicio = message.getString("DataHoraInicio")
                            val DataHoraFim = message.getString("DataHoraFim")
                            val NOportunidade = message.getIntOrNull("NOportunidade")
                            val NEntrevista = message.getIntOrNull("NEntrevista")
                            val DataHoraNotificacao = message.getString("DataHoraNotificacao")
                            val NotificacaoEnviada = message.getInt("NotificacaoEnviada")
                            val NomeUsuario = message.getString("NomeUsuario")
                            val NomeUsuarioCriador = message.getString("NomeUsuarioCriador")

                            postCounter++
                            println("O utilizador \'$NomeUsuario\' foi adicionado à lista de participantes da reunião \'${GlobalVariables.detalhesTituloReuniao}\' com sucesso.")

                            if (postCounter == loopCounter) {
                                val detalhes = if (postCounter > 1) {
                                    "Os utilizadores selecionados foram foram adicionados à lista de participantes da reunião \'${GlobalVariables.detalhesTituloReuniao}\' com sucesso."
                                } else {
                                    "O utilizador \'$NomeUsuario\' foi adicionado à lista de participantes da reunião \'${GlobalVariables.detalhesTituloReuniao}\' com sucesso."
                                }
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Aviso")
                                builder.setMessage(detalhes)
                                builder.setIcon(R.drawable.ic_information)
                                builder.setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                    verDetalhesReuniao(GlobalVariables.detalhesTipoReuniao)
                                }
                                builder.show()
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