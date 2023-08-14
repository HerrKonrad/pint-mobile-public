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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerEntrevistasVagas : Fragment() {

    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var entrevistasAdapter: AdapterEntrevistas
    private lateinit var entrevistasList: ArrayList<ItemsEntrevistas>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_entrevistas_vagas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.entrevistas_imgbtn_view_more)
        searchView = view.findViewById(R.id.entrevistas_vagas_searchView)
        recyclerView = view.findViewById(R.id.entrevistas_vagas_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        entrevistasList = ArrayList()
        entrevistasAdapter = AdapterEntrevistas(requireContext(), entrevistasList)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        if (globalVariables.checkForInternet(requireContext())) {
            getEntrevistas(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Vaga"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    entrevistasAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        imgbtn_view_more.setOnClickListener {
            val options = if (havePermissions) {
                arrayOf(
                    "Visualizar vagas disponíveis",
                    "Visualizar candidaturas",
                    "Visualizar reuniões das entrevistas",
                    "Visualizar entrevistas",
                    "Gerir vagas"
                )
            } else {
                arrayOf(
                    "Visualizar vagas disponíveis",
                    "Visualizar candidaturas",
                    "Visualizar reuniões das entrevistas",
                    "Visualizar entrevistas"
                )
            }

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
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
                    }

                    4 -> {
                        if (havePermissions) {
                            dialogOptions.dismiss()
                            val fragment = parentFragmentManager.beginTransaction()
                            fragment.replace(R.id.fragment_container, FragmentGerirVagas())
                            fragment.commit()
                        }
                    }
                }
            }
            builderOptions.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
            fragment.commit()
        }
    }

    private fun getEntrevistas(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/entrevistas")
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
                                val NEntrevista = get.getInt("NEntrevista")
                                val NCandidatura = get.getInt("NCandidatura")
                                val NUsuario = get.getInt("NUsuario")
                                var Descricao = get.getString("Descricao")
                                var Estado = get.getString("Estado")
                                var NomeUsuario = get.getString("NomeUsuario")
                                var NomeVaga = get.getString("NomeVaga")

                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (Estado.isNullOrBlank() || Estado == "null") {
                                    Estado = ""
                                }
                                if (NomeUsuario.isNullOrBlank() || NomeUsuario == "null") {
                                    NomeUsuario = ""
                                }
                                if (NomeVaga.isNullOrBlank() || NomeVaga == "null") {
                                    NomeVaga = ""
                                }

                                if (havePermissions) {
                                    entrevistasList.add(
                                        ItemsEntrevistas(
                                            NEntrevista,
                                            NCandidatura,
                                            Descricao,
                                            Estado,
                                            NUsuario,
                                            NomeUsuario,
                                            NomeVaga
                                        )
                                    )
                                } else {
                                    if (NUsuario == GlobalVariables.idUtilizadorAutenticado) {
                                        entrevistasList.add(
                                            ItemsEntrevistas(
                                                NEntrevista,
                                                NCandidatura,
                                                Descricao,
                                                Estado,
                                                NUsuario,
                                                NomeUsuario,
                                                NomeVaga
                                            )
                                        )
                                    }
                                }
                            }

                            entrevistasAdapter = AdapterEntrevistas(context, entrevistasList)
                            recyclerView.adapter = entrevistasAdapter

                            if (entrevistasList.isEmpty()) {
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