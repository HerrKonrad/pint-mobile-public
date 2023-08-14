package com.example.mobilepint

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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

class LoginActivity : AppCompatActivity() {

    private var globalVariables = GlobalVariables()
    private var lastBackPressTime: Long = 0
    private val RC_SIGN_IN = 1
    private lateinit var toast: Toast
    private lateinit var loginDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        var strEmail = ""
        var strPassword = ""
        var msgErrors = ""

        val edtxt_email = findViewById<View>(R.id.login_edtxt_email) as EditText
        val edtxt_password = findViewById<View>(R.id.login_edtxt_password) as EditText
        val btn_iniciar_sessao = findViewById<View>(R.id.login_btn_iniciar_sessao) as Button
        val txt_esqueceu_palavra_passe = findViewById<View>(R.id.login_txt_esqueceu_palavra_passe) as TextView
        val imgbtn_iniciar_com_facebook = findViewById<View>(R.id.login_imgbtn_iniciar_com_facebook) as ImageButton
        val imgbtn_iniciar_com_google = findViewById<View>(R.id.login_imgbtn_iniciar_com_google) as ImageButton
        val txt_criar_conta = findViewById<View>(R.id.login_txt_crie_uma_aqui) as TextView
        GlobalVariables.preferencesLogin = getSharedPreferences(GlobalVariables.LOGIN, Context.MODE_PRIVATE)

