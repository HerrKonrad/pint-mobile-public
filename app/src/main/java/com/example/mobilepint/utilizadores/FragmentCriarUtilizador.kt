package com.example.mobilepint.utilizadores

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.administracao.FragmentVerAdministracao
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.collections.ArrayList

class FragmentCriarUtilizador: Fragment() {

    data class ItemsCargos(val NCargo: Int, val Cargo: String)

    private lateinit var spinner_tipo_cargo: Spinner
    private lateinit var cargosList: ArrayList<ItemsCargos>
    private var globalVariables = GlobalVariables()
    private var dataNascimentoDate = Date()
    private var positionTipoCargo: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_utilizador, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var msgErrors = ""
        var strNome = ""
        var strEmail = ""
        var strDataNascimento = ""
        var strGenero = ""
        var strTelefonePessoal = ""
        var strLocalidade = ""
        var strLinkedin = ""

        val txt_titulo_janela = view.findViewById(R.id.criar_utilizador_txt_titulo) as TextView
        val txt_email = view.findViewById(R.id.criar_utilizador_txt_email) as TextView
        val txt_email_label = view.findViewById(R.id.criar_utilizador_txt_email_label) as TextView
        val edtxt_email = view.findViewById(R.id.criar_utilizador_edtxt_email) as EditText
        val edtxt_nome = view.findViewById(R.id.criar_utilizador_edtxt_nome) as EditText
        val edtxt_data_nascimento = view.findViewById(R.id.criar_utilizador_edtxt_data_nascimento) as EditText
        val rdbtn_feminino = view.findViewById(R.id.criar_utilizador_rdbtn_feminino) as RadioButton
        val rdbtn_masculino = view.findViewById(R.id.criar_utilizador_rdbtn_masculino) as RadioButton
        val rdbtn_outro = view.findViewById(R.id.criar_utilizador_rdbtn_outro) as RadioButton
        val edtxt_telefone_pessoal = view.findViewById(R.id.criar_utilizador_edtxt_telefone_pessoal) as EditText
        val edtxt_localidade = view.findViewById(R.id.criar_utilizador_edtxt_localidade) as EditText
        val edtxt_linkedin = view.findViewById(R.id.criar_utilizador_edtxt_linkedin) as EditText
        val btn_criar_utilizador = view.findViewById(R.id.criar_utilizador_btn_criar_utilizador) as Button

        spinner_tipo_cargo = view.findViewById(R.id.criar_utilizador_spinner_tipo_cargo) as Spinner
        cargosList = ArrayList()

