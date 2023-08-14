package com.example.mobilepint.ideias

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

class FragmentVerIdeias : Fragment() {

    private lateinit var toast: Toast
    private lateinit var txt_titulo: TextView
    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutIdeiasTodas: LinearLayout
    private lateinit var linearLayoutIdeias: LinearLayout
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreateIdeia: FloatingActionButton
    private lateinit var ideiasFullAdapter: AdapterIdeiasFull
    private lateinit var ideiasAdapter: AdapterIdeias
    private lateinit var ideiasList: ArrayList<ItemsIdeias>
    private var lastBackPressTime: Long = 0
    private var verIdeiasTodas: Boolean = true
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_ideias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_titulo = view.findViewById(R.id.ideias_txt_titulo)
        imgbtn_view_more = view.findViewById(R.id.ideias_imgbtn_view_more)
        searchView = view.findViewById(R.id.ideias_searchView)
        linearLayoutIdeiasTodas = view.findViewById(R.id.ideias_linearLayout_ideias_todas)
        linearLayoutIdeias = view.findViewById(R.id.ideias_linearLayout_ideias)
        recyclerView = view.findViewById(R.id.ideias_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateIdeia = view.findViewById(R.id.ideias_fabCreate)

        ideiasList = ArrayList()
        ideiasFullAdapter = AdapterIdeiasFull(requireContext(), ideiasList)
        ideiasAdapter = AdapterIdeias(requireContext(), ideiasList)
        txt_titulo.text = getString(R.string.str_ideias)
        linearLayoutIdeiasTodas.visibility = View.VISIBLE
        linearLayoutIdeias.visibility = View.GONE
        fabCreateIdeia.visibility = View.VISIBLE

        val urlIdeias = if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_IDEIAS.numCargo) {
            "${GlobalVariables.serverUrl}/api/ideias"
        } else {
            "${GlobalVariables.serverUrl}/api/ideias?nusuario=${GlobalVariables.idUtilizadorAutenticado}"
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getIdeias(requireContext(), urlIdeias)
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Título"
        searchView.setQuery("", false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    if (verIdeiasTodas) {
                        ideiasFullAdapter.filter.filter(newText)
                    } else {
                        ideiasAdapter.filter.filter(newText)
                    }
                } catch (nullPointerException: NullPointerException) {
                    nullPointerException.printStackTrace()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return true
            }
        })

        imgbtn_view_more.setOnClickListener {
            val options = arrayOf(
                "Visualizar todas as ideias",
                "Visualizar ideias propostas",
                "Visualizar ideias aceitas",
                "Visualizar ideias rejeitadas",
                "Visualizar ideias arquivadas"
            )
            val builderOptions = android.app.AlertDialog.Builder(requireContext())
            builderOptions.setTitle("Escolha uma opção:")
            builderOptions.setItems(options) { dialogOptions, which ->
                when (which) {
                    0 -> {
                        dialogOptions.dismiss()
                        verIdeiasTodas = true
                        txt_titulo.text = getString(R.string.str_ideias)
                        searchView.setQuery("", false)
                        linearLayoutIdeiasTodas.visibility = View.VISIBLE
                        linearLayoutIdeias.visibility = View.GONE
                        fabCreateIdeia.visibility = View.VISIBLE

                        if (ideiasList.isNotEmpty()) {
                            ideiasFullAdapter = AdapterIdeiasFull(requireContext(), ideiasList)
                            recyclerView.adapter = ideiasFullAdapter
                        }
                    }

                    1 -> {
                        dialogOptions.dismiss()
                        verIdeiasTodas = false
                        txt_titulo.text = getString(R.string.str_ideias_propostas)
                        searchView.setQuery("", false)
                        linearLayoutIdeiasTodas.visibility = View.GONE
                        linearLayoutIdeias.visibility = View.VISIBLE
                        fabCreateIdeia.visibility = View.GONE

                        if (ideiasList.isNotEmpty()) {
                            val ideiasFiltradasList = ArrayList(ideiasList.filter { it.Estado == "Pendente" })
                            ideiasAdapter = AdapterIdeias(requireContext(), ideiasFiltradasList)
                            recyclerView.adapter = ideiasAdapter

                            if (ideiasFiltradasList.isEmpty()) {
                                Toast.makeText(requireContext(), "Sem dados para exibir.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    2 -> {
                        dialogOptions.dismiss()
                        verIdeiasTodas = false
                        txt_titulo.text = getString(R.string.str_ideias_aceitas)
                        searchView.setQuery("", false)
                        linearLayoutIdeiasTodas.visibility = View.GONE
                        linearLayoutIdeias.visibility = View.VISIBLE
                        fabCreateIdeia.visibility = View.GONE

                        if (ideiasList.isNotEmpty()) {
                            val ideiasFiltradasList = ArrayList(ideiasList.filter { it.Estado == "Aceite" })
                            ideiasAdapter = AdapterIdeias(requireContext(), ideiasFiltradasList)
                            recyclerView.adapter = ideiasAdapter

                            if (ideiasFiltradasList.isEmpty()) {
                                Toast.makeText(requireContext(), "Sem dados para exibir.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    3 -> {
                        dialogOptions.dismiss()
                        verIdeiasTodas = false
                        txt_titulo.text = getString(R.string.str_ideias_rejeitadas)
                        searchView.setQuery("", false)
                        linearLayoutIdeiasTodas.visibility = View.GONE
                        linearLayoutIdeias.visibility = View.VISIBLE
                        fabCreateIdeia.visibility = View.GONE

                        if (ideiasList.isNotEmpty()) {
                            val ideiasFiltradasList = ArrayList(ideiasList.filter { it.Estado == "Rejeitada" })
                            ideiasAdapter = AdapterIdeias(requireContext(), ideiasFiltradasList)
                            recyclerView.adapter = ideiasAdapter

                            if (ideiasFiltradasList.isEmpty()) {
                                Toast.makeText(requireContext(), "Sem dados para exibir.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    4 -> {
                        dialogOptions.dismiss()
                        verIdeiasTodas = false
                        txt_titulo.text = getString(R.string.str_ideias_arquivadas)
                        searchView.setQuery("", false)
                        linearLayoutIdeiasTodas.visibility = View.GONE
                        linearLayoutIdeias.visibility = View.VISIBLE
                        fabCreateIdeia.visibility = View.GONE

                        if (ideiasList.isNotEmpty()) {
                            val ideiasFiltradasList = ArrayList(ideiasList.filter { it.Estado == "Arquivada" })
                            ideiasAdapter = AdapterIdeias(requireContext(), ideiasFiltradasList)
                            recyclerView.adapter = ideiasAdapter

                            if (ideiasFiltradasList.isEmpty()) {
                                Toast.makeText(requireContext(), "Sem dados para exibir.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            builderOptions.show()
        }

        fabCreateIdeia.setOnClickListener {
            GlobalVariables.criarIdeia = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarIdeia())
            fragment.commit()
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

    private fun getIdeias(context: Context, url: String) {
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
                                val NIdeia = get.getInt("NIdeia")
                                val NUsuario = get.getInt("NUsuario")
                                var Titulo = get.getString("Titulo")
                                var Data = get.getString("Data")
                                var Estado = get.getString("Estado")
                                var Descricao = get.getString("Descricao")
                                var NomeUsuario = get.getString("NomeUsuario")

                                if (Titulo.isNullOrBlank() || Titulo == "null") {
                                    Titulo = ""
                                }
                                if (Data.isNullOrBlank() || Data == "null") {
                                    Data = ""
                                }
                                if (Estado.isNullOrBlank() || Estado == "null") {
                                    Estado = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (NomeUsuario.isNullOrBlank() || NomeUsuario == "null") {
                                    NomeUsuario = ""
                                }

                                ideiasList.add(
                                    ItemsIdeias(
                                        NIdeia,
                                        NUsuario,
                                        Titulo,
                                        Data,
                                        Estado,
                                        Descricao,
                                        NomeUsuario
                                    )
                                )
                            }

                            ideiasFullAdapter = AdapterIdeiasFull(context, ideiasList)
                            recyclerView.adapter = ideiasFullAdapter

                            if (ideiasList.isEmpty()) {
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