package com.example.mobilepint.oportunidades

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
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

class FragmentVerContactosCliente : Fragment() {

    private lateinit var txt_nome_empresa_label: TextView
    private lateinit var txt_email_principal_label: TextView
    private lateinit var txt_telefone_principal_label: TextView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactosClientesList: ArrayList<ItemsContactos>
    private lateinit var contactosClientesAdapter: AdapterContactosClientes
    private lateinit var fabCreateContacto: FloatingActionButton
    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_contactos_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_nome_empresa_label = view.findViewById(R.id.contactos_cliente_txt_nome_empresa_label)
        txt_email_principal_label = view.findViewById(R.id.contactos_cliente_txt_email_principal_empresa_label)
        txt_telefone_principal_label = view.findViewById(R.id.contactos_cliente_txt_telefone_principal_empresa_label)
        recyclerView = view.findViewById(R.id.contactos_cliente_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        fabCreateContacto = view.findViewById(R.id.contactos_cliente_fabCreate)
        contactosClientesList = ArrayList()
        contactosClientesAdapter = AdapterContactosClientes(requireContext(), contactosClientesList)

        txt_nome_empresa_label.text = GlobalVariables.detalhesNomeCliente
        txt_email_principal_label.text = GlobalVariables.detalhesEmailCliente
        txt_telefone_principal_label.text = GlobalVariables.detalhesTelefoneCliente

        if (globalVariables.checkForInternet(requireContext())) {
            getContactosClientes(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        fabCreateContacto.setOnClickListener {
            GlobalVariables.criarContactoCliente = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarContactoCliente())
            fragment.commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentDetalhesCliente())
            fragment.commit()
        }
    }

    private fun getContactosClientes(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/contactos?ncliente=" + GlobalVariables.detalhesNumCliente)
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
                                val NContactos = get.getInt("NContactos")
                                val NCliente = get.getInt("NCliente")
                                var Telefone = get.getString("Telefone")
                                var Email = get.getString("Email")

                                if (Telefone.isNullOrBlank() || Telefone == "null") {
                                    Telefone = ""
                                }
                                if (Email.isNullOrBlank() || Email == "null") {
                                    Email = ""
                                }

                                contactosClientesList.add(
                                    ItemsContactos(
                                        NContactos,
                                        Telefone,
                                        Email,
                                        NCliente
                                    )
                                )
                            }

                            contactosClientesAdapter = AdapterContactosClientes(context, contactosClientesList)
                            recyclerView.adapter = contactosClientesAdapter

                            if (contactosClientesList.isEmpty()) {
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