        if (globalVariables.checkForInternet(requireContext())) {
            getCargos(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
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

        if (GlobalVariables.criarUtilizador) {
            txt_titulo_janela.text = getString(R.string.str_criar_utilizador)
            btn_criar_utilizador.text = getString(R.string.str_criar_utilizador)
            txt_email.text = getString(R.string.str_email_asterisco)
            txt_email_label.visibility = View.GONE
            edtxt_email.visibility = View.VISIBLE
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_utilizador)
            btn_criar_utilizador.text = getString(R.string.str_editar_utilizador)
            txt_email.text = getString(R.string.str_email)
            txt_email_label.visibility = View.VISIBLE
            edtxt_email.visibility = View.GONE

            val strFormattedBirthDate = if (GlobalVariables.detalhesDataNascimentoUtilizador.isNotBlank()) {
                try {
                    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val startDateTime = LocalDateTime.parse(GlobalVariables.detalhesDataNascimentoUtilizador, inputFormatter)
                    startDateTime.format(outputFormatter)
                } catch (dateTimeParseException: DateTimeParseException) {
                    dateTimeParseException.printStackTrace()
                    GlobalVariables.detalhesDataNascimentoUtilizador
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    GlobalVariables.detalhesDataNascimentoUtilizador
                }
            } else {
                GlobalVariables.detalhesDataNascimentoUtilizador
            }

            txt_email_label.text = GlobalVariables.detalhesEmailUtilizador
            edtxt_email.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesEmailUtilizador)
            edtxt_nome.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesNomeUtilizador)
            edtxt_data_nascimento.text = Editable.Factory.getInstance().newEditable(strFormattedBirthDate)
            edtxt_telefone_pessoal.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTelefoneUtilizador)
            edtxt_localidade.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesLocalidadeUtilizador)
            edtxt_linkedin.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesLinkedinUtilizador)

            when (GlobalVariables.detalhesGeneroUtilizador) {
                "Masculino" -> {
                    rdbtn_masculino.isChecked = true
                }

                "Feminino" -> {
                    rdbtn_feminino.isChecked = true
                }

                "Outro" -> {
                    rdbtn_outro.isChecked = true
                }

                else -> {
                    rdbtn_outro.isChecked = true
                }
            }
        }

        spinner_tipo_cargo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionTipoCargo = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btn_criar_utilizador.setOnClickListener {
            btn_criar_utilizador.isEnabled = false
            Handler().postDelayed({
                btn_criar_utilizador.isEnabled = true
            }, 10000)

            if (edtxt_nome.text.toString().isNotBlank()) {
                strNome = edtxt_nome.text.toString()
            } else {
                msgErrors += "O campo \'Nome\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_email.text.toString().isNotBlank()) {
                strEmail = edtxt_email.text.toString()
                if (!globalVariables.isValidEmail(strEmail)) {
                    msgErrors += "O e-mail inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                msgErrors += "O campo \'E-mail\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
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

            if (rdbtn_feminino.isChecked) {
                strGenero = rdbtn_feminino.text.toString()
            } else if (rdbtn_masculino.isChecked) {
                strGenero = rdbtn_masculino.text.toString()
            } else if (rdbtn_outro.isChecked) {
                strGenero = rdbtn_outro.text.toString()
            } else {
                msgErrors += "Por favor, selecione o seu género.\n"
            }

            if (edtxt_telefone_pessoal.text.toString().isNotBlank()) {
                strTelefonePessoal = edtxt_telefone_pessoal.text.toString()
                if (!globalVariables.isValidPhone(strTelefonePessoal)) {
                    msgErrors += "O número de telefone inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                strTelefonePessoal = ""
            }

            strLinkedin = edtxt_linkedin.text.toString().ifBlank { "" }
            strLocalidade = edtxt_localidade.text.toString().ifBlank { "" }

            if (msgErrors.isBlank()) {
                val numCargoSelecionado = cargosList[positionTipoCargo].NCargo
                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarUtilizador) {
                        postAdminRegister(
                            requireContext(),
                            strEmail,
                            strNome,
                            numCargoSelecionado,
                            strTelefonePessoal,
                            strLinkedin,
                            dataNascimentoDate,
                            strGenero,
                            strLocalidade
                        )
                    } else {
                        putAdminRegister(
                            requireContext(),
                            strEmail,
                            strNome,
                            numCargoSelecionado,
                            strTelefonePessoal,
                            strLinkedin,
                            dataNascimentoDate,
                            strGenero,
                            strLocalidade
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
            if (GlobalVariables.criarUtilizador) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesUtilizador())
                fragment.commit()
            }
        }
    }

    private fun getCargos(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/cargos")
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
                                val NCargo = get.getInt("NCargo")
                                val Cargo = get.getString("Cargo")
                                cargosList.add(ItemsCargos(NCargo, Cargo))
                            }

                            val adapterCargos = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                cargosList.map { it.Cargo }.toTypedArray()
                            )
                            adapterCargos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner_tipo_cargo.adapter = adapterCargos

                            if (GlobalVariables.criarUtilizador) {
                                positionTipoCargo = cargosList.indexOfFirst { it.Cargo == "Utilizador Externo" }
                                if (positionTipoCargo != -1) {
                                    try {
                                        spinner_tipo_cargo.setSelection(positionTipoCargo)
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
                            } else {
                                val numCargo = GlobalVariables.detalhesNumCargoUtilizador
                                positionTipoCargo = cargosList.indexOfFirst { it.NCargo == numCargo }
                                if (positionTipoCargo != -1) {
                                    try {
                                        spinner_tipo_cargo.setSelection(positionTipoCargo)
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
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

    private fun postAdminRegister(
        context: Context,
        email: String,
        nome: String,
        nCargo: Int,
        telefone: String,
        linkedin: String,
        dataNasicmento: Date,
        genero: String,
        localidade: String
    ) {
        data class Post(
            val Email: String,
            val Nome: String,
            val NCargo: Int,
            val Telefone: String,
            val Linkedin: String,
            val DataNascimento: Date,
            val Genero: String,
            val Localidade: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(
            email,
            nome,
            nCargo,
            telefone,
            linkedin,
            dataNasicmento,
            genero,
            localidade
        )

        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/adminregister")
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
                            val NUsuario = message.getInt("NUsuario")
                            val Nome = message.getString("Nome")
                            val Email = message.getString("Email")
                            val NCargo = message.getInt("NCargo")
                            val Telefone = message.getString("Telefone")
                            val Linkedin = message.getString("Linkedin")
                            val Curriculo = message.getString("CV")
                            val Foto = message.getString("Foto")
                            val DataNascimento = message.getString("DataNascimento")
                            val Genero = message.getString("Genero")
                            val Localidade = message.getString("Localidade")
                            val Estado = message.getInt("Estado")
                            val DataHoraRegisto = message.getString("DataHoraRegisto")

                            val detalhes = "<b>O utilizador \'$Email\' foi criado com sucesso.</b><br><br> O utilizador deve verificar o seu e-mail e clicar no link de ativação para começar a usar a plataforma."
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setMessage(Html.fromHtml(detalhes))
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
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

    private fun putAdminRegister(
        context: Context,
        email: String,
        nome: String,
        nCargo: Int,
        telefone: String,
        linkedin: String,
        dataNasicmento: Date,
        genero: String,
        localidade: String
    ) {
        data class Post(
            val Nome: String,
            val NCargo: Int,
            val Telefone: String,
            val Linkedin: String,
            val DataNascimento: Date,
            val Genero: String,
            val Localidade: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(
            nome,
            nCargo,
            telefone,
            linkedin,
            dataNasicmento,
            genero,
            localidade
        )
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios/" + GlobalVariables.detalhesNumUtilizador)
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
                            builder.setMessage("O utilizador \'$email\' foi alterado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
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