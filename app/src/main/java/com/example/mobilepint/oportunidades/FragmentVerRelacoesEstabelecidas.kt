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
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerRelacoesEstabelecidas : Fragment() {

    private lateinit var txt_titulo_negocio_label: TextView
    private lateinit var txt_nome_cliente_label: TextView
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var relacoesEstabelecidasList: ArrayList<ItemsRelacoesEstabelecidas>
    private lateinit var relacoesEstabelecidasAdapter: AdapterRelacoesEstabelecidas
    private lateinit var fabCreateRelacao: FloatingActionButton
    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_relacoes_estabelecidas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_titulo_negocio_label = view.findViewById(R.id.relacoes_estabelecidas_txt_titulo_negocio_label)
        txt_nome_cliente_label = view.findViewById(R.id.relacoes_estabelecidas_txt_nome_cliente_label)
        searchView = view.findViewById(R.id.relacoes_estabelecidas_searchView)
        recyclerView = view.findViewById(R.id.relacoes_estabelecidas_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateRelacao = view.findViewById(R.id.relacoes_estabelecidas_fabCreate)
        relacoesEstabelecidasList = ArrayList()
        relacoesEstabelecidasAdapter = AdapterRelacoesEstabelecidas(requireContext(), relacoesEstabelecidasList)

        txt_titulo_negocio_label.text = GlobalVariables.detalhesTituloOportunidade
        txt_nome_cliente_label.text = GlobalVariables.detalhesNomeCliente

        if (globalVariables.checkForInternet(requireContext())) {
            getRelacoesEstabelecidas(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Descrição"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    relacoesEstabelecidasAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        fabCreateRelacao.setOnClickListener {
            GlobalVariables.criarRelacaoCliente = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarRelacaoCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentDetalhesOportunidade())
            fragment.commit()
        }
    }

    private fun getRelacoesEstabelecidas(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/status?noportunidade=" + GlobalVariables.detalhesNumOportunidade)
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
                                val NStatus = get.getInt("NStatus")
                                val NOportunidade = get.getInt("NOportunidade")
                                var Titulo = get.getString("Titulo")
                                var Descricao = get.getString("Descricao")
                                var EnderecoAnexo = get.getString("EnderecoAnexo")
                                var DataHora = get.getString("DataHora")

                                if (Titulo.isNullOrBlank() || Titulo == "null") {
                                    Titulo = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (EnderecoAnexo.isNullOrBlank() || EnderecoAnexo == "null") {
                                    EnderecoAnexo = ""
                                }
                                if (DataHora.isNullOrBlank() || DataHora == "null") {
                                    DataHora = ""
                                }

                                relacoesEstabelecidasList.add(
                                    ItemsRelacoesEstabelecidas(
                                        NStatus,
                                        Titulo,
                                        Descricao,
                                        EnderecoAnexo,
                                        DataHora,
                                        NOportunidade
                                    )
                                )
                            }

                            relacoesEstabelecidasAdapter = AdapterRelacoesEstabelecidas(context, relacoesEstabelecidasList)
                            recyclerView.adapter = relacoesEstabelecidasAdapter

                            if (relacoesEstabelecidasList.isEmpty()) {
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