package com.example.mobilepint.oportunidades

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentCriarCliente: Fragment() {

    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strNomeEmpresa = ""
        var strEmailEmpresa = ""
        var strTelefoneEmpresa = ""
        var strDescricaoEmpresa = ""
        var msgErrors = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_cliente_txt_titulo_janela) as TextView
        val edtxt_nome_empresa = view.findViewById(R.id.criar_cliente_edtxt_nome_empresa) as EditText
        val edtxt_email_empresa = view.findViewById(R.id.criar_cliente_edtxt_email_empresa) as EditText
        val edtxt_telefone_empresa = view.findViewById(R.id.criar_cliente_edtxt_telefone_empresa) as EditText
        val edtxt_descricao_empresa = view.findViewById(R.id.criar_cliente_edtxt_descricao_empresa) as EditText
        val btn_criar_cliente = view.findViewById(R.id.criar_cliente_btn_criar_cliente) as Button

        if (GlobalVariables.criarCliente) {
            txt_titulo_janela.text = getString(R.string.str_criar_cliente)
            btn_criar_cliente.text = getString(R.string.str_criar_cliente)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_cliente)
            btn_criar_cliente.text = getString(R.string.str_editar_cliente)

            edtxt_nome_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesNomeCliente)
            edtxt_email_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEmailCliente)
            edtxt_telefone_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTelefoneCliente)
            edtxt_descricao_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoCliente)
        }

        btn_criar_cliente.setOnClickListener {
            btn_criar_cliente.isEnabled = false
            Handler().postDelayed({
                btn_criar_cliente.isEnabled = true
            }, 10000)

            if (edtxt_nome_empresa.text.toString().isNotBlank()) {
                strNomeEmpresa = edtxt_nome_empresa.text.toString()
            } else {
                msgErrors += "O campo \'Nome da empresa\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_email_empresa.text.toString().isNotBlank()) {
                strEmailEmpresa = edtxt_email_empresa.text.toString()
                if (!globalVariables.isValidEmail(strEmailEmpresa)) {
                    msgErrors += "O e-mail inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                msgErrors += "O campo \'Email da empresa\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_telefone_empresa.text.toString().isNotBlank()) {
                strTelefoneEmpresa = edtxt_telefone_empresa.text.toString()
                if (!globalVariables.isValidPhone(strTelefoneEmpresa)) {
                    msgErrors += "O número de telefone inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                msgErrors += "O campo \'Telefone da empresa\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_empresa.text.toString().isNotBlank()) {
                strDescricaoEmpresa = edtxt_descricao_empresa.text.toString()
            } else {
                msgErrors += "O campo \'Descrição da empresa\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarCliente) {
                        postCliente(
                            requireContext(),
                            strNomeEmpresa,
                            strEmailEmpresa,
                            strTelefoneEmpresa,
                            strDescricaoEmpresa,
                            GlobalVariables.idUtilizadorAutenticado
                        )
                    } else {
                        putCliente(
                            requireContext(),
                            strNomeEmpresa,
                            strEmailEmpresa,
                            strTelefoneEmpresa,
                            strDescricaoEmpresa
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage(msgErrors)
                builder.setIcon(R.drawable.ic_information)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    msgErrors = ""
                }
                builder.show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (GlobalVariables.criarCliente) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerClientes())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesCliente())
                fragment.commit()
            }
        }
    }

    private fun postCliente(
        context: Context,
        nome: String,
        email: String,
        telefone: String,
        descricao: String,
        numUsuario: Int
    ) {
        data class Post(
            val NomeEmp: String,
            val EmailEmp: String,
            val TelefoneEmp: String,
            val Descricao: String,
            val NUsuarioCriador: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, email, telefone, descricao, numUsuario)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/clientes")
            .post(requestBody)
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
                            val message = jsonObject.getJSONObject("message")
                            val NCliente = message.getInt("NCliente")
                            val NomeEmp = message.getString("NomeEmp")
                            val EmailEmp = message.getString("EmailEmp")
                            val TelefoneEmp = message.getString("TelefoneEmp")
                            val Descricao = message.getString("Descricao")
                            val NUsuarioCriador = message.getInt("NUsuarioCriador")
                            val DataHoraCriacao = message.getString("DataHoraCriacao")
                            val NomeUsuarioCriador = message.getString("NomeUsuarioCriador")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O cliente \'$NomeEmp\' foi criado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerClientes())
                                fragment.commit()
                            }
                            builder.show()
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

    private fun putCliente(
        context: Context,
        nome: String,
        email: String,
        telefone: String,
        descricao: String
    ) {
        data class Post(
            val NomeEmp: String,
            val EmailEmp: String,
            val TelefoneEmp: String,
            val Descricao: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, email, telefone, descricao)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/clientes/" + GlobalVariables.detalhesNumCliente)
            .put(requestBody)
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
                            val message = jsonObject.getString("message")
                            println("Response body message: $message")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O cliente \'$nome\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerClientes())
                                fragment.commit()
                            }
                            builder.show()
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