package com.example.mobilepint.vagas

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.administracao.ItemsLocalidades
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

class FragmentCriarVaga : Fragment() {
    data class ItemsTipoVagas(val NTipoVaga: Int, val NomeTipoVaga: String)

    private lateinit var spinner_localidades_vaga: Spinner
    private lateinit var spinner_tipo_vagas: Spinner
    private lateinit var localidadesList: ArrayList<ItemsLocalidades>
    private lateinit var tipoVagasList: ArrayList<ItemsTipoVagas>
    private var globalVariables = GlobalVariables()
    private var positionLocalidades: Int = 0
    private var positionTipoVagas: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_vaga, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strNomeVaga = ""
        var strSubtituloVaga = ""
        var strDescricaoVaga = ""
        var msgErrors = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_vaga_txt_titulo_janela) as TextView
        val edtxt_nome_vaga = view.findViewById(R.id.criar_vaga_edtxt_nome_vaga) as EditText
        val edtxt_subtitulo_vaga = view.findViewById(R.id.criar_vaga_edtxt_subtitulo_vaga) as EditText
        spinner_localidades_vaga = view.findViewById(R.id.criar_vaga_spinner_localidades_vaga) as Spinner
        spinner_tipo_vagas = view.findViewById(R.id.criar_vaga_spinner_tipo_vagas) as Spinner
        val edtxt_descricao_perfil = view.findViewById(R.id.criar_vaga_edtxt_descricao_perfil_vaga) as EditText
        val btn_criar_vaga = view.findViewById(R.id.criar_vaga_btn_criar_vaga) as Button

        spinner_localidades_vaga.isEnabled = false
        spinner_tipo_vagas.isEnabled = false
        localidadesList = ArrayList()
        tipoVagasList = ArrayList()

        if (GlobalVariables.criarVaga) {
            txt_titulo_janela.text = getString(R.string.str_criar_vaga)
            btn_criar_vaga.text = getString(R.string.str_criar_vaga)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_vaga)
            btn_criar_vaga.text = getString(R.string.str_editar_vaga)

            edtxt_nome_vaga.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesNomeVaga)
            edtxt_subtitulo_vaga.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesSubtituloVaga)
            edtxt_descricao_perfil.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoVaga)
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getLocalidades(requireContext())
            getTipoVagas(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        spinner_localidades_vaga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionLocalidades = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner_tipo_vagas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionTipoVagas = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btn_criar_vaga.setOnClickListener {
            btn_criar_vaga.isEnabled = false
            Handler().postDelayed({
                btn_criar_vaga.isEnabled = true
            }, 10000)

            if (edtxt_nome_vaga.text.toString().isNotBlank()) {
                strNomeVaga = edtxt_nome_vaga.text.toString()
            } else {
                msgErrors += "O campo \'Nome da vaga\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_subtitulo_vaga.text.toString().isNotBlank()) {
                strSubtituloVaga = edtxt_subtitulo_vaga.text.toString()
            } else {
                msgErrors += "O campo \'Subtítulo da vaga\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_perfil.text.toString().isNotBlank()) {
                strDescricaoVaga = edtxt_descricao_perfil.text.toString()
            } else {
                msgErrors += "O campo \'Descrição do perfil\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                val nLocalidade = if (localidadesList.isNotEmpty()) localidadesList[positionLocalidades].NLocalidade else -1
                val nTipoVaga = if (tipoVagasList.isNotEmpty()) tipoVagasList[positionTipoVagas].NTipoVaga else -1

                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarVaga) {
                        postVaga(
                            requireContext(),
                            strNomeVaga,
                            strSubtituloVaga,
                            strDescricaoVaga,
                            nLocalidade,
                            nTipoVaga
                        )
                    } else {
                        putVaga(
                            requireContext(),
                            strNomeVaga,
                            strSubtituloVaga,
                            strDescricaoVaga,
                            nLocalidade,
                            nTipoVaga
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
            if (GlobalVariables.criarVaga) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentGerirVagas())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesVaga())
                fragment.commit()
            }
        }
    }

    private fun getLocalidades(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/localidades")
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
                                val NLocalidade = get.getInt("NLocalidade")
                                val Localidade = get.getString("Localidade")
                                localidadesList.add(ItemsLocalidades(NLocalidade, Localidade))
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                localidadesList.map { it.Localidade }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_localidades_vaga.adapter = adapter
                            spinner_localidades_vaga.isEnabled = localidadesList.isNotEmpty()

                            if (!GlobalVariables.criarVaga) {
                                positionLocalidades = localidadesList.indexOfFirst { it.NLocalidade == GlobalVariables.detalhesNumLocalidadeVaga }
                                if (positionLocalidades != -1) {
                                    try {
                                        spinner_localidades_vaga.setSelection(positionLocalidades)
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
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

    private fun getTipoVagas(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/tipovagas")
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
                                val NTipoVaga = get.getInt("NTipoVaga")
                                val NomeTipoVaga = get.getString("NomeTipoVaga")
                                tipoVagasList.add(ItemsTipoVagas(NTipoVaga, NomeTipoVaga))
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                tipoVagasList.map { it.NomeTipoVaga }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_tipo_vagas.adapter = adapter
                            spinner_tipo_vagas.isEnabled = tipoVagasList.isNotEmpty()

                            if (!GlobalVariables.criarVaga) {
                                positionTipoVagas = tipoVagasList.indexOfFirst { it.NTipoVaga == GlobalVariables.detalhesNumTipoVaga }
                                if (positionTipoVagas != -1) {
                                    try {
                                        spinner_tipo_vagas.setSelection(positionTipoVagas)
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
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

    private fun postVaga(
        context: Context,
        nome: String,
        subtitulo: String,
        descricao: String,
        numLocalidade: Int,
        numTipoVaga: Int
    ) {
        data class Post(
            val NomeVaga: String,
            val Subtitulo: String,
            val Descricao: String,
            val NLocalidade: Int,
            val NTipoVaga: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, subtitulo, descricao, numLocalidade, numTipoVaga)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/vagas")
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
                            val NVaga = message.getInt("NVaga")
                            val NomeVaga = message.getString("NomeVaga")
                            val Subtitulo = message.getString("Subtitulo")
                            val Descricao = message.getString("Descricao")
                            val NLocalidade = message.getInt("NLocalidade")
                            val NTipoVaga = message.getInt("NTipoVaga")
                            val Localidade = message.getString("Localidade")
                            val TipoVaga = message.getString("TipoVaga")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A vaga \'$NomeVaga\' foi criada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentGerirVagas())
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

    private fun putVaga(
        context: Context,
        nome: String,
        subtitulo: String,
        descricao: String,
        numLocalidade: Int,
        numTipoVaga: Int
    ) {
        data class Post(
            val NomeVaga: String,
            val Subtitulo: String,
            val Descricao: String,
            val NLocalidade: Int,
            val NTipoVaga: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, subtitulo, descricao, numLocalidade, numTipoVaga)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/vagas/" + GlobalVariables.detalhesNumVaga)
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
                            builder.setMessage("A vaga \'$nome\' foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentGerirVagas())
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