package com.example.mobilepint.vagas

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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentGerirVagas : Fragment() {

    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var txt_permissions: TextView
    private lateinit var cardView: CardView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var vagasFullAdapter: AdapterVagasFull
    private lateinit var vagasFullList: ArrayList<ItemsVagas>
    private lateinit var fabCreateVaga: FloatingActionButton
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gerir_vagas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.gerir_vagas_imgbtn_view_more)
        searchView = view.findViewById(R.id.gerir_vagas_searchView)
        txt_permissions = view.findViewById(R.id.gerir_vagas_txt_permissions)
        cardView = view.findViewById(R.id.gerir_vagas_cardView)
        recyclerView = view.findViewById(R.id.gerir_vagas_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateVaga = view.findViewById(R.id.gerir_vagas_fabCreate)
        vagasFullList = ArrayList()
        vagasFullAdapter = AdapterVagasFull(requireContext(), vagasFullList)

        if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo) {
            searchView.visibility = View.VISIBLE
            txt_permissions.visibility = View.GONE
            cardView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            fabCreateVaga.visibility = View.VISIBLE

            if (globalVariables.checkForInternet(requireContext())) {
                getVagas(requireContext())
            } else {
                Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
            }
        } else {
            searchView.visibility = View.INVISIBLE
            txt_permissions.visibility = View.VISIBLE
            cardView.visibility = View.GONE
            recyclerView.visibility = View.GONE
            fabCreateVaga.visibility = View.GONE
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Nome"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    vagasFullAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        imgbtn_view_more.setOnClickListener {
            val options = arrayOf(
                "Visualizar vagas disponíveis",
                "Visualizar candidaturas",
                "Visualizar reuniões das entrevistas",
                "Visualizar entrevistas",
                "Gerir vagas"
            )
            val builderOptions = android.app.AlertDialog.Builder(requireContext())
            builderOptions.setTitle("Escolha uma opção:")
            builderOptions.setItems(options) { dialogOptions, which ->
                when (which) {
                    0 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
                        fragment.commit()
                    }

                    1 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerCandidaturasVagas())
                        fragment.commit()
                    }

                    2 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerReunioesEntrevistas())
                        fragment.commit()
                    }

                    3 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerEntrevistasVagas())
                        fragment.commit()
                    }

                    4 -> {
                        dialogOptions.dismiss()
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builderOptions.show()
        }

        fabCreateVaga.setOnClickListener {
            GlobalVariables.criarVaga = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarVaga())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
            fragment.commit()
        }
    }

    private fun getVagas(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/vagas")
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
                                val NVaga = get.getInt("NVaga")
                                val NLocalidade = get.getInt("NLocalidade")
                                val NTipoVaga = get.getInt("NTipoVaga")
                                val Estado = get.getInt("Estado")
                                var NomeVaga = get.getString("NomeVaga")
                                var Subtitulo = get.getString("Subtitulo")
                                var Descricao = get.getString("Descricao")
                                var DataCriacao = get.getString("DataCriacao")
                                var Localidade = get.getString("Localidade")
                                var TipoVaga = get.getString("TipoVaga")

                                if (NomeVaga.isNullOrBlank() || NomeVaga == "null") {
                                    NomeVaga = ""
                                }
                                if (Subtitulo.isNullOrBlank() || Subtitulo == "null") {
                                    Subtitulo = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (DataCriacao.isNullOrBlank() || DataCriacao == "null") {
                                    DataCriacao = ""
                                }
                                if (Localidade.isNullOrBlank() || Localidade == "null") {
                                    Localidade = ""
                                }
                                if (TipoVaga.isNullOrBlank() || TipoVaga == "null") {
                                    TipoVaga = ""
                                }

                                vagasFullList.add(
                                    ItemsVagas(
                                        NVaga,
                                        NomeVaga,
                                        Subtitulo,
                                        Descricao,
                                        NLocalidade,
                                        NTipoVaga,
                                        Estado,
                                        DataCriacao,
                                        Localidade,
                                        TipoVaga
                                    )
                                )
                            }

                            vagasFullAdapter = AdapterVagasFull(context, vagasFullList)
                            recyclerView.adapter = vagasFullAdapter

                            if (vagasFullList.isEmpty()) {
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