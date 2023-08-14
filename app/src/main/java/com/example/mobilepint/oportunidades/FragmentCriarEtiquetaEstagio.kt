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

class FragmentCriarEtiquetaEstagio: Fragment() {

    private var globalVariables = GlobalVariables()
    private var bundleNumEtiqueta: Int = 0
    private var bundleNumEstagio: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_etiqueta_estagio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strEtiqueta = ""
        var strEstagio = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_etiqueta_estagio_txt_titulo_janela) as TextView
        val layout_criar_etiqueta = view.findViewById(R.id.criar_etiqueta_estagio_linearLayout_criar_etiqueta) as LinearLayout
        val edtxt_nome_etiqueta = view.findViewById(R.id.criar_etiqueta_estagio_edtxt_nome_etiqueta) as EditText
        val layout_criar_estagio = view.findViewById(R.id.criar_etiqueta_estagio_linearLayout_criar_estagio) as LinearLayout
        val edtxt_nome_estagio = view.findViewById(R.id.criar_etiqueta_estagio_edtxt_nome_estagio) as EditText
        val btn_criar_etiqueta_estagio = view.findViewById(R.id.criar_etiqueta_estagio_btn_criar) as Button

        if (GlobalVariables.criarEtiquetaEstagio) {
            if (GlobalVariables.verEtiquetas) {
                txt_titulo_janela.text = getString(R.string.str_criar_area_negocio)
                btn_criar_etiqueta_estagio.text = getString(R.string.str_criar_area_negocio)
                layout_criar_etiqueta.visibility = View.VISIBLE
                layout_criar_estagio.visibility = View.GONE
            } else {
                txt_titulo_janela.text = "Criar estágio"
                btn_criar_etiqueta_estagio.text = "Criar estágio"
                layout_criar_etiqueta.visibility = View.GONE
                layout_criar_estagio.visibility = View.VISIBLE
            }
        } else {
            if (GlobalVariables.verEtiquetas) {
                txt_titulo_janela.text = "Editar área de negócio"
                btn_criar_etiqueta_estagio.text = "Editar área de negócio"
                layout_criar_etiqueta.visibility = View.VISIBLE
                layout_criar_estagio.visibility = View.GONE

                val bundleEtiqueta = arguments
                if (bundleEtiqueta != null) {
                    bundleNumEtiqueta = bundleEtiqueta.getInt("NumEtiqueta")
                    val bundleNomeEtiqueta = bundleEtiqueta.getString("NomeEtiqueta")
                    edtxt_nome_etiqueta.text = Editable.Factory.getInstance().newEditable(bundleNomeEtiqueta)
                }
            } else {
                txt_titulo_janela.text = "Editar estágio"
                btn_criar_etiqueta_estagio.text = "Editar estágio"
                layout_criar_etiqueta.visibility = View.GONE
                layout_criar_estagio.visibility = View.VISIBLE

                val bundleEstagio = arguments
                if (bundleEstagio != null) {
                    bundleNumEstagio = bundleEstagio.getInt("NumEstagio")
                    val bundleNomeEstagio = bundleEstagio.getString("NomeEstagio")
                    edtxt_nome_estagio.text = Editable.Factory.getInstance().newEditable(bundleNomeEstagio)
                }
            }
        }

        btn_criar_etiqueta_estagio.setOnClickListener {
            btn_criar_etiqueta_estagio.isEnabled = false
            Handler().postDelayed({
                btn_criar_etiqueta_estagio.isEnabled = true
            }, 10000)

            if (GlobalVariables.verEtiquetas) {
                if (edtxt_nome_etiqueta.text.toString().isNotBlank()) {
                    strEtiqueta = edtxt_nome_etiqueta.text.toString()
                } else {
                    msgErrors += "O campo \'Nome da área de negócio\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
                }
            } else {
                if (edtxt_nome_estagio.text.toString().isNotBlank()) {
                    strEstagio = edtxt_nome_estagio.text.toString()
                } else {
                    msgErrors += "O campo \'Nome do estágio de negócio\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
                }
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarEtiquetaEstagio) {
                        if (GlobalVariables.verEtiquetas) {
                            postEtiqueta(requireContext(), strEtiqueta)
                        } else {
                            postEstagio(requireContext(), strEstagio)
                        }
                    } else {
                        if (GlobalVariables.verEtiquetas) {
                            putEtiqueta(requireContext(), strEtiqueta)
                        } else {
                            putEstagio(requireContext(), strEstagio)
                        }
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
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
            fragment.commit()
        }
    }

    private fun postEtiqueta(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/etiquetas")
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
                            val NEtiqueta = message.getInt("NEtiqueta")
                            val Nome = message.getString("Nome")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A área de negócio \'$Nome\' foi criada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
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

    private fun postEstagio(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/estagios")
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
                            val NEstagio = message.getInt("NEstagio")
                            val Nome = message.getString("Nome")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O estágio de negócio \'$Nome\' foi criado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
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

    private fun putEtiqueta(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/etiquetas/" + bundleNumEtiqueta)
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
                            builder.setMessage("A área de negócio \'$nome\' foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
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

    private fun putEstagio(context: Context, nome: String) {
        data class Post(val Nome: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/estagios/" + bundleNumEstagio)
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
                            builder.setMessage("O estágio de negócio \'$nome\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerEtiquetasEstagios())
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