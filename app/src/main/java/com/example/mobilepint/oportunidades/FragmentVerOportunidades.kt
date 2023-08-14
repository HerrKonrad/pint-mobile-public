package com.example.mobilepint.oportunidades

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerOportunidades : Fragment() {

    private lateinit var toast: Toast
    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var spinner_estagios: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var fabCreateOportunidade: FloatingActionButton
    private lateinit var clientesList: ArrayList<ItemsClientes>
    private lateinit var estagiosList: ArrayList<ItemsEstagios>
    private lateinit var oportunidadesAdapter: AdapterOportunidades
    private lateinit var oportunidadesList: ArrayList<ItemsOportunidades>
    private var lastBackPressTime: Long = 0
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_oportunidades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.oportunidades_imgbtn_view_more)
        searchView = view.findViewById(R.id.oportunidades_searchView)
        spinner_estagios = view.findViewById(R.id.oportunidades_spinner_estagios)
        recyclerView = view.findViewById(R.id.oportunidades_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateOportunidade = view.findViewById(R.id.oportunidades_fabCreate)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo)
        clientesList = ArrayList()
        estagiosList = ArrayList()
        oportunidadesList = ArrayList()
        oportunidadesAdapter = AdapterOportunidades(requireContext(), oportunidadesList)

        val urlClientes = if (havePermissions) {
            "${GlobalVariables.serverUrl}/api/clientes"
        } else {
            "${GlobalVariables.serverUrl}/api/clientes?nusuario=${GlobalVariables.idUtilizadorAutenticado}"
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getEstagios(requireContext())
            getClientes(requireContext(), urlClientes)
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        spinner_estagios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (estagiosList.isNotEmpty()) {
                    val numEstagio = estagiosList[position].NEstagio
                    val urlOportunidades = if (havePermissions) {
                        "${GlobalVariables.serverUrl}/api/oportunidades?nestagio=${numEstagio}"
                    } else {
                        "${GlobalVariables.serverUrl}/api/oportunidades?nestagio=${numEstagio}&nusuario=${GlobalVariables.idUtilizadorAutenticado}"
                    }

                    if (globalVariables.checkForInternet(requireContext())) {
                        oportunidadesList.clear()
                        getOportunidades(requireContext(), urlOportunidades)
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "A lista de estágios está vazia.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Título"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    oportunidadesAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        imgbtn_view_more.setOnClickListener {
            val options = arrayOf(
                "Visualizar negócios",
                "Visualizar reuniões dos negócios",
                "Visualizar clientes",
                "Visualizar áreas de negócio",
                "Visualizar tipos de projetos",
                "Visualizar estágios"
            )
            val builderOptions = android.app.AlertDialog.Builder(requireContext())
            builderOptions.setTitle("Escolha uma opção:")
            builderOptions.setItems(options) { dialogOptions, which ->
                when (which) {
                    0 -> {
                        dialogOptions.dismiss()
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
                    }

                    1 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerReunioesOportunidades())
                        fragment.commit()
                    }

                    2 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerClientes())
                        fragment.commit()
                    }

                    3 -> {
                        dialogOptions.dismiss()
                        GlobalVariables.verEtiquetas = true
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
                        fragment.commit()
                    }

                    4 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerTiposProjetos())
                        fragment.commit()
                    }

                    5 -> {
                        dialogOptions.dismiss()
                        GlobalVariables.verEtiquetas = false
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
                        fragment.commit()
                    }
                }
            }
            builderOptions.show()
        }

        fabCreateOportunidade.setOnClickListener {
            if (clientesList.isNotEmpty()) {
                GlobalVariables.criarOportunidade = true
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentCriarOportunidade())
                fragment.commit()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage("É necessário ter pelo menos um cliente criado para adicionar uma oportunidade. Por favor, crie primeiro um cliente e depois tente novamente.")
                builder.setIcon(R.drawable.ic_information)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentTime = System.currentTimeMillis()
            if (lastBackPressTime < currentTime - 4000) {
                toast = Toast.makeText(requireContext(), "Pressione novamente para sair.", Toast.LENGTH_LONG)
                toast.show()
                lastBackPressTime = currentTime
            } else {
                toast.cancel()
                requireActivity().finish()
            }
        }
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
                            spinner_estagios.adapter = adapter
                            spinner_estagios.isEnabled = estagiosList.isNotEmpty()

                            if (estagiosList.isEmpty()) {
                                Toast.makeText(requireContext(), "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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

    private fun getOportunidades(context: Context, url: String) {
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
                                val message = jsonArray.getJSONObject(i)
                                val NOportunidade = message.getInt("NOportunidade")
                                val NEtiqueta = message.getInt("NEtiqueta")
                                val NEstagio = message.getInt("NEstagio")
                                val NCliente = message.getInt("NCliente")
                                val NUsuario = message.getInt("NUsuario")
                                val NTipoProjeto = message.getInt("NTipoProjeto")
                                var Titulo = message.getString("Titulo")
                                var Valor = message.getString("Valor")
                                var Descricao = message.getString("Descricao")
                                var DataHoraCriacao = message.getString("DataHoraCriacao")
                                var NomeEtiqueta = message.getString("NomeEtiqueta")
                                var NomeEstagio = message.getString("NomeEstagio")
                                var NomeCliente = message.getString("NomeCliente")
                                var NomeUsuarioCriador = message.getString("NomeUsuarioCriador")
                                var TelefoneCliente = message.getString("TelefoneCliente")
                                var EmailCliente = message.getString("EmailCliente")
                                var TipoProjeto = message.getString("TipoProjeto")

                                if (Titulo.isNullOrBlank() || Titulo == "null") {
                                    Titulo = ""
                                }
                                if (Valor.isNullOrBlank() || Valor == "null") {
                                    Valor = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (DataHoraCriacao.isNullOrBlank() || DataHoraCriacao == "null") {
                                    DataHoraCriacao = ""
                                }
                                if (NomeEtiqueta.isNullOrBlank() || NomeEtiqueta == "null") {
                                    NomeEtiqueta = ""
                                }
                                if (NomeEstagio.isNullOrBlank() || NomeEstagio == "null") {
                                    NomeEstagio = ""
                                }
                                if (NomeCliente.isNullOrBlank() || NomeCliente == "null") {
                                    NomeCliente = ""
                                }
                                if (NomeUsuarioCriador.isNullOrBlank() || NomeUsuarioCriador == "null") {
                                    NomeUsuarioCriador = ""
                                }
                                if (TelefoneCliente.isNullOrBlank() || TelefoneCliente == "null") {
                                    TelefoneCliente = ""
                                }
                                if (EmailCliente.isNullOrBlank() || EmailCliente == "null") {
                                    EmailCliente = ""
                                }
                                if (TipoProjeto.isNullOrBlank() || TipoProjeto == "null") {
                                    TipoProjeto = ""
                                }

                                oportunidadesList.add(
                                    ItemsOportunidades(
                                        NOportunidade,
                                        Titulo,
                                        Valor,
                                        Descricao,
                                        NEtiqueta,
                                        NEstagio,
                                        NCliente,
                                        NUsuario,
                                        NTipoProjeto,
                                        DataHoraCriacao,
                                        NomeEtiqueta,
                                        NomeEstagio,
                                        NomeCliente,
                                        NomeUsuarioCriador,
                                        TelefoneCliente,
                                        EmailCliente,
                                        TipoProjeto
                                    )
                                )
                            }

                            oportunidadesAdapter = AdapterOportunidades(context, oportunidadesList)
                            recyclerView.adapter = oportunidadesAdapter
                            searchView.setQuery("", false)

                            if (oportunidadesList.isEmpty()) {
                                Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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
                                val NUsuarioCriador = get.getInt("NUsuarioCriador")
                                var NomeEmp = get.getString("NomeEmp")
                                var EmailEmp = get.getString("EmailEmp")
                                var TelefoneEmp = get.getString("TelefoneEmp")
                                var Descricao = get.getString("Descricao")
                                var DataHoraCriacao = get.getString("DataHoraCriacao")
                                var NomeUsuarioCriador = get.getString("NomeUsuarioCriador")

                                if (NomeEmp.isNullOrBlank() || NomeEmp == "null") {
                                    NomeEmp = ""
                                }
                                if (EmailEmp.isNullOrBlank() || EmailEmp == "null") {
                                    EmailEmp = ""
                                }
                                if (TelefoneEmp.isNullOrBlank() || TelefoneEmp == "null") {
                                    TelefoneEmp = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (DataHoraCriacao.isNullOrBlank() || DataHoraCriacao == "null") {
                                    DataHoraCriacao = ""
                                }
                                if (NomeUsuarioCriador.isNullOrBlank() || NomeUsuarioCriador == "null") {
                                    NomeUsuarioCriador = ""
                                }

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