        if (checkPreferencesLogin()) {
            try {
                edtxt_email.setText(GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_EMAIL, ""))
                edtxt_password.setText(GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_PASS, ""))
            } catch (e: Exception) {
                e.printStackTrace()
                edtxt_email.setText("")
                edtxt_password.setText("")
            }
        } else {
            edtxt_email.setText("")
            edtxt_password.setText("")
        }

        btn_iniciar_sessao.setOnClickListener {
            btn_iniciar_sessao.isEnabled = false
            Handler().postDelayed({
                btn_iniciar_sessao.isEnabled = true
            }, 10000)

            if (edtxt_email.text.toString().isNotBlank()) {
                strEmail = edtxt_email.text.toString()
            } else {
                msgErrors += "O campo \'E-mail\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_password.text.toString().isNotBlank()) {
                strPassword = edtxt_password.text.toString()
            } else {
                msgErrors += "O campo \'Palavra-passe\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(applicationContext)) {
                    val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
                    val builder = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)

                    val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                    val dialogDescription = dialogView.findViewById<TextView>(R.id.dialog_description)

                    dialogTitle.text = getString(R.string.str_iniciar_sessao)
                    dialogDescription.text = getString(R.string.str_aguarde_reticencias)

                    loginDialog = builder.create()
                    loginDialog.show()
                    getLoginData(this, strEmail, strPassword)
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

        txt_esqueceu_palavra_passe.setOnClickListener {
            val intent = Intent(this, RecuperarPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        imgbtn_iniciar_com_facebook.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Aviso")
            builder.setMessage("Funcionalidade temporariamente indisponível.")
            builder.setIcon(R.drawable.ic_information)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        imgbtn_iniciar_com_google.setOnClickListener {
            if (globalVariables.checkForInternet(applicationContext)) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            } else {
                Toast.makeText(applicationContext, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
            }
        }

        txt_criar_conta.setOnClickListener {
            GlobalVariables.criarContaVerInicio = false
            val intent = Intent(this, CriarContaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Pressione novamente para sair.", Toast.LENGTH_LONG)
            toast.show()
            this.lastBackPressTime = System.currentTimeMillis()
        } else {
            toast.cancel()
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun checkPreferencesLogin(): Boolean {
        val containsEmail = GlobalVariables.preferencesLogin?.contains(GlobalVariables.LOGIN_EMAIL) == true
        val containsPass = GlobalVariables.preferencesLogin?.contains(GlobalVariables.LOGIN_PASS) == true
        return containsEmail && containsPass
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idGoogle = account.id.toString()
            val emailGoogle = account.email.toString()
            val nomeGoogle = account.displayName.toString()
            val fotoGoogle = account.photoUrl.toString()

            if (globalVariables.checkForInternet(applicationContext)) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
                val builder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)

                val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                val dialogDescription = dialogView.findViewById<TextView>(R.id.dialog_description)

                dialogTitle.text = getString(R.string.str_iniciar_sessao)
                dialogDescription.text = getString(R.string.str_aguarde_reticencias)

                loginDialog = builder.create()
                loginDialog.show()
                getLoginGoogle(this, idGoogle, emailGoogle, nomeGoogle, fotoGoogle)
            } else {
                Toast.makeText(applicationContext, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Erro")
            builder.setMessage("Sign-in result failed code: ${e.statusCode}")
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun getLoginData(context: Context, email: String, password: String) {
        data class Post(val Email: String, val Senha: String)

        val client = OkHttpClient()
        val post = Post(email, password)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")

                try {
                    runOnUiThread {
                        loginDialog.dismiss()
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Erro")
                        builder.setMessage("Falha no pedido: ${e.message}")
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
                            loginDialog.dismiss()
                            val message = jsonObject.getString("message")
                            GlobalVariables.token = message

                            var preferencesLoginEmail: String?
                            var preferencesLoginPass: String?

                            if (checkPreferencesLogin()) {
                                try {
                                    preferencesLoginEmail = GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_EMAIL, "")
                                    preferencesLoginPass = GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_PASS, "")
                                } catch (e: Exception) {
                                    preferencesLoginEmail = ""
                                    preferencesLoginPass = ""
                                }

                                if (preferencesLoginEmail?.isNotBlank() == true && preferencesLoginPass?.isNotBlank() == true) {
                                    if (email != preferencesLoginEmail || password != preferencesLoginPass) {
                                        val dia = AlertDialog.Builder(context)
                                        dia.setTitle("Iniciar sessão")
                                        dia.setMessage("Deseja guardar os seus dados de sessão para sessões futuras?")
                                        dia.setIcon(R.drawable.ic_information)
                                        dia.setPositiveButton("Sim") { dialog, _ ->
                                            val editorLogin = GlobalVariables.preferencesLogin!!.edit()
                                            editorLogin.clear()
                                            editorLogin.putString(GlobalVariables.LOGIN_EMAIL, email)
                                            editorLogin.putString(GlobalVariables.LOGIN_PASS, password)
                                            editorLogin.apply()
                                            dialog.dismiss()
                                            if (globalVariables.checkForInternet(context)) {
                                                getAuthenticatedUserData(context)
                                            } else {
                                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        dia.setNegativeButton("Não") { dialog, _ ->
                                            dialog.dismiss()
                                            if (globalVariables.checkForInternet(context)) {
                                                getAuthenticatedUserData(context)
                                            } else {
                                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        dia.show()
                                    } else {
                                        if (globalVariables.checkForInternet(context)) {
                                            getAuthenticatedUserData(context)
                                        } else {
                                            Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                val dia = AlertDialog.Builder(context)
                                dia.setTitle("Iniciar sessão")
                                dia.setMessage("Deseja guardar os seus dados de sessão para sessões futuras?")
                                dia.setIcon(R.drawable.ic_information)
                                dia.setPositiveButton("Sim") { dialog, _ ->
                                    val editorLogin = GlobalVariables.preferencesLogin!!.edit()
                                    editorLogin.clear()
                                    editorLogin.putString(GlobalVariables.LOGIN_EMAIL, email)
                                    editorLogin.putString(GlobalVariables.LOGIN_PASS, password)
                                    editorLogin.apply()
                                    dialog.dismiss()
                                    if (globalVariables.checkForInternet(context)) {
                                        getAuthenticatedUserData(context)
                                    } else {
                                        Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                dia.setNegativeButton("Não") { dialog, _ ->
                                    dialog.dismiss()
                                    if (globalVariables.checkForInternet(context)) {
                                        getAuthenticatedUserData(context)
                                    } else {
                                        Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                dia.show()
                            }
                        } else {
                            loginDialog.dismiss()
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
                    loginDialog.dismiss()
                    Toast.makeText(context, "JSON error: ${jsonException.message}", Toast.LENGTH_LONG).show()
                    jsonException.printStackTrace()
                } catch (exception: Exception) {
                    loginDialog.dismiss()
                    exception.printStackTrace()
                }
            }
        })
    }

    private fun getLoginGoogle(
        context: Context,
        idGoogle: String,
        email: String,
        nome: String,
        foto: String
    ) {
        data class Post(val IDGoogle: String, val Email: String, val Nome: String, val Foto: String)

        val client = OkHttpClient()
        val post = Post(idGoogle, email, nome, foto)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/googleandroid")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")

                try {
                    runOnUiThread {
                        loginDialog.dismiss()
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Erro")
                        builder.setMessage("Falha no pedido: ${e.message}")
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
                            loginDialog.dismiss()
                            val message = jsonObject.getString("message")
                            GlobalVariables.token = message
                            if (globalVariables.checkForInternet(context)) {
                                getAuthenticatedUserData(context)
                            } else {
                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            loginDialog.dismiss()
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
                    loginDialog.dismiss()
                    Toast.makeText(context, "JSON error: ${jsonException.message}", Toast.LENGTH_LONG).show()
                    jsonException.printStackTrace()
                } catch (exception: Exception) {
                    loginDialog.dismiss()
                    exception.printStackTrace()
                }
            }
        })
    }

    private fun getAuthenticatedUserData(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/checktoken")
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
                            val NumUtilizador = message.getInt("NUsuario")
                            val NumCargo = message.getInt("NCargo")
                            val Nome = message.getString("Nome")
                            val Email = message.getString("Email")
                            val Foto = message.getString("Foto")

                            GlobalVariables.idUtilizadorAutenticado = NumUtilizador
                            GlobalVariables.idCargoUtilizadorAutenticado = NumCargo
                            GlobalVariables.nomeUtilizadorAutenticado = Nome
                            GlobalVariables.emailUtilizadorAutenticado = Email
                            GlobalVariables.fotoUtilizadorAutenticado = Foto

                            val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent)
                            finish()
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