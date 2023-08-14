package com.example.mobilepint.oportunidades

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

class FragmentCriarOportunidade : Fragment() {

    private lateinit var txt_titulo_janela: TextView
    private lateinit var edtxt_titulo_negocio: EditText
    private lateinit var edtxt_valor_negocio: EditText
    private lateinit var edtxt_descricao_negocio: EditText
    private lateinit var spinner_clientes_negocio: Spinner
    private lateinit var spinner_etiquetas_negocio: Spinner
    private lateinit var spinner_tipos_projetos_negocio: Spinner
    private lateinit var spinner_estagios_negocio: Spinner
    private lateinit var btn_criar_negocio: Button
    private lateinit var clientesList: ArrayList<ItemsClientes>
    private lateinit var etiquetasList: ArrayList<ItemsEtiquetas>
    private lateinit var tiposProjetosList: ArrayList<ItemsTiposProjetos>
    private lateinit var estagiosList: ArrayList<ItemsEstagios>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false
    private var positionClientes: Int = 0
    private var positionEtiquetas: Int = 0
    private var positionTiposProjetos: Int = 0
    private var positionEstagios: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_oportunidade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strTituloNegocio = ""
        var strValorNegocio = ""
        var strDescricaoNegocio = ""
        var msgErrors = ""

        txt_titulo_janela = view.findViewById(R.id.criar_negocio_txt_titulo_janela)
        edtxt_titulo_negocio = view.findViewById(R.id.criar_negocio_edtxt_titulo_negocio)
        edtxt_valor_negocio = view.findViewById(R.id.criar_negocio_edtxt_valor_negocio)
        edtxt_descricao_negocio = view.findViewById(R.id.criar_negocio_edtxt_descricao_negocio)
        spinner_clientes_negocio = view.findViewById(R.id.criar_negocio_spinner_clientes_negocio)
        spinner_etiquetas_negocio = view.findViewById(R.id.criar_negocio_spinner_etiquetas_negocio)
        spinner_tipos_projetos_negocio = view.findViewById(R.id.criar_negocio_spinner_tipos_projetos_negocio)
        spinner_estagios_negocio = view.findViewById(R.id.criar_negocio_spinner_estagios_negocio)
        btn_criar_negocio = view.findViewById(R.id.criar_negocio_btn_criar_negocio)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo)

        clientesList = ArrayList()
        etiquetasList = ArrayList()
        tiposProjetosList = ArrayList()
        estagiosList = ArrayList()

        if (GlobalVariables.criarOportunidade) {
            txt_titulo_janela.text = getString(R.string.str_criar_negocio)
            btn_criar_negocio.text = getString(R.string.str_criar_negocio)

            spinner_tipos_projetos_negocio.isEnabled = true
            spinner_etiquetas_negocio.isEnabled = true
            spinner_clientes_negocio.isEnabled = true
            spinner_estagios_negocio.isEnabled = false
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_negocio)
            btn_criar_negocio.text = getString(R.string.str_editar_negocio)

            spinner_tipos_projetos_negocio.isEnabled = true
            spinner_etiquetas_negocio.isEnabled = true
            spinner_clientes_negocio.isEnabled = true
            spinner_estagios_negocio.isEnabled = havePermissions

            edtxt_titulo_negocio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTituloOportunidade)
            edtxt_valor_negocio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesValorOportunidade)
            edtxt_descricao_negocio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoOportunidade)
        }

        val urlClientes = if (havePermissions) {
            "${GlobalVariables.serverUrl}/api/clientes"
        } else {
            "${GlobalVariables.serverUrl}/api/clientes?nusuario=${GlobalVariables.idUtilizadorAutenticado}"
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getClientes(requireContext(), urlClientes)
            getEtiquetas(requireContext())
            getTiposProjetos(requireContext())
            getEstagios(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        spinner_clientes_negocio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionClientes = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner_etiquetas_negocio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionEtiquetas = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner_tipos_projetos_negocio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionTiposProjetos = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        spinner_estagios_negocio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionEstagios = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btn_criar_negocio.setOnClickListener {
            btn_criar_negocio.isEnabled = false
            Handler().postDelayed({
                btn_criar_negocio.isEnabled = true
            }, 10000)

            if (edtxt_titulo_negocio.text.toString().isNotBlank()) {
                strTituloNegocio = edtxt_titulo_negocio.text.toString()
            } else {
                msgErrors += "O campo \'Título do negócio\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            strValorNegocio = edtxt_valor_negocio.text.toString().ifBlank { "" }

            if (edtxt_descricao_negocio.text.toString().isNotBlank()) {
                strDescricaoNegocio = edtxt_descricao_negocio.text.toString()
            } else {
                msgErrors += "O campo \'Descrição do negócio\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                val nCliente = if (clientesList.isNotEmpty()) {
                    clientesList[positionClientes].NCliente
                } else {
                    -1
                }
                val nEtiqueta = if (etiquetasList.isNotEmpty()) {
                    etiquetasList[positionEtiquetas].NEtiqueta
                } else {
                    -1
                }
                val nTipoProjeto = if (tiposProjetosList.isNotEmpty()) {
                    tiposProjetosList[positionTiposProjetos].NTipoProjeto
                } else {
                    -1
                }
                val nEstagio = if (estagiosList.isNotEmpty()) {
                    estagiosList[positionEstagios].NEstagio
                } else {
                    -1
                }

                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarOportunidade) {
                        postOportunidade(
                            requireContext(),
                            strTituloNegocio,
                            strValorNegocio,
                            strDescricaoNegocio,
                            nEtiqueta,
                            nEstagio,
                            nCliente,
                            GlobalVariables.idUtilizadorAutenticado,
                            nTipoProjeto
                        )
                    } else {
                        putOportunidade(
                            requireContext(),
                            strTituloNegocio,
                            strValorNegocio,
                            strDescricaoNegocio,
                            nEtiqueta,
                            nEstagio,
                            nCliente,
                            nTipoProjeto
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
            if (GlobalVariables.criarOportunidade) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesOportunidade())
                fragment.commit()
            }
        }
    }

    private fun getClientes(context: Context, url: String) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(url)
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
                                val NCliente = get.getInt("NCliente")
                                val NomeEmp = get.getString("NomeEmp")
                                val EmailEmp = get.getString("EmailEmp")
                                val TelefoneEmp = get.getString("TelefoneEmp")
                                val Descricao = get.getString("Descricao")
                                val NUsuarioCriador = get.getInt("NUsuarioCriador")
                                val DataHoraCriacao = get.getString("DataHoraCriacao")
                                val NomeUsuarioCriador = get.getString("NomeUsuarioCriador")
                                clientesList.add(
                                    ItemsClientes(
                                        NCliente,
                                        NomeEmp,
                                        EmailEmp,
                                        TelefoneEmp,
                                        Descricao,
                                        NUsuarioCriador,
                                        DataHoraCriacao,
                                        NomeUsuarioCriador
                                    )
                                )
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                clientesList.map { it.NomeEmp }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_clientes_negocio.adapter = adapter

                            if (!GlobalVariables.criarOportunidade) {
                                positionClientes = clientesList.indexOfFirst { it.NCliente == GlobalVariables.detalhesNumCliente }
                                if (positionClientes != -1) {
                                    try {
                                        spinner_clientes_negocio.setSelection(positionClientes)
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

    private fun getEtiquetas(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/etiquetas")
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
                                val NEtiqueta = get.getInt("NEtiqueta")
                                val Nome = get.getString("Nome")
                                etiquetasList.add(ItemsEtiquetas(NEtiqueta, Nome))
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                etiquetasList.map { it.Nome }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_etiquetas_negocio.adapter = adapter
                            spinner_etiquetas_negocio.isEnabled = etiquetasList.isNotEmpty()

                            if (!GlobalVariables.criarOportunidade) {
                                positionEtiquetas = etiquetasList.indexOfFirst { it.NEtiqueta == GlobalVariables.detalhesNumEtiquetaOportunidade }
                                if (positionEtiquetas != -1) {
                                    try {
                                        spinner_etiquetas_negocio.setSelection(positionEtiquetas)
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

    private fun getTiposProjetos(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/tipoprojetos")
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
                                val NTipoProjeto = get.getInt("NTipoProjeto")
                                val Nome = get.getString("Nome")
                                tiposProjetosList.add(ItemsTiposProjetos(NTipoProjeto, Nome))
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                tiposProjetosList.map { it.Nome }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_tipos_projetos_negocio.adapter = adapter
                            spinner_tipos_projetos_negocio.isEnabled = tiposProjetosList.isNotEmpty()

                            if (!GlobalVariables.criarOportunidade) {
                                positionTiposProjetos = tiposProjetosList.indexOfFirst { it.NTipoProjeto == GlobalVariables.detalhesNumTipoProjetoOportunidade }
                                if (positionTiposProjetos != -1) {
                                    try {
                                        spinner_tipos_projetos_negocio.setSelection(positionTiposProjetos)
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

    private fun getEstagios(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/estagios")
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
                                val NEstagio = get.getInt("NEstagio")
                                val Nome = get.getString("Nome")
                                estagiosList.add(ItemsEstagios(NEstagio, Nome))
                            }

                            val adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                estagiosList.map { it.Nome }.toTypedArray()
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_estagios_negocio.adapter = adapter

                            if (!GlobalVariables.criarOportunidade) {
                                if (havePermissions) {
                                    spinner_estagios_negocio.isEnabled = estagiosList.isNotEmpty()
                                } else {
                                    spinner_estagios_negocio.isEnabled = false
                                }

                                positionEstagios = estagiosList.indexOfFirst { it.NEstagio == GlobalVariables.detalhesNumEstagioOportunidade }
                                if (positionEstagios != -1) {
                                    try {
                                        spinner_estagios_negocio.setSelection(positionEstagios)
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

    private fun postOportunidade(
        context: Context,
        titulo: String,
        valor: String,
        descricao: String,
        numEtiqueta: Int,
        numEstagio: Int,
        numCliente: Int,
        numUsuario: Int,
        numTipoProjeto: Int
    ) {
        data class Post(
            val Titulo: String,
            val Valor: String,
            val Descricao: String,
            val NEtiqueta: Int,
            val NEstagio: Int,
            val NCliente: Int,
            val NUsuario: Int,
            val NTipoProjeto: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(
            titulo,
            valor,
            descricao,
            numEtiqueta,
            numEstagio,
            numCliente,
            numUsuario,
            numTipoProjeto
        )
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/oportunidades")
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
                            val NOportunidade = message.getInt("NOportunidade")
                            val Titulo = message.getString("Titulo")
                            val Valor = message.getString("Valor")
                            val Descricao = message.getString("Descricao")
                            val NEtiqueta = message.getInt("NEtiqueta")
                            val NEstagio = message.getInt("NEstagio")
                            val NCliente = message.getInt("NCliente")
                            val NUsuario = message.getInt("NUsuario")
                            val NTipoProjeto = message.getInt("NTipoProjeto")
                            val DataHoraCriacao = message.getString("DataHoraCriacao")
                            val NomeEtiqueta = message.getString("NomeEtiqueta")
                            val NomeEstagio = message.getString("NomeEstagio")
                            val NomeCliente = message.getString("NomeCliente")
                            val NomeUsuarioCriador = message.getString("NomeUsuarioCriador")
                            val TipoProjeto = message.getString("TipoProjeto")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A oportunidade \'$Titulo\' foi criada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
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

    private fun putOportunidade(
        context: Context,
        titulo: String,
        valor: String,
        descricao: String,
        numEtiqueta: Int,
        numEstagio: Int,
        numCliente: Int,
        numTipoProjeto: Int
    ) {
        data class Post(
            val Titulo: String,
            val Valor: String,
            val Descricao: String,
            val NEtiqueta: Int,
            val NEstagio: Int,
            val NCliente: Int,
            val NTipoProjeto: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, valor, descricao, numEtiqueta, numEstagio, numCliente, numTipoProjeto)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/oportunidades/" + GlobalVariables.detalhesNumOportunidade)
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
                            builder.setMessage("A oportunidade \'$titulo\' foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
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