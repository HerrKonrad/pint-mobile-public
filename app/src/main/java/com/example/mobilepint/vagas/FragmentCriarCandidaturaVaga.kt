package com.example.mobilepint.vagas

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.EnumCargos
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FragmentCriarCandidaturaVaga : Fragment() {

    private lateinit var txt_titulo_janela: TextView
    private lateinit var txt_nome_vaga_label: TextView
    private lateinit var txt_subtitulo_vaga_label: TextView
    private lateinit var txt_nome_candidato_label: TextView
    private lateinit var txt_email_candidato_label: TextView
    private lateinit var txt_telefone_pessoal_label: TextView
    private lateinit var txt_linkedin_label: TextView
    private lateinit var txt_localidade_label: TextView
    private lateinit var txt_data_nascimento_label: TextView
    private lateinit var txt_genero_label: TextView
    private lateinit var txt_curriculo: TextView
    private lateinit var edtxt_curriculo: EditText
    private lateinit var imgbtn_carregar_ficheiro: ImageButton
    private lateinit var imgbtn_download_ficheiro: ImageButton
    private lateinit var txt_observacao_candidato: TextView
    private lateinit var edtxt_pretencao_salarial: EditText
    private lateinit var txt_pretencao_salarial_label: TextView
    private lateinit var edtxt_mensagem: EditText
    private lateinit var txt_mensagem_label: TextView
    private lateinit var txt_data_criacao_label: TextView
    private lateinit var txt_estagio_label: TextView
    private lateinit var txt_estado_label: TextView
    private lateinit var btn_candidatar_me: Button
    private lateinit var btn_criar_reuniao_entrevista: Button
    private lateinit var btn_aceitar_candidatura: Button
    private lateinit var btn_rejeitar_candidatura: Button
    private lateinit var layout_mais_detalhes_candidato: LinearLayout
    private lateinit var layout_criar_candidatura: LinearLayout
    private lateinit var layout_ver_candidatura: LinearLayout
    private lateinit var layout_btns_permissoes: LinearLayout
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado
    private var numUtilizadorCandidato: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_candidatura_vaga, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var strPretencaoSalarial = ""
        var strMensagem = ""

        txt_titulo_janela = view.findViewById(R.id.candidatura_vaga_txt_titulo_janela)
        txt_nome_vaga_label = view.findViewById(R.id.candidatura_vaga_txt_nome_vaga_label)
        txt_subtitulo_vaga_label = view.findViewById(R.id.candidatura_vaga_txt_subtitulo_vaga_label)
        txt_nome_candidato_label = view.findViewById(R.id.candidatura_vaga_txt_nome_candidato_label)
        txt_email_candidato_label = view.findViewById(R.id.candidatura_vaga_txt_email_candidato_label)
        txt_telefone_pessoal_label = view.findViewById(R.id.candidatura_vaga_txt_telefone_pessoal_label)
        txt_linkedin_label = view.findViewById(R.id.candidatura_vaga_txt_linkedin_label)
        txt_localidade_label = view.findViewById(R.id.candidatura_vaga_txt_localidade_label)
        txt_data_nascimento_label = view.findViewById(R.id.candidatura_vaga_txt_data_nascimento_label)
        txt_genero_label = view.findViewById(R.id.candidatura_vaga_txt_genero_label)
        txt_curriculo = view.findViewById(R.id.candidatura_vaga_txt_curriculo)
        edtxt_curriculo = view.findViewById(R.id.candidatura_vaga_edtxt_curriculo)
        imgbtn_carregar_ficheiro = view.findViewById(R.id.candidatura_vaga_imgbtn_carregar_ficheiro)
        imgbtn_download_ficheiro = view.findViewById(R.id.candidatura_vaga_imgbtn_download_ficheiro)
        txt_observacao_candidato = view.findViewById(R.id.candidatura_vaga_txt_observacao_candidato)
        edtxt_pretencao_salarial = view.findViewById(R.id.candidatura_vaga_edtxt_pretencao_salarial)
        txt_pretencao_salarial_label = view.findViewById(R.id.candidatura_vaga_txt_pretencao_salarial_label)
        edtxt_mensagem = view.findViewById(R.id.candidatura_vaga_edtxt_mensagem)
        txt_mensagem_label = view.findViewById(R.id.candidatura_vaga_txt_mensagem_label)
        txt_data_criacao_label = view.findViewById(R.id.candidatura_vaga_txt_data_criacao_label)
        txt_estagio_label = view.findViewById(R.id.candidatura_vaga_txt_estagio_label)
        txt_estado_label = view.findViewById(R.id.candidatura_vaga_txt_estado_label)
        btn_candidatar_me = view.findViewById(R.id.candidatura_vaga_btn_candidatar_me)
        btn_criar_reuniao_entrevista = view.findViewById(R.id.candidatura_vaga_btn_criar_reuniao_entrevista)
        btn_aceitar_candidatura = view.findViewById(R.id.candidatura_vaga_btn_aceitar_candidatura)
        btn_rejeitar_candidatura = view.findViewById(R.id.candidatura_vaga_btn_rejeitar_candidatura)
        layout_mais_detalhes_candidato = view.findViewById(R.id.candidatura_vaga_linearLayout_mais_detalhes_candidato)
        layout_criar_candidatura = view.findViewById(R.id.candidatura_vaga_linearLayout_criar_candidatura)
        layout_ver_candidatura = view.findViewById(R.id.candidatura_vaga_linearLayout_ver_candidatura)
        layout_btns_permissoes = view.findViewById(R.id.candidatura_vaga_linearLayout_btns_permissoes)

        if (GlobalVariables.criarCandidatura) {
            txt_titulo_janela.text = getString(R.string.str_candidatar_me)
            txt_observacao_candidato.visibility = View.VISIBLE
            layout_mais_detalhes_candidato.visibility = View.GONE
            layout_criar_candidatura.visibility = View.VISIBLE
            layout_ver_candidatura.visibility = View.GONE
            layout_btns_permissoes.visibility = View.GONE
            btn_candidatar_me.visibility = View.VISIBLE

            numUtilizadorCandidato = GlobalVariables.idUtilizadorAutenticado
            txt_curriculo.text = getString(R.string.str_curriculo_asterisco)
            edtxt_curriculo.hint = getString(R.string.str_curriculo_asterisco)

            imgbtn_carregar_ficheiro.visibility = View.VISIBLE
            imgbtn_download_ficheiro.visibility = View.GONE
            val widthInDp = 100
            val density = resources.displayMetrics.density
            val widthInPx = (widthInDp * density).toInt()
            imgbtn_carregar_ficheiro.layoutParams.width = widthInPx

            txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVaga
            txt_subtitulo_vaga_label.text = GlobalVariables.detalhesSubtituloVaga
            txt_nome_candidato_label.text = GlobalVariables.nomeUtilizadorAutenticado
            txt_email_candidato_label.text = GlobalVariables.emailUtilizadorAutenticado
        } else {
            txt_titulo_janela.text = getString(R.string.str_detalhes_candidatura)
            txt_observacao_candidato.visibility = View.GONE
            layout_mais_detalhes_candidato.visibility = View.VISIBLE
            layout_criar_candidatura.visibility = View.GONE
            layout_ver_candidatura.visibility = View.VISIBLE
            btn_candidatar_me.visibility = View.GONE

            numUtilizadorCandidato = GlobalVariables.detalhesNumUsuarioCandidatura
            txt_curriculo.text = getString(R.string.str_curriculo)
            edtxt_curriculo.hint = getString(R.string.str_curriculo)

            imgbtn_carregar_ficheiro.visibility = View.GONE
            imgbtn_download_ficheiro.visibility = View.VISIBLE
            val widthInDp = 100
            val density = resources.displayMetrics.density
            val widthInPx = (widthInDp * density).toInt()
            imgbtn_download_ficheiro.layoutParams.width = widthInPx

            if (numCargo == EnumCargos.ADMINISTRADOR.numCargo || numCargo == EnumCargos.RECURSOS_HUMANOS.numCargo) {
                layout_btns_permissoes.visibility = View.VISIBLE
            } else {
                layout_btns_permissoes.visibility = View.GONE
            }

            val strEstadoCandidatura = if (GlobalVariables.detalhesEstadoCandidatura == 1) {
                "Ativa"
            } else {
                "Inativa"
            }

            val strFormattedDate = if (GlobalVariables.detalhesDataCandidatura.isNotBlank()) {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataCandidatura, inputFormatter)
                startDateTime.format(outputFormatter)
            } else {
                ""
            }

            txt_nome_vaga_label.text = GlobalVariables.detalhesNomeVagaCandidatura
            txt_subtitulo_vaga_label.text = GlobalVariables.detalhesSubtituloVagaCandidatura
            txt_nome_candidato_label.text = GlobalVariables.detalhesNomeUsuarioCandidatura
            txt_email_candidato_label.text = GlobalVariables.detalhesEmailUsuarioCandidatura
            txt_data_criacao_label.text = strFormattedDate
            txt_estagio_label.text = GlobalVariables.detalhesEstagioCandidatura
            txt_estado_label.text = strEstadoCandidatura
            txt_pretencao_salarial_label.text = getString(R.string.str_pretencao_salarial_label, GlobalVariables.detalhesPretencaoSalarial)
            txt_mensagem_label.text = GlobalVariables.detalhesMensagemCandidatura
        }

        if (globalVariables.checkForInternet(requireContext())) {
            getCandidateData(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
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

        btn_candidatar_me.setOnClickListener {
            btn_candidatar_me.isEnabled = false
            Handler().postDelayed({
                btn_candidatar_me.isEnabled = true
            }, 10000)

            strPretencaoSalarial = edtxt_pretencao_salarial.text.toString().ifBlank { "" }
            strMensagem = edtxt_mensagem.text.toString().ifBlank { "" }

            if (edtxt_curriculo.text.toString().isNotBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    postCandidatura(
                        requireContext(),
                        GlobalVariables.detalhesNumVaga,
                        GlobalVariables.idUtilizadorAutenticado,
                        strPretencaoSalarial,
                        strMensagem
                    )
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Aviso")
                builder.setMessage("O campo \'Currículo\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.")
                builder.setIcon(R.drawable.ic_information)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }

        btn_criar_reuniao_entrevista.setOnClickListener {
            GlobalVariables.criarReuniaoEntrevista = true
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.fragment_container, FragmentCriarReuniaoEntrevista())
            fragment.commit()
        }

        btn_aceitar_candidatura.setOnClickListener {
            if (GlobalVariables.detalhesEstagioCandidatura == "Aceite") {
                Toast.makeText(requireContext(), "A candidatura do candidato \'${GlobalVariables.detalhesNomeUsuarioCandidatura}\' foi aceite.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Aviso")
                    builder.setMessage("Tem a certeza que pretende aceitar a candidatura do candidato '${GlobalVariables.detalhesNomeUsuarioCandidatura}'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                        putCandidatura(requireContext(), "Aceite")
                    }
                    builder.setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            }
        }

        btn_rejeitar_candidatura.setOnClickListener {
            if (GlobalVariables.detalhesEstagioCandidatura == "Rejeitada") {
                Toast.makeText(requireContext(), "A candidatura do candidato \'${GlobalVariables.detalhesNomeUsuarioCandidatura}\' foi rejeitada.", Toast.LENGTH_LONG).show()
            } else {
                if (globalVariables.checkForInternet(requireContext())) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Aviso")
                    builder.setMessage("Tem a certeza que pretende rejeitar a candidatura do candidato '${GlobalVariables.detalhesNomeUsuarioCandidatura}'?")
                    builder.setIcon(R.drawable.ic_information)
                    builder.setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_LONG).show()
                        putCandidatura(requireContext(), "Rejeitada")
                    }
                    builder.setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (GlobalVariables.criarCandidatura) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesVaga())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerCandidaturasVagas())
                fragment.commit()
            }
        }
    }

    private fun getCandidateData(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios/" + numUtilizadorCandidato)
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
                                Foto = ""
                            }
                            DataNascimento = if (DataNascimento.isNullOrBlank() || DataNascimento == "null") {
                                ""
                            } else {
                                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val dateTime = LocalDateTime.parse(DataNascimento, inputFormatter)
                                val date = LocalDate.from(dateTime)
                                date.format(outputFormatter)
                            }
                            if (Genero.isNullOrBlank() || Genero == "null") {
                                Genero = ""
                            }
                            if (Localidade.isNullOrBlank() || Localidade == "null") {
                                Localidade = ""
                            }
                            if (DataHoraRegisto.isNullOrBlank() || DataHoraRegisto == "null") {
                                DataHoraRegisto = ""
                            }

                            txt_telefone_pessoal_label.text = Telefone
                            txt_linkedin_label.text = Linkedin
                            txt_localidade_label.text = Localidade
                            txt_data_nascimento_label.text = DataNascimento
                            txt_genero_label.text = Genero
                            edtxt_curriculo.text = Editable.Factory.getInstance().newEditable(CV)
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

                            if (globalVariables.checkForInternet(context)) {
                                putUserCurriculum(context, message)
                            } else {
                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
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

    private fun putUserCurriculum(context: Context, curriculo: String) {
        data class Post(val CV: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(curriculo)
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

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O seu currículo foi adicionado ao seu perfil para oportunidades futuras.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
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

    private fun postCandidatura(
        context: Context,
        numVaga: Int,
        numUsuario: Int,
        pretencaoSalarial: String,
        mensagem: String
    ) {
        data class Post(
            val NVaga: Int,
            val NUsuario: Int,
            val PretencaoSalarial: String,
            val Mensagem: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numVaga, numUsuario, pretencaoSalarial, mensagem)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/candidaturas")
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
                            val NCandidatura = message.getInt("NCandidatura")
                            val NVaga = message.getInt("NVaga")
                            val NUsuario = message.getInt("NUsuario")
                            val DataCandidatura = message.getString("DataCandidatura")
                            val PretencaoSalarial = message.getString("PretencaoSalarial")
                            val Mensagem = message.getString("Mensagem")
                            val Estado = message.getInt("Estado")
                            val NomeUsuario = message.getString("NomeUsuario")
                            val NomeVaga = message.getString("NomeVaga")
                            val EmailUsuario = message.getString("EmailUsuario")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("A sua candidatura foi submetida com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerCandidaturasVagas())
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

    private fun putCandidatura(context: Context, estagio: String) {
        data class Post(val Estado: Int, val Estagio: String)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(0, estagio)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/candidaturas/" + GlobalVariables.detalhesNumCandidatura)
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

                            val outputMessage = when (estagio) {
                                "Aceite" -> {
                                    "A candidatura do candidato \'${GlobalVariables.detalhesNomeUsuarioCandidatura}\' foi aceite."
                                }

                                "Rejeitada" -> {
                                    "A candidatura do candidato \'${GlobalVariables.detalhesNomeUsuarioCandidatura}\' foi rejeitada."
                                }

                                else -> {
                                    "A candidatura do candidato \'${GlobalVariables.detalhesNomeUsuarioCandidatura}\' foi alterada com sucesso."
                                }
                            }

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(outputMessage)
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerCandidaturasVagas())
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