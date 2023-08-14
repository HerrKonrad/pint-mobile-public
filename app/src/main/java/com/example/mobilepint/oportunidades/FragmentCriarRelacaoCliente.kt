package com.example.mobilepint.oportunidades

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FragmentCriarRelacaoCliente: Fragment() {

    private lateinit var edtxt_endereco_anexo: EditText
    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_relacao_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strTituloInteracao = ""
        var strDescricaoInteracao = ""
        var strEnderecoAnexo = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_interacao_txt_titulo_janela) as TextView
        val txt_subtitulo_janela = view.findViewById(R.id.criar_interacao_txt_subtitulo_janela) as TextView
        val edtxt_titulo_interacao = view.findViewById(R.id.criar_interacao_edtxt_titulo_interacao) as EditText
        val edtxt_descricao_interacao = view.findViewById(R.id.criar_interacao_edtxt_descricao_interacao) as EditText
        edtxt_endereco_anexo = view.findViewById(R.id.criar_interacao_edtxt_endereco_anexo)
        val imgbtn_carregar_ficheiro = view.findViewById(R.id.criar_interacao_imgbtn_carregar_ficheiro) as ImageButton
        val imgbtn_download_ficheiro = view.findViewById(R.id.criar_interacao_imgbtn_download_ficheiro) as ImageButton
        val btn_criar_interacao = view.findViewById(R.id.criar_interacao_btn_criar_interacao) as Button

        if (GlobalVariables.criarRelacaoCliente) {
            txt_titulo_janela.text = getString(R.string.str_criar_interacao)
            txt_subtitulo_janela.text = getString(R.string.str_estabelecer_relacao_cliente)
            btn_criar_interacao.text = getString(R.string.str_criar_interacao)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_interacao)
            txt_subtitulo_janela.text = getString(R.string.str_editar_relacao_estabelecida)
            btn_criar_interacao.text = getString(R.string.str_editar_interacao)

            edtxt_titulo_interacao.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTituloStatus)
            edtxt_descricao_interacao.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoStatus)
            edtxt_endereco_anexo.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEnderecoAnexoStatus)
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data

                val inputStream = requireContext().contentResolver.openInputStream(uri!!)
                val fileName = globalVariables.getFileName(requireContext(), uri) ?: "documento.pdf"
                val file = File(requireContext().cacheDir, fileName)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)

                Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                uploadFile(requireContext(), file)
            }
        }

        imgbtn_carregar_ficheiro.setOnClickListener {
            imgbtn_carregar_ficheiro.isEnabled = false
            Handler().postDelayed({
                imgbtn_carregar_ficheiro.isEnabled = true
            }, 10000)

            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            resultLauncher.launch(intent)
        }

        imgbtn_download_ficheiro.setOnClickListener {
            val urlCurriculo = edtxt_endereco_anexo.text.toString()
            if (urlCurriculo.isNotBlank()) {
                val fileName = urlCurriculo.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlCurriculo, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
            }
        }

        btn_criar_interacao.setOnClickListener {
            btn_criar_interacao.isEnabled = false
            Handler().postDelayed({
                btn_criar_interacao.isEnabled = true
            }, 10000)

            if (edtxt_titulo_interacao.text.toString().isNotBlank()) {
                strTituloInteracao = edtxt_titulo_interacao.text.toString()
            } else {
                msgErrors += "O campo \'Título da interação\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_interacao.text.toString().isNotBlank()) {
                strDescricaoInteracao = edtxt_descricao_interacao.text.toString()
            } else {
                msgErrors += "O campo \'Descrição da interação\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            strEnderecoAnexo = edtxt_endereco_anexo.text.toString().ifBlank { "" }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarRelacaoCliente) {
                        postStatus(
                            requireContext(),
                            strTituloInteracao,
                            strDescricaoInteracao,
                            strEnderecoAnexo,
                            GlobalVariables.detalhesNumOportunidade
                        )
                    } else {
                        putStatus(
                            requireContext(),
                            strTituloInteracao,
                            strDescricaoInteracao,
                            strEnderecoAnexo
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
            if (GlobalVariables.criarRelacaoCliente) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerRelacoesEstabelecidas())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesRelacaoCliente())
                fragment.commit()
            }
        }
    }

    private fun uploadFile(context: Context, file: File) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("ficheiro", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/ficheiro")
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
                            val message = jsonObject.getString("message")
                            edtxt_endereco_anexo.text = Editable.Factory.getInstance().newEditable(message)
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

    private fun downloadAndSaveFile(context: Context, fileUrl: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(fileUrl))
                .setTitle(fileName)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            Toast.makeText(context, "A transferência foi concluída com sucesso.", Toast.LENGTH_LONG).show()
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun postStatus(
        context: Context,
        titulo: String,
        descricao: String,
        enderecoAnexo: String,
        numOportunidade: Int
    ) {
        data class Post(
            val Titulo: String,
            val Descricao: String,
            val EnderecoAnexo: String,
            val NOportunidade: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, descricao, enderecoAnexo, numOportunidade)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/status")
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
                            val NStatus = message.getInt("NStatus")
                            val Titulo = message.getString("Titulo")
                            val Descricao = message.getString("Descricao")
                            val EnderecoAnexo = message.getString("EnderecoAnexo")
                            val DataHora = message.getString("DataHora")
                            val NOportunidade = message.getInt("NOportunidade")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A relação \'$Titulo\' com o cliente \'${GlobalVariables.detalhesNomeCliente}\' foi estabelecida com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerRelacoesEstabelecidas())
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

    private fun putStatus(
        context: Context,
        titulo: String,
        descricao: String,
        enderecoAnexo: String
    ) {
        data class Post(val Titulo: String, val Descricao: String, val EnderecoAnexo: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, descricao, enderecoAnexo)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/status/" + GlobalVariables.detalhesNumStatus)
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
                            builder.setMessage("A relação \'$titulo\' estabelecida com o cliente \'${GlobalVariables.detalhesNomeCliente}\' foi alterada com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerRelacoesEstabelecidas())
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