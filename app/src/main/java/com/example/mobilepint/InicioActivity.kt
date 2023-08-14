package com.example.mobilepint

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import java.util.TimeZone

class InicioActivity : AppCompatActivity() {

    private var globalVariables = GlobalVariables()
    private var lastBackPressTime: Long = 0
    private lateinit var toast: Toast
    private lateinit var loginDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_inicio)

        val timeZone = TimeZone.getTimeZone("Europe/Lisbon")
        TimeZone.setDefault(timeZone)

        val btn_iniciar_sessao = findViewById<View>(R.id.inicio_btn_iniciar_sessao) as Button
        val btn_criar_conta = findViewById<View>(R.id.inicio_btn_criar_conta) as Button
        GlobalVariables.preferencesLogin = getSharedPreferences(GlobalVariables.LOGIN, Context.MODE_PRIVATE)

        if (checkPreferencesLogin()) {
            try {
                val email = GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_EMAIL, "").toString()
                val pass = GlobalVariables.preferencesLogin?.getString(GlobalVariables.LOGIN_PASS, "").toString()

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
                    getLoginData(this, email, pass)
                } else {
                    Toast.makeText(applicationContext, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btn_iniciar_sessao.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_criar_conta.setOnClickListener {
            GlobalVariables.criarContaVerInicio = true
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

    private fun checkPreferencesLogin(): Boolean {
        val containsEmail = GlobalVariables.preferencesLogin?.contains(GlobalVariables.LOGIN_EMAIL) == true
        val containsPass = GlobalVariables.preferencesLogin?.contains(GlobalVariables.LOGIN_PASS) == true
        return containsEmail && containsPass
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
                    Toast.makeText(context, "JSON error: ${jsonException.message}", Toast.LENGTH_LONG).show()
                    jsonException.printStackTrace()
                } catch (exception: Exception) {
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