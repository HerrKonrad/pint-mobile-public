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
import com.example.mobilepint.ItemsUtilizadoresReunioes
import com.example.mobilepint.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerReunioesOportunidades : Fragment() {

    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var utilizadoresReunioesList: ArrayList<ItemsUtilizadoresReunioes>
    private lateinit var reunioesOportunidadesAdapter: AdapterReunioesOportunidades
    private lateinit var reunioesOportunidadesList: ArrayList<ItemsReunioesOportunidades>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_reunioes_oportunidades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.reunioes_oportunidades_imgbtn_view_more)
        searchView = view.findViewById(R.id.reunioes_oportunidades_searchView)
        recyclerView = view.findViewById(R.id.reunioes_oportunidades_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        utilizadoresReunioesList = ArrayList()
        reunioesOportunidadesList = ArrayList()
        reunioesOportunidadesAdapter = AdapterReunioesOportunidades(requireContext(), reunioesOportunidadesList)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo)

        if (globalVariables.checkForInternet(requireContext())) {
            getReunioesUtilizador(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Título"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    reunioesOportunidadesAdapter.filter.filter(newText)
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
                "Visualizar estágios"
            )
            val builderOptions = android.app.AlertDialog.Builder(requireContext())
            builderOptions.setTitle("Escolha uma opção:")
            builderOptions.setItems(options) { dialogOptions, which ->
                when (which) {
                    0 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
                        fragment.commit()
                    }

                    1 -> {
                        dialogOptions.dismiss()
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
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
                        GlobalVariables.verEtiquetas = false
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
                        fragment.commit()
                    }
                }
            }
            builderOptions.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
            fragment.commit()
        }
    }

    private fun getReunioesUtilizador(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarioreunioes?nusuario=" + GlobalVariables.idUtilizadorAutenticado)
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
                                val NReunioes = message.getInt("NReunioes")

                                utilizadoresReunioesList.add(
                                    ItemsUtilizadoresReunioes(
                                        NUsuario,
                                        NReunioes
                                    )
                                )
                            }

                            if (globalVariables.checkForInternet(context)) {
                                getReunioesOportunidades(context)
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

    private fun getReunioesOportunidades(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/reunioes?tipo=1")
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
                                val NReunioes = message.getInt("NReunioes")
                                val NUsuarioCriador = message.getInt("NUsuarioCriador")
                                val Tipo = message.getInt("Tipo")
                                val NOportunidade = message.getInt("NOportunidade")
                                var Titulo = message.getString("Titulo")
                                var Descricao = message.getString("Descricao")
                                var DataHoraInicio = message.getString("DataHoraInicio")
                                var DataHoraFim = message.getString("DataHoraFim")
                                var DataHoraNotificacao  = message.getString("DataHoraNotificacao")
                                var NomeUsuarioCriador = message.getString("NomeUsuarioCriador")

                                if (Titulo.isNullOrBlank() || Titulo == "null") {
                                    Titulo = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (DataHoraInicio.isNullOrBlank() || DataHoraInicio == "null") {
                                    DataHoraInicio = ""
                                }
                                if (DataHoraFim.isNullOrBlank() || DataHoraFim == "null") {
                                    DataHoraFim = ""
                                }
                                if (DataHoraNotificacao.isNullOrBlank() || DataHoraNotificacao == "null") {
                                    DataHoraNotificacao = ""
                                }
                                if (NomeUsuarioCriador.isNullOrBlank() || NomeUsuarioCriador == "null") {
                                    NomeUsuarioCriador = ""
                                }

                                if (havePermissions) {
                                    reunioesOportunidadesList.add(
                                        ItemsReunioesOportunidades(
                                            NReunioes,
                                            NUsuarioCriador,
                                            Titulo,
                                            Descricao,
                                            Tipo,
                                            DataHoraInicio,
                                            DataHoraFim,
                                            NOportunidade,
                                            DataHoraNotificacao,
                                            NomeUsuarioCriador
                                        )
                                    )
                                } else {
                                    if (utilizadoresReunioesList.any { it.NReunioes == NReunioes }) {
                                        reunioesOportunidadesList.add(
                                            ItemsReunioesOportunidades(
                                                NReunioes,
                                                NUsuarioCriador,
                                                Titulo,
                                                Descricao,
                                                Tipo,
                                                DataHoraInicio,
                                                DataHoraFim,
                                                NOportunidade,
                                                DataHoraNotificacao,
                                                NomeUsuarioCriador
                                            )
                                        )
                                    }
                                }
                            }

                            reunioesOportunidadesAdapter = AdapterReunioesOportunidades(context, reunioesOportunidadesList)
                            recyclerView.adapter = reunioesOportunidadesAdapter

                            if (reunioesOportunidadesList.isEmpty()) {
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
}