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
import androidx.cardview.widget.CardView
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

class FragmentVerEtiquetasEstagios : Fragment() {

    private lateinit var txt_titulo_janela: TextView
    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var txt_permissions: TextView
    private lateinit var searchView: SearchView
    private lateinit var cardView: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var fabCreateEtiquetasEstagios: FloatingActionButton
    private lateinit var etiquetasEstagiosAdapter: AdapterEtiquetasEstagios
    private lateinit var etiquetasList: ArrayList<ItemsEtiquetas>
    private lateinit var estagiosList: ArrayList<ItemsEstagios>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_etiquetas_estagios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_titulo_janela = view.findViewById(R.id.etiquetas_estagios_txt_titulo)
        imgbtn_view_more = view.findViewById(R.id.etiquetas_estagios_imgbtn_view_more)
        txt_permissions = view.findViewById(R.id.etiquetas_estagios_txt_permissions)
        searchView = view.findViewById(R.id.etiquetas_estagios_searchView)
        cardView = view.findViewById(R.id.etiquetas_estagios_cardView)
        recyclerView = view.findViewById(R.id.etiquetas_estagios_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateEtiquetasEstagios = view.findViewById(R.id.etiquetas_estagios_fabCreate)

        etiquetasList = ArrayList()
        estagiosList = ArrayList()
        etiquetasEstagiosAdapter = AdapterEtiquetasEstagios(requireContext(), etiquetasList, estagiosList)
        handlePermissions(numCargo)

        if (GlobalVariables.verEtiquetas) {
            txt_titulo_janela.text = getString(R.string.str_areas_negocio)
        } else {
            txt_titulo_janela.text = getString(R.string.str_estagios)
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getEtiquetas(requireContext())
            getEstagios(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Nome"
        searchView.setQuery("", false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    etiquetasEstagiosAdapter.filter.filter(newText)
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
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerClientes())
                        fragment.commit()
                    }

                    3 -> {
                        dialogOptions.dismiss()
                        searchView.setQuery("", false)
                        if (GlobalVariables.verEtiquetas) {
                            Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
                        } else {
                            GlobalVariables.verEtiquetas = true
                            txt_titulo_janela.text = getString(R.string.str_areas_negocio)
                            handlePermissions(numCargo)
                            etiquetasEstagiosAdapter = AdapterEtiquetasEstagios(requireContext(), etiquetasList, estagiosList)
                            recyclerView.adapter = etiquetasEstagiosAdapter
                        }
                    }

                    4 -> {
                        dialogOptions.dismiss()
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerTiposProjetos())
                        fragment.commit()
                    }

                    5 -> {
                        dialogOptions.dismiss()
                        searchView.setQuery("", false)
                        if (!GlobalVariables.verEtiquetas) {
                            Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
                        } else {
                            GlobalVariables.verEtiquetas = false
                            txt_titulo_janela.text = getString(R.string.str_estagios)
                            handlePermissions(numCargo)
                            etiquetasEstagiosAdapter = AdapterEtiquetasEstagios(requireContext(), etiquetasList, estagiosList)
                            recyclerView.adapter = etiquetasEstagiosAdapter
                        }
                    }
                }
            }
            builderOptions.show()
        }

        fabCreateEtiquetasEstagios.setOnClickListener {
            GlobalVariables.criarEtiquetaEstagio = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarEtiquetaEstagio())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
            fragment.commit()
        }
    }

    private fun handlePermissions(NumCargo: Int) {
        if (NumCargo == EnumCargos.ADMINISTRADOR.numCargo || NumCargo == EnumCargos.GESTOR_VENDAS.numCargo) {
            txt_permissions.visibility = View.GONE
            searchView.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            fabCreateEtiquetasEstagios.visibility = View.VISIBLE
        } else {
            txt_permissions.visibility = View.GONE
            searchView.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            fabCreateEtiquetasEstagios.visibility = View.GONE
        }
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

                            if (GlobalVariables.verEtiquetas) {
                                etiquetasEstagiosAdapter = AdapterEtiquetasEstagios(context, etiquetasList, estagiosList)
                                recyclerView.adapter = etiquetasEstagiosAdapter

                                if (etiquetasList.isEmpty()) {
                                    Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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

                            if (!GlobalVariables.verEtiquetas) {
                                etiquetasEstagiosAdapter = AdapterEtiquetasEstagios(context, etiquetasList, estagiosList)
                                recyclerView.adapter = etiquetasEstagiosAdapter

                                if (estagiosList.isEmpty()) {
                                    Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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
}