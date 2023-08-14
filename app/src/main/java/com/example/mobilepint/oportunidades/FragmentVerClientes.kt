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

class FragmentVerClientes : Fragment() {

    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var fabCreateCliente: FloatingActionButton
    private lateinit var clientesAdapter: AdapterClientes
    private lateinit var clientesList: ArrayList<ItemsClientes>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_clientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.clientes_imgbtn_view_more)
        searchView = view.findViewById(R.id.clientes_searchView)
        recyclerView = view.findViewById(R.id.clientes_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateCliente = view.findViewById(R.id.clientes_fabCreate)
        clientesList = ArrayList()
        clientesAdapter = AdapterClientes(requireContext(), clientesList)

        val urlClientes = if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo) {
            "${GlobalVariables.serverUrl}/api/clientes"
        } else {
            "${GlobalVariables.serverUrl}/api/clientes?nusuario=${GlobalVariables.idUtilizadorAutenticado}"
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getClientes(requireContext(), urlClientes)
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Nome"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    clientesAdapter.filter.filter(newText)
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
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
                        fragment.commit()
                    }

                    1 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerReunioesOportunidades())
                        fragment.commit()
                    }

                    2 -> {
                        dialogOptions.dismiss()
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
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

        fabCreateCliente.setOnClickListener {
            GlobalVariables.criarCliente = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
            fragment.commit()
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

                            clientesAdapter = AdapterClientes(context, clientesList)
                            recyclerView.adapter = clientesAdapter

                            if (clientesList.isEmpty()) {
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