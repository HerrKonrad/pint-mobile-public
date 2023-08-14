package com.example.mobilepint.vagas

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

class FragmentRecomendarVaga : Fragment() {

    private lateinit var edtxt_endereco_ficheiro: EditText
    private var globalVariables = GlobalVariables()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recomendar_vaga, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strNomeCandidato = ""
        var strEmailCandidato = ""
        var strTelefoneCandidato = ""
        var strLinkedinCandidato = ""
        var strEnderecoFicheiro = ""
        var msgErrors = ""

        val edtxt_nome_candidato = view.findViewById(R.id.recomendar_vaga_edtxt_nome_candidato) as EditText
        val edtxt_email_candidato = view.findViewById(R.id.recomendar_vaga_edtxt_email_candidato) as EditText
        val edtxt_telefone_candidato = view.findViewById(R.id.recomendar_vaga_edtxt_telefone_candidato) as EditText
        val edtxt_linkedin_candidato = view.findViewById(R.id.recomendar_vaga_edtxt_linkedin_candidato) as EditText
        edtxt_endereco_ficheiro = view.findViewById(R.id.recomendar_vaga_edtxt_endereco_ficheiro) as EditText
        val imgbtn_carregar_ficheiro = view.findViewById(R.id.recomendar_vaga_imgbtn_carregar_ficheiro) as ImageButton
        val imgbtn_download_ficheiro = view.findViewById(R.id.recomendar_vaga_imgbtn_download_ficheiro) as ImageButton
        val btn_recomendar_vaga = view.findViewById(R.id.recomendar_vaga_btn_recomendar_vaga) as Button

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
            val urlCurriculo = edtxt_endereco_ficheiro.text.toString()
            if (urlCurriculo.isNotBlank()) {
                val fileName = urlCurriculo.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlCurriculo, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
            }
        }

        btn_recomendar_vaga.setOnClickListener {
            btn_recomendar_vaga.isEnabled = false
            Handler().postDelayed({
                btn_recomendar_vaga.isEnabled = true
            }, 10000)

            if (edtxt_nome_candidato.text.toString().isNotBlank()) {
                strNomeCandidato = edtxt_nome_candidato.text.toString()
            } else {
                msgErrors += "O campo \'Nome do candidato\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_email_candidato.text.toString().isNotBlank()) {
                strEmailCandidato = edtxt_email_candidato.text.toString()
                if (!globalVariables.isValidEmail(strEmailCandidato)) {
                    msgErrors += "O e-mail inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                msgErrors += "O campo \'E-mail do candidato\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_telefone_candidato.text.toString().isNotBlank()) {
                strTelefoneCandidato = edtxt_telefone_candidato.text.toString()
                if (!globalVariables.isValidPhone(strTelefoneCandidato)) {
                    msgErrors += "O número de telefone inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                strTelefoneCandidato = ""
            }

            strLinkedinCandidato = edtxt_linkedin_candidato.text.toString().ifBlank { "" }
            strEnderecoFicheiro = edtxt_endereco_ficheiro.text.toString().ifBlank { "" }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    postIndicacao(
                        requireContext(),
                        GlobalVariables.detalhesNumVaga,
                        GlobalVariables.idUtilizadorAutenticado,
                        strNomeCandidato,
                        strEmailCandidato,
                        strTelefoneCandidato,
                        strLinkedinCandidato,
                        strEnderecoFicheiro
                    )
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
            fragment.replace(R.id.fragment_container, FragmentDetalhesVaga())
            fragment.commit()
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
                            edtxt_endereco_ficheiro.text = Editable.Factory.getInstance().newEditable(message)
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

    private fun postIndicacao(
        context: Context,
        numVaga: Int,
        numUsuario: Int,
        nomeCand: String,
        emailCand: String,
        telefoneCand: String,
        linkedinCand: String,
        curriculoCand: String
    ) {
        data class Post(
            val NVaga: Int,
            val NUsuario: Int,
            val NomeCand: String,
            val EmailCand: String,
            val TelefoneCand: String,
            val LINKEDIN: String,
            val CV: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(
            numVaga,
            numUsuario,
            nomeCand,
            emailCand,
            telefoneCand,
            linkedinCand,
            curriculoCand
        )

        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/indicacoes")
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
                            val NIndicacao = message.getInt("NIndicacao")
                            val NVaga = message.getInt("NVaga")
                            val NUsuario = message.getInt("NUsuario")
                            val NomeCand = message.getString("NomeCand")
                            val EmailCand = message.getString("EmailCand")
                            val TelefoneCand = message.getString("TelefoneCand")
                            val Linkedin = message.getString("LINKEDIN")
                            val CV = message.getString("CV")
                            val NomeUsuario = message.getString("NomeUsuario")
                            val NomeVaga = message.getString("NomeVaga")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A sua recomendação foi submetida com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerVagasDisponiveis())
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