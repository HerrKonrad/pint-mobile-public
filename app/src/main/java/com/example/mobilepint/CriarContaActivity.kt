package com.example.mobilepint

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CriarContaActivity : AppCompatActivity() {

    private var globalVariables = GlobalVariables()
    private var dataNascimentoDate = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_criar_conta)

        var msgErrors = ""
        var strNome = ""
        var strEmail = ""
        var strPalavraPasse = ""
        var strConfirmarPalavraPasse = ""
        var strDataNascimento = ""
        var strGenero = ""
        var strTelefonePessoal = ""
        var strLinkedin = ""
        var strLocalidade = ""

        val edtxt_nome_proprio = findViewById<View>(R.id.criar_conta_edtxt_nome_proprio) as EditText
        val edtxt_apelido = findViewById<View>(R.id.criar_conta_edtxt_apelido) as EditText
        val edtxt_email = findViewById<View>(R.id.criar_conta_edtxt_email) as EditText
        val edtxt_palavra_passe = findViewById<View>(R.id.criar_conta_edtxt_palavra_passe) as EditText
        val edtxt_confirmar_palavra_passe = findViewById<View>(R.id.criar_conta_edtxt_confirmar_palavra_passe) as EditText
        val edtxt_data_nascimento = findViewById<View>(R.id.criar_conta_edtxt_data_nascimento) as EditText
        val rdbtn_feminino = findViewById<View>(R.id.criar_conta_rdbtn_feminino) as RadioButton
        val rdbtn_masculino = findViewById<View>(R.id.criar_conta_rdbtn_masculino) as RadioButton
        val rdbtn_outro = findViewById<View>(R.id.criar_conta_rdbtn_outro) as RadioButton
        val edtxt_telefone_pessoal = findViewById<View>(R.id.criar_conta_edtxt_telefone_pessoal) as EditText
        val edtxt_linkedin = findViewById<View>(R.id.criar_conta_edtxt_linkedin) as EditText
        val edtxt_localidade = findViewById<View>(R.id.criar_conta_edtxt_localidade) as EditText
        val btn_criar_nova_conta = findViewById<View>(R.id.criar_conta_btn_criar_nova_conta) as Button

        edtxt_data_nascimento.isFocusable = false
        edtxt_data_nascimento.setOnClickListener {
            val datePicker = DatePickerDialog(this)
            datePicker.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)
                val date = calendar.time
                val formattedDate = globalVariables.dateFormat.format(date)
                edtxt_data_nascimento.setText(formattedDate)
            }
            datePicker.show()
        }

        btn_criar_nova_conta.setOnClickListener {
            btn_criar_nova_conta.isEnabled = false
            Handler().postDelayed({
                btn_criar_nova_conta.isEnabled = true
            }, 10000)

            if (edtxt_nome_proprio.text.toString().isBlank()) {
                msgErrors += "O campo \'Nome próprio\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            } else if (edtxt_apelido.text.toString().isBlank()) {
                msgErrors += "O campo \'Apelido\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            } else {
                strNome = edtxt_nome_proprio.text.toString() + " " + edtxt_apelido.text.toString()
            }

            if (edtxt_email.text.toString().isNotBlank()) {
                strEmail = edtxt_email.text.toString()
                if (!globalVariables.isValidEmail(strEmail)) {
                    msgErrors += "O e-mail inserido é inválido. Por favor, verifique e tente novamente.\n"
                }
            } else {
                msgErrors += "O campo \'E-mail\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_palavra_passe.text.toString().isNotBlank()) {
                strPalavraPasse = edtxt_palavra_passe.text.toString()
            } else {
                msgErrors += "O campo \'Palavra-passe\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_confirmar_palavra_passe.text.toString().isNotBlank()) {
                strConfirmarPalavraPasse = edtxt_confirmar_palavra_passe.text.toString()
            } else {
                msgErrors += "O campo \'Confirmar palavra-passe\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (strPalavraPasse != strConfirmarPalavraPasse) {
                msgErrors += "As senhas inseridas não correspondem. Por favor, tente novamente.\n"
            } else {
                if (!globalVariables.isValidPassword(strPalavraPasse)) {
                    msgErrors += "A senha deve conter pelo menos uma letra maiúscula ou minúscula, pelo menos um dígito numérico e deve ter pelo menos 6 caracteres de comprimento.\n"
                }
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
                if (globalVariables.checkForInternet(applicationContext)) {
                    postRegister(
                        this,
                        strNome,
                        strEmail,
                        strPalavraPasse,
                        strTelefonePessoal,
                        strLinkedin,
                        dataNascimentoDate,
                        strGenero,
                        strLocalidade
                    )
                } else {
                    Toast.makeText(applicationContext, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            } else {
                val builder = AlertDialog.Builder(this)
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
    }

    override fun onBackPressed() {
        if (GlobalVariables.criarContaVerInicio) {
            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun postRegister(
        context: Context,
        nome: String,
        email: String,
        senha: String,
        telefone: String,
        linkedin: String,
        dataNasicmento: Date,
        genero: String,
        localidade: String
    ) {
        data class Post(
            val Nome: String,
            val Email: String,
            val Senha: String,
            val Telefone: String,
            val Linkedin: String,
            val DataNascimento: Date,
            val Genero: String,
            val Localidade: String
        )

        val client = OkHttpClient()
        val post = Post(
            nome,
            email,
            senha,
            telefone,
            linkedin,
            dataNasicmento,
            genero,
            localidade
        )
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")

                try {
                    runOnUiThread {
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
                    runOnUiThread {
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

                            val detalhes = "<b>A conta \'$Email\' foi criada com sucesso.</b><br><br> Por favor, verifique o seu e-mail e clique no link de ativação para começar a usar a nossa plataforma."
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(Html.fromHtml(detalhes))
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val intent = Intent(context, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
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