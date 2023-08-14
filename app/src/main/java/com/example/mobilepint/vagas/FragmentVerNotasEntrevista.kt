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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerNotasEntrevista : Fragment() {

    private lateinit var txt_nome_candidato_label: TextView
    private lateinit var txt_nome_vaga_label: TextView
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var entrevistasList: ArrayList<ItemsEntrevistas>
    private lateinit var notasEntrevistasList: ArrayList<ItemsNotasEntrevistas>
    private lateinit var notasEntrevistasAdapter: AdapterNotasEntrevistas
    private lateinit var fabCreateNota: FloatingActionButton
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false
    private var urlNotasEntrevistas: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_notas_entrevista, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_nome_candidato_label = view.findViewById(R.id.notas_entrevista_txt_nome_candidato_label)
        txt_nome_vaga_label = view.findViewById(R.id.notas_entrevista_txt_nome_vaga_label)
        searchView = view.findViewById(R.id.notas_entrevista_searchView)
        recyclerView = view.findViewById(R.id.notas_entrevista_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateNota = view.findViewById(R.id.notas_entrevista_fabCreate)
        entrevistasList = ArrayList()
        notasEntrevistasList = ArrayList()
        notasEntrevistasAdapter = AdapterNotasEntrevistas(requireContext(), notasEntrevistasList)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVagaCandidatura
        txt_nome_candidato_label.text = GlobalVariables.detalhesNomeUsuarioCandidatura
        urlNotasEntrevistas = "${GlobalVariables.serverUrl}/api/nota?nentrevista=${GlobalVariables.detalhesNumEntrevista}"

        if (globalVariables.checkForInternet(requireContext())) {
            getEntrevistas(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Texto"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    notasEntrevistasAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        fabCreateNota.setOnClickListener {
            GlobalVariables.criarNotaEntrevista = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarNotaEntrevista())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentDetalhesEntrevista())
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

                            if (globalVariables.checkForInternet(requireContext())) {
                                getNotasEntrevistas(context, urlNotasEntrevistas)
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

    private fun getNotasEntrevistas(context: Context, url: String) {
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
                                val NNota = get.getInt("NNota")
                                val NEntrevista = get.getInt("NEntrevista")
                                val NUsuarioRH = get.getInt("NUsuarioRH")
                                var Texto = get.getString("Texto")
                                var Anexo = get.getString("Anexo")
                                var DataHora = get.getString("DataHora")
                                var NomeRH = get.getString("NomeRH")

                                if (Texto.isNullOrBlank() || Texto == "null") {
                                    Texto = ""
                                }
                                if (Anexo.isNullOrBlank() || Anexo == "null") {
                                    Anexo = ""
                                }
                                if (DataHora.isNullOrBlank() || DataHora == "null") {
                                    DataHora = ""
                                }
                                if (NomeRH.isNullOrBlank() || NomeRH == "null") {
                                    NomeRH = ""
                                }

                                var NumCandidatura = -1
                                var NumUsuario = -1
                                var NomeUsuario = ""
                                var NomeVaga = ""
                                var EstadoEntrevista = ""

                                if (entrevistasList.any { it.NEntrevista == NEntrevista }) {
                                    val item = entrevistasList.find { it.NEntrevista == NEntrevista }
                                    NumCandidatura = item?.NCandidatura ?: -1
                                    NumUsuario = item?.NumUsuarioCandidatura ?: -1
                                    NomeUsuario = item?.NomeUsuarioCandidatura ?: ""
                                    NomeVaga = item?.NomeVagaCandidatura ?: ""
                                    EstadoEntrevista = item?.EstadoEntrevista ?: ""
                                }

                                notasEntrevistasList.add(
                                    ItemsNotasEntrevistas(
                                        NNota,
                                        NEntrevista,
                                        NUsuarioRH,
                                        Texto,
                                        Anexo,
                                        DataHora,
                                        NomeRH,
                                        NumCandidatura,
                                        NumUsuario,
                                        NomeUsuario,
                                        NomeVaga,
                                        EstadoEntrevista
                                    )
                                )
                            }
                            notasEntrevistasAdapter = AdapterNotasEntrevistas(context, notasEntrevistasList)
                            recyclerView.adapter = notasEntrevistasAdapter

                            if (notasEntrevistasList.isEmpty()) {
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