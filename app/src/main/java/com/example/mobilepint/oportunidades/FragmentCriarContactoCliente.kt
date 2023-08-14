package com.example.mobilepint.oportunidades

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentCriarContactoCliente: Fragment() {

    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_contacto_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strEmailEmpresa = ""
        var strTelefoneEmpresa = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_contacto_txt_titulo_janela) as TextView
        val txt_subtitulo_janela = view.findViewById(R.id.criar_contacto_txt_subtitulo_janela) as TextView
        val edtxt_email_empresa = view.findViewById(R.id.criar_contacto_edtxt_email_empresa) as EditText
        val edtxt_telefone_empresa = view.findViewById(R.id.criar_contacto_edtxt_telefone_empresa) as EditText
        val btn_criar_contacto = view.findViewById(R.id.criar_contacto_btn_criar_contacto) as Button

        if (GlobalVariables.criarContactoCliente) {
            txt_titulo_janela.text = getString(R.string.str_criar_contacto)
            txt_subtitulo_janela.text = getString(R.string.str_adicionar_contacto_cliente)
            btn_criar_contacto.text = getString(R.string.str_criar_contacto)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_contacto)
            txt_subtitulo_janela.text = getString(R.string.str_editar_contacto_selecionado_cliente)
            btn_criar_contacto.text = getString(R.string.str_editar_contacto)

            edtxt_email_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEmailContacto)
            edtxt_telefone_empresa.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTelefoneContacto)
        }

        btn_criar_contacto.setOnClickListener {
            btn_criar_contacto.isEnabled = false
            Handler().postDelayed({
                btn_criar_contacto.isEnabled = true
            }, 10000)

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

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarContactoCliente) {
                        postContacto(
                            requireContext(),
                            strEmailEmpresa,
                            strTelefoneEmpresa,
                            GlobalVariables.detalhesNumCliente
                        )
                    } else {
                        putContacto(requireContext(), strEmailEmpresa, strTelefoneEmpresa)
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
            if (GlobalVariables.criarContactoCliente) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerContactosCliente())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesContactoCliente())
                fragment.commit()
            }
        }
    }

    private fun postContacto(
        context: Context,
        email: String,
        telefone: String,
        numCliente: Int
    ) {
        data class Post(val Email: String, val Telefone: String, val NCliente: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(email, telefone, numCliente)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/contactos")
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
                            val NContactos = message.getInt("NContactos")
                            val Telefone = message.getString("Telefone")
                            val Email = message.getString("Email")
                            val NCliente = message.getInt("NCliente")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O contacto foi adicionado ao cliente \'${GlobalVariables.detalhesNomeCliente}\' com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerContactosCliente())
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

    private fun putContacto(context: Context, email: String, telefone: String) {
        data class Post(val Email: String, val Telefone: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(email, telefone)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/contactos/" + GlobalVariables.detalhesNumContacto)
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
                            builder.setMessage("O contacto do cliente \'${GlobalVariables.detalhesNomeCliente}\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerContactosCliente())
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