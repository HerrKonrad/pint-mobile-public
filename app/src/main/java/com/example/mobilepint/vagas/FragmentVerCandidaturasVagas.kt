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

class FragmentVerCandidaturasVagas : Fragment() {

    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var candidaturasVagasAdapter: AdapterCandidaturasVagas
    private lateinit var candidaturasVagasList: ArrayList<ItemsCandidaturas>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var havePermissions: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_candidaturas_vagas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgbtn_view_more = view.findViewById(R.id.candidaturas_vagas_imgbtn_view_more)
        searchView = view.findViewById(R.id.candidaturas_vagas_searchView)
        recyclerView = view.findViewById(R.id.candidaturas_vagas_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        candidaturasVagasList = ArrayList()
        candidaturasVagasAdapter = AdapterCandidaturasVagas(requireContext(), candidaturasVagasList)
        havePermissions = (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo)

        val urlCandidaturas = if (havePermissions) {
            "${GlobalVariables.serverUrl}/api/candidaturas"
        } else {
            "${GlobalVariables.serverUrl}/api/candidaturas?nusuario=${GlobalVariables.idUtilizadorAutenticado}"
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getCandidaturas(requireContext(), urlCandidaturas)
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
                    candidaturasVagasAdapter.filter.filter(newText)
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
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
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

    private fun getCandidaturas(context: Context, url: String) {
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
                                val NCandidatura = message.getInt("NCandidatura")
                                val NVaga = message.getInt("NVaga")
                                val NUsuario = message.getInt("NUsuario")
                                val Estado = message.getInt("Estado")
                                var Estagio = message.getString("Estagio")
                                var DataCandidatura = message.getString("DataCandidatura")
                                var PretencaoSalarial = message.getString("PretencaoSalarial")
                                var Mensagem = message.getString("Mensagem")
                                var NomeUsuario = message.getString("NomeUsuario")
                                var NomeVaga = message.getString("NomeVaga")
                                var SubtituloVaga = message.getString("Subtitulo")
                                var EmailUsuario = message.getString("EmailUsuario")

                                if (Estagio.isNullOrBlank() || Estagio == "null") {
                                    Estagio = ""
                                }
                                if (DataCandidatura.isNullOrBlank() || DataCandidatura == "null") {
                                    DataCandidatura = ""
                                }
                                if (PretencaoSalarial.isNullOrBlank() || PretencaoSalarial == "null") {
                                    PretencaoSalarial = ""
                                }
                                if (Mensagem.isNullOrBlank() || Mensagem == "null") {
                                    Mensagem = ""
                                }
                                if (NomeUsuario.isNullOrBlank() || NomeUsuario == "null") {
                                    NomeUsuario = ""
                                }
                                if (NomeVaga.isNullOrBlank() || NomeVaga == "null") {
                                    NomeVaga = ""
                                }
                                if (SubtituloVaga.isNullOrBlank() || SubtituloVaga == "null") {
                                    SubtituloVaga = ""
                                }
                                if (EmailUsuario.isNullOrBlank() || EmailUsuario == "null") {
                                    EmailUsuario = ""
                                }

                                candidaturasVagasList.add(
                                    ItemsCandidaturas(
                                        NCandidatura,
                                        NVaga,
                                        NUsuario,
                                        DataCandidatura,
                                        PretencaoSalarial,
                                        Mensagem,
                                        Estado,
                                        Estagio,
                                        NomeUsuario,
                                        NomeVaga,
                                        SubtituloVaga,
                                        EmailUsuario
                                    )
                                )
                            }

                            candidaturasVagasAdapter = AdapterCandidaturasVagas(context, candidaturasVagasList)
                            recyclerView.adapter = candidaturasVagasAdapter

                            if (candidaturasVagasList.isEmpty()) {
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