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

class FragmentVerTiposProjetos : Fragment() {

    private lateinit var txt_titulo_janela: TextView
    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var cardView: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var fabCreateTipoProjeto: FloatingActionButton
    private lateinit var tiposProjetosAdapter: AdapterTiposProjetos
    private lateinit var tiposProjetosList: ArrayList<ItemsTiposProjetos>
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_tipos_projetos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_titulo_janela = view.findViewById(R.id.tipos_projetos_txt_titulo)
        imgbtn_view_more = view.findViewById(R.id.tipos_projetos_imgbtn_view_more)
        searchView = view.findViewById(R.id.tipos_projetos_searchView)
        cardView = view.findViewById(R.id.tipos_projetos_cardView)
        recyclerView = view.findViewById(R.id.tipos_projetos_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateTipoProjeto = view.findViewById(R.id.tipos_projetos_fabCreate)

        tiposProjetosList = ArrayList()
        tiposProjetosAdapter = AdapterTiposProjetos(requireContext(), tiposProjetosList)

        if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.GESTOR_VENDAS.numCargo) {
            fabCreateTipoProjeto.visibility = View.VISIBLE
        } else {
            fabCreateTipoProjeto.visibility = View.GONE
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getTiposProjetos(requireContext())
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
                    tiposProjetosAdapter.filter.filter(newText)
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
                        GlobalVariables.verEtiquetas = true
                        val fragment = parentFragmentManager.beginTransaction()
                        fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
                        fragment.commit()
                    }

                    4 -> {
                        dialogOptions.dismiss()
                        Toast.makeText(requireContext(), "Página atual", Toast.LENGTH_SHORT).show()
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

        fabCreateTipoProjeto.setOnClickListener {
            GlobalVariables.criarTipoProjeto = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarTipoProjeto())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerOportunidades())
            fragment.commit()
        }
    }

    private fun getTiposProjetos(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/tipoprojetos")
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
                                val NTipoProjeto = get.getInt("NTipoProjeto")
                                val Nome = get.getString("Nome")
                                tiposProjetosList.add(ItemsTiposProjetos(NTipoProjeto, Nome))
                            }

                            tiposProjetosAdapter = AdapterTiposProjetos(context, tiposProjetosList)
                            recyclerView.adapter = tiposProjetosAdapter

                            if (tiposProjetosList.isEmpty()) {
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