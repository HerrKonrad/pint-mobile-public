package com.example.mobilepint.configuracoes

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentDetalhesPerfil : Fragment() {

    private lateinit var txt_email_label: TextView
    private lateinit var txt_data_registo_label: TextView
    private lateinit var edtxt_nome: EditText
    private lateinit var edtxt_telefone_pessoal: EditText
    private lateinit var edtxt_data_nascimento: EditText
    private lateinit var spinner_genero: Spinner
    private lateinit var edtxt_linkedin: EditText
    private lateinit var edtxt_localidade: EditText
    private lateinit var edtxt_curriculo: EditText
    private lateinit var imgbtn_carregar_ficheiro: ImageButton
    private lateinit var imgbtn_download_ficheiro: ImageButton
    private lateinit var edtxt_foto: EditText
    private lateinit var imgbtn_carregar_imagem: ImageButton
    private lateinit var imgbtn_download_imagem: ImageButton
    private lateinit var img_foto_perfil: ImageView
    private lateinit var btn_atualizar_perfil: Button
    private var globalVariables = GlobalVariables()
    private var dataNascimentoDate = Date()
    private var REQUEST_CODE_IMAGE_PICKER = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalhes_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strNome = ""
        var strTelefonePessoal = ""
        var strDataNascimento = ""
        var strGenero = ""
        var strLinkedin = ""
        var strLocalidade = ""
        var strCurriculo = ""
        var strFoto = ""

        txt_email_label = view.findViewById(R.id.detalhes_perfil_txt_email_label)
        txt_data_registo_label = view.findViewById(R.id.detalhes_perfil_txt_data_registo_label)
        edtxt_nome = view.findViewById(R.id.detalhes_perfil_edtxt_nome)
        edtxt_telefone_pessoal = view.findViewById(R.id.detalhes_perfil_edtxt_telefone_pessoal)
        edtxt_data_nascimento = view.findViewById(R.id.detalhes_perfil_edtxt_data_nascimento)
        spinner_genero = view.findViewById(R.id.detalhes_perfil_spinner_genero)
        edtxt_linkedin = view.findViewById(R.id.detalhes_perfil_edtxt_linkedin)
        edtxt_localidade = view.findViewById(R.id.detalhes_perfil_edtxt_localidade)
        edtxt_curriculo = view.findViewById(R.id.detalhes_perfil_edtxt_curriculo)
        imgbtn_carregar_ficheiro = view.findViewById(R.id.detalhes_perfil_imgbtn_carregar_ficheiro)
        imgbtn_download_ficheiro = view.findViewById(R.id.detalhes_perfil_imgbtn_download_ficheiro)
        edtxt_foto = view.findViewById(R.id.detalhes_perfil_edtxt_foto)
        imgbtn_carregar_imagem = view.findViewById(R.id.detalhes_perfil_imgbtn_carregar_imagem)
        imgbtn_download_imagem = view.findViewById(R.id.detalhes_perfil_imgbtn_download_imagem)
        img_foto_perfil = view.findViewById(R.id.detalhes_perfil_img_foto_perfil)
        btn_atualizar_perfil = view.findViewById(R.id.detalhes_perfil_btn_atualizar_perfil)

        if (globalVariables.checkForInternet(requireContext())) {
            getUserData(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.str_array_generos,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner_genero.adapter = adapter
            spinner_genero.setSelection(0)
        }

        edtxt_data_nascimento.isFocusable = false
        edtxt_data_nascimento.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)
                val date = calendar.time
                val formattedDate = globalVariables.dateFormat.format(date)
                edtxt_data_nascimento.setText(formattedDate)
            }
            datePicker.show()
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
            val urlCurriculo = edtxt_curriculo.text.toString()
            if (urlCurriculo.isNotBlank()) {
                val fileName = urlCurriculo.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlCurriculo, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
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
            val urlFoto = edtxt_foto.text.toString()
            if (urlFoto.isNotBlank()) {
                val fileName = urlFoto.substringAfterLast("/").substringBeforeLast("?")
                downloadAndSaveFile(requireContext(), urlFoto, fileName)
            } else {
                Toast.makeText(requireContext(), "URL inválido. Não é possível realizar o download.", Toast.LENGTH_LONG).show()
            }
        }

        btn_atualizar_perfil.setOnClickListener {
            btn_atualizar_perfil.isEnabled = false
            Handler().postDelayed({
                btn_atualizar_perfil.isEnabled = true
            }, 10000)

            if (edtxt_nome.text.toString().isNotBlank()) {
                strNome = edtxt_nome.text.toString()
            } else {
                msgErrors += "O campo \'Nome\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_data_nascimento.text.toString().isNotBlank()) {
                strDataNascimento = edtxt_data_nascimento.text.toString()
                if (!globalVariables.isValidDate(strDataNascimento)) {
                    msgErrors += "Data inválida. Por favor, insira uma data válida.\n"
                } else {
                    dataNascimentoDate = globalVariables.dateFormat.parse(strDataNascimento) as Date
                }
            } else {
                msgErrors += "O campo \'Data de Nascimento\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_telefone_pessoal.text.toString().isNotBlank()) {
                strTelefonePessoal = edtxt_telefone_pessoal.text.toString()
                if (!globalVariables.isValidPhone(strTelefonePessoal)) {
                    msgErrors += "Por favor, insira um número de telefone correto.\n"
                }
            } else {
                strTelefonePessoal = ""
            }

            strGenero = spinner_genero.selectedItem.toString().ifBlank { "" }
            strLinkedin = edtxt_linkedin.text.toString().ifBlank { "" }
            strLocalidade = edtxt_localidade.text.toString().ifBlank { "" }
            strCurriculo = edtxt_curriculo.text.toString().ifBlank { "" }
            strFoto = edtxt_foto.text.toString().ifBlank { "" }

            if (strGenero.isBlank()) {
                msgErrors += "Por favor, selecione o seu género.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    putUserData(
                        requireContext(),
                        strNome,
                        strTelefonePessoal,
                        strLinkedin,
                        strCurriculo,
                        strFoto,
                        dataNascimentoDate,
                        strGenero,
                        strLocalidade
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
            fragment.replace(R.id.fragment_container, FragmentConfiguracoes())
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

    private fun getUserData(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios/" + GlobalVariables.idUtilizadorAutenticado)
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
                            val NUsuario = message.getInt("NUsuario")
                            val NCargo = message.getInt("NCargo")
                            val Estado = message.getInt("Estado")
                            var Nome = message.getString("Nome")
                            var Email = message.getString("Email")
                            var Telefone = message.getString("Telefone")
                            var Linkedin = message.getString("Linkedin")
                            var CV = message.getString("CV")
                            var Foto = message.getString("Foto")
                            var DataNascimento = message.getString("DataNascimento")
                            var Genero = message.getString("Genero")
                            var Localidade = message.getString("Localidade")
                            var DataHoraRegisto = message.getString("DataHoraRegisto")

                            if (Nome.isNullOrBlank() || Nome == "null") {
                                Nome = ""
                            }
                            if (Email.isNullOrBlank() || Email == "null") {
                                Email = ""
                            }
                            if (Telefone.isNullOrBlank() || Telefone == "null") {
                                Telefone = ""
                            }
                            if (Linkedin.isNullOrBlank() || Linkedin == "null") {
                                Linkedin = ""
                            }
                            if (CV.isNullOrBlank() || CV == "null") {
                                CV = ""
                            }
                            if (Foto.isNullOrBlank() || Foto == "null") {
                                img_foto_perfil.visibility = View.GONE
                                Foto = ""
                            } else {
                                img_foto_perfil.visibility = View.VISIBLE
                                Glide.with(context)
                                    .load(Foto)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .apply(RequestOptions().fitCenter())
                                    .into(img_foto_perfil)
                            }
                            DataNascimento = if (DataNascimento.isNullOrBlank() || DataNascimento == "null") {
                                ""
                            } else {
                                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val dateTime = LocalDateTime.parse(DataNascimento, inputFormatter)
                                dateTime.format(outputFormatter)
                            }
                            if (Genero.isNullOrBlank() || Genero == "null") {
                                Genero = ""
                            }
                            if (Localidade.isNullOrBlank() || Localidade == "null") {
                                Localidade = ""
                            }
                            DataHoraRegisto = if (DataHoraRegisto.isNullOrBlank() || DataHoraRegisto == "null") {
                                ""
                            } else {
                                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val dateTime = LocalDateTime.parse(DataHoraRegisto, inputFormatter)
                                dateTime.format(outputFormatter)
                            }

                            txt_email_label.text = Email
                            txt_data_registo_label.text = DataHoraRegisto
                            edtxt_nome.text = Editable.Factory.getInstance().newEditable(Nome)
                            edtxt_telefone_pessoal.text = Editable.Factory.getInstance().newEditable(Telefone)
                            edtxt_data_nascimento.text = Editable.Factory.getInstance().newEditable(DataNascimento)
                            edtxt_linkedin.text = Editable.Factory.getInstance().newEditable(Linkedin)
                            edtxt_localidade.text = Editable.Factory.getInstance().newEditable(Localidade)
                            edtxt_curriculo.text = Editable.Factory.getInstance().newEditable(CV)
                            edtxt_foto.text = Editable.Factory.getInstance().newEditable(Foto)

                            when (Genero) {
                                "Feminino" -> spinner_genero.setSelection(0)
                                "Masculino" -> spinner_genero.setSelection(1)
                                "Outro" -> spinner_genero.setSelection(2)
                                else -> spinner_genero.setSelection(2)
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
                            edtxt_curriculo.text = Editable.Factory.getInstance().newEditable(message)
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

    private fun uploadImage(context: Context, file: File) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("imagem", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
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
                            edtxt_foto.text = Editable.Factory.getInstance().newEditable(message)
                            img_foto_perfil.visibility = View.VISIBLE
                            Glide.with(context)
                                .load(message)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .apply(RequestOptions().fitCenter())
                                .into(img_foto_perfil)
                        } else {
                            img_foto_perfil.visibility = View.GONE
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

    private fun putUserData(
        context: Context,
        nome: String,
        telefone: String,
        linkedin: String,
        curriculo: String,
        foto: String,
        dataNascimento: Date,
        genero: String,
        localidade: String
    ) {
        data class Post(
            val Nome: String,
            val Telefone: String,
            val Linkedin: String,
            val CV: String,
            val Foto: String,
            val DataNascimento: Date,
            val Genero: String,
            val Localidade: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(nome, telefone, linkedin, curriculo, foto, dataNascimento, genero, localidade)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios/" + GlobalVariables.idUtilizadorAutenticado)
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
                            GlobalVariables.nomeUtilizadorAutenticado = nome

                            val detalhes = "<b>O seu perfil foi atualizado com sucesso.</b><br><br> Para garantir o funcionamento adequado da aplicação, é recomendado que a reinicie agora."
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(Html.fromHtml(detalhes))
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentConfiguracoes())
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