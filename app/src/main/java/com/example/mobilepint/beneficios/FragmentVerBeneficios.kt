package com.example.mobilepint.beneficios

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
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

class FragmentVerBeneficios : Fragment() {

    private lateinit var toast: Toast
    private lateinit var searchView: SearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreateBeneficio: FloatingActionButton
    private lateinit var beneficiosAdapter: AdapterBeneficios
    private lateinit var beneficiosList: ArrayList<ItemsBeneficios>
    private var lastBackPressTime: Long = 0
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_beneficios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchView = view.findViewById(R.id.beneficios_searchView)
        recyclerView = view.findViewById(R.id.beneficios_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateBeneficio = view.findViewById(R.id.beneficios_fabCreate)
        beneficiosList = ArrayList()
        beneficiosAdapter = AdapterBeneficios(requireContext(), beneficiosList)

        if (globalVariables.checkForInternet(requireContext())) {
            getBeneficios(requireContext())
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
                    beneficiosAdapter.filter.filter(newText)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
                return true
            }
        })

        if (numCargo == EnumCargos.ADMINISTRADOR.numCargo) {
            fabCreateBeneficio.visibility = View.VISIBLE
        } else {
            fabCreateBeneficio.visibility = View.GONE
        }

        fabCreateBeneficio.setOnClickListener {
            GlobalVariables.criarBeneficio = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarBeneficio())
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

    private fun getBeneficios(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/beneficios")
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
                                val NBeneficio = get.getInt("NBeneficio")
                                val NomeBeneficio = get.getString("NomeBeneficio")
                                val Subtitulo = get.getString("Subtitulo")
                                val Descricao = get.getString("Descricao")
                                val EnderecoImagem = get.getString("EnderecoImagem")
                                beneficiosList.add(
                                    ItemsBeneficios(
                                        NBeneficio,
                                        NomeBeneficio,
                                        Subtitulo,
                                        Descricao,
                                        EnderecoImagem
                                    )
                                )
                            }

                            beneficiosAdapter = AdapterBeneficios(context, beneficiosList)
                            recyclerView.adapter = beneficiosAdapter

                            if (beneficiosList.isEmpty()) {
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