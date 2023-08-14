package com.example.mobilepint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.mobilepint.administracao.FragmentVerAdministracao
import com.example.mobilepint.administracao.ItemsReunioes
import com.example.mobilepint.beneficios.FragmentVerBeneficios
import com.example.mobilepint.configuracoes.FragmentConfiguracoes
import com.example.mobilepint.configuracoes.FragmentDetalhesPerfil
import com.example.mobilepint.configuracoes.FragmentEventosCalendario
import com.example.mobilepint.ideias.FragmentVerIdeias
import com.example.mobilepint.oportunidades.FragmentVerOportunidades
import com.example.mobilepint.vagas.FragmentVerVagasDisponiveis
import com.google.android.material.navigation.NavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var drawer: DrawerLayout
    private lateinit var img_foto_perfil: ImageView
    private lateinit var txt_nome_utilizador: TextView
    private lateinit var txt_email_utilizador: TextView
    private lateinit var utilizadoresReunioesList: ArrayList<ItemsUtilizadoresReunioes>
    private lateinit var reunioesList: ArrayList<ItemsReunioes>
    private var globalVariables = GlobalVariables()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navigationView = findViewById(R.id.nav_view)
        drawer = findViewById(R.id.drawer_layout)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = navigationView.getHeaderView(0)
        img_foto_perfil = headerView.findViewById(R.id.nav_header_img_foto_perfil) as ImageView
        txt_nome_utilizador = headerView.findViewById(R.id.nav_header_txt_nome_utilizador) as TextView
        txt_email_utilizador = headerView.findViewById(R.id.nav_header_txt_email_utilizador) as TextView
        txt_nome_utilizador.text = ""
        txt_email_utilizador.text = ""
        img_foto_perfil.setImageResource(R.drawable.user_profile_photo)
        utilizadoresReunioesList = ArrayList()
        reunioesList = ArrayList()

        val strNome = GlobalVariables.nomeUtilizadorAutenticado
        val strEmail = GlobalVariables.emailUtilizadorAutenticado
        val strFoto = GlobalVariables.fotoUtilizadorAutenticado

        val menu = navigationView.menu
        val administracaoItem = menu.findItem(R.id.nav_administracao)
        administracaoItem.isVisible = GlobalVariables.idCargoUtilizadorAutenticado == 0

        val horaAtual = LocalTime.now()
        val horaManha = LocalTime.of(6, 0)
        val horaTarde = LocalTime.of(12, 0)
        val horaNoite = LocalTime.of(20, 0)

        val saudacao = when {
            horaAtual.isAfterOrEqual(horaManha) && horaAtual.isBefore(horaTarde) ->
                "Bom dia"

            horaAtual.isAfterOrEqual(horaTarde) && horaAtual.isBefore(horaNoite) ->
                "Boa tarde"

            else ->
                "Boa noite"
        }

        txt_nome_utilizador.text = strNome
        txt_email_utilizador.text = strEmail

        if (strNome.isNotBlank()) {
            Toast.makeText(this, "$saudacao, $strNome!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "$saudacao!", Toast.LENGTH_LONG).show()
        }

        if (strFoto.isBlank() || strFoto == "null") {
            img_foto_perfil.setImageResource(R.drawable.user_profile_photo)
        } else {
            Glide.with(this).load(strFoto).into(img_foto_perfil)
        }

        if (globalVariables.checkForInternet(this)) {
            getReunioesUtilizador(this)
        } else {
            Toast.makeText(this, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerVagasDisponiveis()).commit()
        navigationView.setCheckedItem(R.id.nav_vagas)
        drawer.closeDrawer(GravityCompat.START)

        img_foto_perfil.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentDetalhesPerfil()).commit()
            navigationView.setCheckedItem(R.id.nav_configuracoes)
            drawer.closeDrawer(GravityCompat.START)
        }

        txt_nome_utilizador.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentDetalhesPerfil()).commit()
            navigationView.setCheckedItem(R.id.nav_configuracoes)
            drawer.closeDrawer(GravityCompat.START)
        }

        txt_email_utilizador.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentDetalhesPerfil()).commit()
            navigationView.setCheckedItem(R.id.nav_configuracoes)
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_vagas -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerVagasDisponiveis()).commit()
                navigationView.setCheckedItem(R.id.nav_vagas)
            }
            R.id.nav_ideias -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerIdeias()).commit()
                navigationView.setCheckedItem(R.id.nav_ideias)
            }
            R.id.nav_oportunidades -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerOportunidades()).commit()
                navigationView.setCheckedItem(R.id.nav_oportunidades)
            }
            R.id.nav_beneficios -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerBeneficios()).commit()
                navigationView.setCheckedItem(R.id.nav_beneficios)
            }
            R.id.nav_administracao -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentVerAdministracao()).commit()
                navigationView.setCheckedItem(R.id.nav_administracao)
            }
            R.id.nav_eventos -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentEventosCalendario()).commit()
                navigationView.setCheckedItem(R.id.nav_eventos)
            }
            R.id.nav_configuracoes -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentConfiguracoes()).commit()
                navigationView.setCheckedItem(R.id.nav_configuracoes)
            }
            R.id.nav_logout -> {
                val dia = AlertDialog.Builder(this)
                dia.setTitle("Terminar sessão")
                dia.setMessage("Tem a certeza que pretende terminar sessão da aplicação?")
                dia.setIcon(android.R.drawable.ic_dialog_alert)
                dia.setPositiveButton("Sim") { dialog, _ ->
                    val editorLogin = GlobalVariables.preferencesLogin!!.edit()
                    editorLogin.clear()
                    editorLogin.apply()
                    GlobalVariables.token = ""
                    dialog.dismiss()

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                dia.setNegativeButton("Não") { dialog, _ ->
                    dialog.dismiss()
                }
                dia.show()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun LocalTime.isAfterOrEqual(other: LocalTime): Boolean {
        return this.isAfter(other) || this == other
    }

    private fun JSONObject.getIntOrNull(key: String): Int? {
        return if (has(key) && !isNull(key)) {
            getInt(key)
        } else {
            null
        }
    }

    private fun getReunioesUtilizador(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarioreunioes?nusuario=" + GlobalVariables.idUtilizadorAutenticado)
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
                            val jsonArray = jsonObject.getJSONArray("message")
                            for (i in 0 until jsonArray.length()) {
                                val message = jsonArray.getJSONObject(i)
                                val NUsuario = message.getInt("NUsuario")
                                val NReunioes = message.getInt("NReunioes")

                                utilizadoresReunioesList.add(
                                    ItemsUtilizadoresReunioes(
                                        NUsuario,
                                        NReunioes
                                    )
                                )
                            }

                            if (globalVariables.checkForInternet(context)) {
                                getReunioes(context)
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

    private fun getReunioes(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/reunioes")
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
                            val jsonArray = jsonObject.getJSONArray("message")
                            for (i in 0 until jsonArray.length()) {
                                val message = jsonArray.getJSONObject(i)
                                val NReunioes = message.getInt("NReunioes")
                                val NUsuarioCriador = message.getInt("NUsuarioCriador")
                                val Tipo = message.getInt("Tipo")
                                val NOportunidade = message.getIntOrNull("NOportunidade")
                                val NEntrevista = message.getIntOrNull("NEntrevista")
                                var Titulo = message.getString("Titulo")
                                var Descricao = message.getString("Descricao")
                                var DataHoraInicio = message.getString("DataHoraInicio")
                                var DataHoraFim = message.getString("DataHoraFim")
                                var DataHoraNotificacao = message.getString("DataHoraNotificacao")
                                var NomeUsuarioCriador = message.getString("NomeUsuarioCriador")

                                if (Titulo.isNullOrBlank() || Titulo == "null") {
                                    Titulo = ""
                                }
                                if (Descricao.isNullOrBlank() || Descricao == "null") {
                                    Descricao = ""
                                }
                                if (DataHoraInicio.isNullOrBlank() || DataHoraInicio == "null") {
                                    DataHoraInicio = ""
                                }
                                if (DataHoraFim.isNullOrBlank() || DataHoraFim == "null") {
                                    DataHoraFim = ""
                                }
                                if (DataHoraNotificacao.isNullOrBlank() || DataHoraNotificacao == "null") {
                                    DataHoraNotificacao = ""
                                }
                                if (NomeUsuarioCriador.isNullOrBlank() || NomeUsuarioCriador == "null") {
                                    NomeUsuarioCriador = ""
                                }

                                if (utilizadoresReunioesList.any { it.NReunioes == NReunioes }) {
                                    reunioesList.add(
                                        ItemsReunioes(
                                            NReunioes,
                                            NUsuarioCriador,
                                            Titulo,
                                            Descricao,
                                            Tipo,
                                            DataHoraInicio,
                                            DataHoraFim,
                                            NOportunidade,
                                            NEntrevista,
                                            DataHoraNotificacao,
                                            NomeUsuarioCriador
                                        )
                                    )
                                }
                            }

                            if (meetingToday()) {
                                showNotification(context)
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

    fun meetingToday(): Boolean {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val horaAtual = now.toLocalTime()
        return reunioesList.any {
            val dataHoraInicio = LocalDateTime.parse(it.DataHoraInicio, DateTimeFormatter.ISO_DATE_TIME)
            val dataInicio = dataHoraInicio.toLocalDate()
            val horaInicio = dataHoraInicio.toLocalTime()
            dataInicio == today && horaInicio.isAfter(horaAtual)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel_id"
        val channelName = "Default Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, "default_channel_id")
            .setSmallIcon(R.drawable.ic_eventos2)
            .setContentTitle("Reuniões do dia")
            .setContentText("Confira a sua agenda para o dia de hoje.")
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}