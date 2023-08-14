package com.example.mobilepint.beneficios

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FragmentCriarBeneficio : Fragment() {

    private lateinit var txt_titulo_janela: TextView
    private lateinit var edtxt_nome_beneficio: EditText
    private lateinit var edtxt_subtitulo_beneficio: EditText
    private lateinit var edtxt_descricao_beneficio: EditText
    private lateinit var edtxt_endereco_imagem: EditText
    private lateinit var imgbtn_carregar_imagem: ImageButton
    private lateinit var imgbtn_download_imagem: ImageButton
    private lateinit var img_foto_beneficio: ImageView
    private lateinit var btn_criar_beneficio: Button
    private var globalVariables = GlobalVariables()
    private var REQUEST_CODE_IMAGE_PICKER = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_beneficio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strNomeBeneficio = ""
        var strSubtituloBeneficio = ""
        var strDescricaoBeneficio = ""
        var strEnderecoImagem = ""
        var msgErrors = ""

        txt_titulo_janela = view.findViewById(R.id.criar_beneficio_txt_titulo_janela)
        edtxt_nome_beneficio = view.findViewById(R.id.criar_beneficio_edtxt_nome_beneficio)
        edtxt_subtitulo_beneficio = view.findViewById(R.id.criar_beneficio_edtxt_subtitulo_beneficio)
        edtxt_descricao_beneficio = view.findViewById(R.id.criar_beneficio_edtxt_descricao_beneficio)
        edtxt_endereco_imagem = view.findViewById(R.id.criar_beneficio_edtxt_endereco_imagem)
        imgbtn_carregar_imagem = view.findViewById(R.id.criar_beneficio_imgbtn_carregar_imagem)
        imgbtn_download_imagem = view.findViewById(R.id.criar_beneficio_imgbtn_download_imagem)
        img_foto_beneficio = view.findViewById(R.id.criar_beneficio_img_foto_beneficio)
        btn_criar_beneficio = view.findViewById(R.id.criar_beneficio_btn_criar_beneficio)

        edtxt_endereco_imagem.isFocusable = false
        img_foto_beneficio.visibility = View.GONE

        if (GlobalVariables.criarBeneficio) {
            txt_titulo_janela.text = getString(R.string.str_criar_beneficio)
            btn_criar_beneficio.text = getString(R.string.str_criar_beneficio)
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_beneficio)
            btn_criar_beneficio.text = getString(R.string.str_editar_beneficio)

            edtxt_nome_beneficio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesNomeBeneficio)
            edtxt_subtitulo_beneficio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesSubtituloBeneficio)
            edtxt_descricao_beneficio.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoBeneficio)
            edtxt_endereco_imagem.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEnderecoImagemBeneficio)

            if (GlobalVariables.detalhesEnderecoImagemBeneficio.isBlank()) {
                img_foto_beneficio.visibility = View.GONE
            } else {
                img_foto_beneficio.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(GlobalVariables.detalhesEnderecoImagemBeneficio)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().fitCenter())
                    .into(img_foto_beneficio)
            }
        }

        imgbtn_carregar_imagem.setOnClickListener {
            imgbtn_carregar_imagem.isEnabled = false
            Handler().postDelayed({
                imgbtn_carregar_imagem.isEnabled = true
            }, 10000)

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
        }

        imgbtn_download_imagem.setOnClickListener {
            val urlImagem = edtxt_endereco_imagem.text.toString()
            if (urlImagem.isNotBlank()) {
                val fileName = urlImagem.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlImagem, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
            }
        }

        btn_criar_beneficio.setOnClickListener {
            btn_criar_beneficio.isEnabled = false
            Handler().postDelayed({
                btn_criar_beneficio.isEnabled = true
            }, 10000)

            if (edtxt_nome_beneficio.text.toString().isNotBlank()) {
                strNomeBeneficio = edtxt_nome_beneficio.text.toString()
            } else {
                msgErrors += "O campo \'Nome do benefício\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_subtitulo_beneficio.text.toString().isNotBlank()) {
                strSubtituloBeneficio = edtxt_subtitulo_beneficio.text.toString()
            } else {
                msgErrors += "O campo \'Subtítulo do benefício\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_beneficio.text.toString().isNotBlank()) {
                strDescricaoBeneficio = edtxt_descricao_beneficio.text.toString()
            } else {
                msgErrors += "O campo \'Descrição do benefício\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            strEnderecoImagem = edtxt_endereco_imagem.text.toString().ifBlank { "" }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarBeneficio) {
                        postBeneficio(
                            requireContext(),
                            strNomeBeneficio,
                            strSubtituloBeneficio,
                            strDescricaoBeneficio,
                            strEnderecoImagem
                        )
                    } else {
                        putBeneficio(
                            requireContext(),
                            strNomeBeneficio,
                            strSubtituloBeneficio,
                            strDescricaoBeneficio,
                            strEnderecoImagem
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
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentVerBeneficios())
            fragment.commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val inputStream = requireContext().contentResolver.openInputStream(uri!!)
            val fileName = globalVariables.getFileName(requireContext(), uri) ?: "image.png"
            val file = File(requireContext().cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
            uploadImage(requireContext(), file)
        }
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

    private fun uploadImage(context: Context, file: File) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("imagem", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Builder()
            .url(GlobalVariables.serverUrl + "/api/imagem")
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
                            edtxt_endereco_imagem.text = Editable.Factory.getInstance().newEditable(message)
                            img_foto_beneficio.visibility = View.VISIBLE
                            Glide.with(requireActivity())
                                .load(message)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .apply(RequestOptions().fitCenter())
                                .into(img_foto_beneficio)
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

    private fun postBeneficio(
        context: Context,
        nome: String,
        subtitulo: String,
        descricao: String,
        enderecoImagem: String
    ) {
        data class Post(
            val NomeBeneficio: String,
            val Subtitulo: String,
            val Descricao: String,
            val EnderecoImagem: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, subtitulo, descricao, enderecoImagem)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Builder()
            .url(GlobalVariables.serverUrl + "/api/beneficios")
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
                            val NBeneficio = message.getInt("NBeneficio")
                            val NomeBeneficio = message.getString("NomeBeneficio")
                            val Subtitulo = message.getString("Subtitulo")
                            val Descricao = message.getString("Descricao")
                            val EnderecoImagem = message.getString("EnderecoImagem")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O benefício \'$NomeBeneficio\' foi criado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerBeneficios())
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

    private fun putBeneficio(
        context: Context,
        nome: String,
        subtitulo: String,
        descricao: String,
        enderecoImagem: String
    ) {
        data class Post(
            val NomeBeneficio: String,
            val Subtitulo: String,
            val Descricao: String,
            val EnderecoImagem: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, subtitulo, descricao, enderecoImagem)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Builder()
            .url(GlobalVariables.serverUrl + "/api/beneficios/" + GlobalVariables.detalhesNumBeneficio)
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
                            builder.setMessage("O benefício \'$nome\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerBeneficios())
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