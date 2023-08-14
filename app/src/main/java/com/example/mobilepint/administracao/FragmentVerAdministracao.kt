package com.example.mobilepint.administracao

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.EnumCargos
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.example.mobilepint.utilizadores.AdapterUtilizadores
import com.example.mobilepint.utilizadores.FragmentCriarUtilizador
import com.example.mobilepint.utilizadores.ItemsUtilizadores
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerAdministracao : Fragment() {
    data class ItemsAdministration(val NumOption: Int, val NameOption: String)

    private lateinit var toast: Toast
    private lateinit var txt_subtitulo: TextView
    private lateinit var imgbtn_view_more: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var txt_permissions: TextView
    private lateinit var cardView: CardView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var administrationList: ArrayList<ItemsAdministration>
    private lateinit var linearLayout_utilizadores: LinearLayout
    private lateinit var utilizadoresAdapter: AdapterUtilizadores
    private lateinit var utilizadoresList: ArrayList<ItemsUtilizadores>
    private lateinit var linearLayout_reunioes: LinearLayout
    private lateinit var reunioesAdapter: AdapterReunioes
    private lateinit var reunioesList: ArrayList<ItemsReunioes>
    private lateinit var linearLayout_localidades: LinearLayout
    private lateinit var localidadesAdapter: AdapterLocalidades
    private lateinit var localidadesList: ArrayList<ItemsLocalidades>
    private lateinit var linearLayout_topicos_ideias: LinearLayout
    private lateinit var topicosIdeiasAdminAdapter: AdapterTopicosIdeiasAdmin
    private lateinit var topicosIdeiasAdminList: ArrayList<ItemsTopicosIdeiasAdmin>
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabCreateItem: FloatingActionButton
    private var lastBackPressTime: Long = 0
    private var globalVariables = GlobalVariables()
    private var numCargo = GlobalVariables.idCargoUtilizadorAutenticado

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_administracao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        administrationList = arrayListOf(
            ItemsAdministration(0, "Utilizadores"),
            ItemsAdministration(1, "Reuniões"),
            ItemsAdministration(2, "Localidades"),
            ItemsAdministration(3, "Tópicos das ideias")
        )

        txt_subtitulo = view.findViewById(R.id.administracao_txt_subtitulo)
        imgbtn_view_more = view.findViewById(R.id.administracao_imgbtn_view_more)
        searchView = view.findViewById(R.id.administracao_searchView)
        txt_permissions = view.findViewById(R.id.administracao_txt_permissions)
        cardView = view.findViewById(R.id.administracao_cardView)
        recyclerView = view.findViewById(R.id.administracao_recyclerview)
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        linearLayout_utilizadores = view.findViewById(R.id.administracao_linearLayout_utilizadores)
        linearLayout_reunioes = view.findViewById(R.id.administracao_linearLayout_reunioes)
        linearLayout_localidades = view.findViewById(R.id.administracao_linearLayout_localidades)
        linearLayout_topicos_ideias = view.findViewById(R.id.administracao_linearLayout_topicos_ideias)
        fabCreateItem = view.findViewById(R.id.administracao_fabCreate)

        utilizadoresList = ArrayList()
        utilizadoresAdapter = AdapterUtilizadores(requireContext(), utilizadoresList)
        reunioesList = ArrayList()
        reunioesAdapter = AdapterReunioes(requireContext(), reunioesList)
        localidadesList = ArrayList()
        localidadesAdapter = AdapterLocalidades(requireContext(), localidadesList)
        topicosIdeiasAdminList = ArrayList()
        topicosIdeiasAdminAdapter = AdapterTopicosIdeiasAdmin(requireContext(), topicosIdeiasAdminList)

        if (numCargo == EnumCargos.ADMINISTRADOR.numCargo) {
            txt_subtitulo.visibility = View.VISIBLE
            imgbtn_view_more.visibility = View.VISIBLE
            searchView.visibility = View.VISIBLE
            txt_permissions.visibility = View.GONE
            cardView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            fabCreateItem.visibility = View.VISIBLE
            selectOption(GlobalVariables.administrationOption)
        } else {
            txt_subtitulo.visibility = View.GONE
            imgbtn_view_more.visibility = View.GONE
            searchView.visibility = View.GONE
            txt_permissions.visibility = View.VISIBLE
            cardView.visibility = View.GONE
            recyclerView.visibility = View.GONE
            linearLayout_utilizadores.visibility = View.GONE
            linearLayout_reunioes.visibility = View.GONE
            linearLayout_localidades.visibility = View.GONE
            linearLayout_topicos_ideias.visibility = View.GONE
            fabCreateItem.visibility = View.GONE
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    when (GlobalVariables.administrationOption) {
                        0 -> { // Utilizadores
                            utilizadoresAdapter.filter.filter(newText)
                        }

                        1 -> { //Reuniões
                            reunioesAdapter.filter.filter(newText)
                        }

                        2 -> { //Localidades
                            localidadesAdapter.filter.filter(newText)
                        }

                        3 -> { // Tópicos das ideias
                            topicosIdeiasAdminAdapter.filter.filter(newText)
                        }
                    }
                } catch (nullPointerException: NullPointerException) {
                    nullPointerException.printStackTrace()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return true
            }
        })

        imgbtn_view_more.setOnClickListener {
            val options = arrayOf(
                "Visualizar utilizadores",
                "Visualizar reuniões",
                "Visualizar localidades",
                "Visualizar tópicos das ideias"
            )
            val builderOptions = android.app.AlertDialog.Builder(requireContext())
            builderOptions.setTitle("Escolha uma opção:")
            builderOptions.setItems(options) { dialogOptions, which ->
                dialogOptions.dismiss()
                GlobalVariables.administrationOption = which
                selectOption(which)
            }
            builderOptions.show()
        }

        fabCreateItem.setOnClickListener {
            when (GlobalVariables.administrationOption) {
                0 -> { // Utilizadores
                    GlobalVariables.criarUtilizador = true
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarUtilizador())
                    fragment.commit()
                }

                1 -> { //Reuniões
                    GlobalVariables.criarReuniaoOutros = true
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarReuniaoOutrosAdmin())
                    fragment.commit()
                }

                2 -> { //Localidades
                    GlobalVariables.criarLocalidade = true
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarLocalidadeAdmin())
                    fragment.commit()
                }

                3 -> { // Tópicos das ideias
                    GlobalVariables.criarTopicoIdeias = true
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarTopicoIdeiasAdmin())
                    fragment.commit()
                }

                else -> {
                    Toast.makeText(context, "Opção inválida", Toast.LENGTH_LONG).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (GlobalVariables.administrationOption == 0) { // Utilizadores
                val currentTime = System.currentTimeMillis()
                if (lastBackPressTime < currentTime - 4000) {
                    toast = Toast.makeText(requireContext(), "Pressione novamente para sair.", Toast.LENGTH_LONG)
                    toast.show()
                    lastBackPressTime = currentTime
                } else {
                    toast.cancel()
                    requireActivity().finish()
                }
            } else {
                GlobalVariables.administrationOption = 0 // Utilizadores
                txt_subtitulo.text = administrationList.find { it.NumOption == 0 }?.NameOption
                searchView.queryHint = "Nome"
                searchView.setQuery("", false)
                linearLayout_utilizadores.visibility = View.VISIBLE
                linearLayout_reunioes.visibility = View.GONE
                linearLayout_localidades.visibility = View.GONE
                linearLayout_topicos_ideias.visibility = View.GONE

                if (utilizadoresList.isEmpty()) {
                    if (globalVariables.checkForInternet(requireContext())) {
                        getUtilizadores(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    utilizadoresAdapter = AdapterUtilizadores(requireContext(), utilizadoresList)
                    recyclerView.adapter = utilizadoresAdapter
                }
            }
        }
    }

    private fun selectOption(option: Int) {
        when (option) {
            0 -> { // Utilizadores
                txt_subtitulo.text = administrationList.find { it.NumOption == 0 }?.NameOption
                searchView.queryHint = "Nome"
                searchView.setQuery("", false)
                linearLayout_utilizadores.visibility = View.VISIBLE
                linearLayout_reunioes.visibility = View.GONE
                linearLayout_localidades.visibility = View.GONE
                linearLayout_topicos_ideias.visibility = View.GONE

                if (utilizadoresList.isEmpty()) {
                    if (globalVariables.checkForInternet(requireContext())) {
                        getUtilizadores(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    utilizadoresAdapter = AdapterUtilizadores(requireContext(), utilizadoresList)
                    recyclerView.adapter = utilizadoresAdapter
                }
            }

            1 -> { //Reuniões
                txt_subtitulo.text = administrationList.find { it.NumOption == 1 }?.NameOption
                searchView.queryHint = "Título"
                searchView.setQuery("", false)
                linearLayout_utilizadores.visibility = View.GONE
                linearLayout_reunioes.visibility = View.VISIBLE
                linearLayout_localidades.visibility = View.GONE
                linearLayout_topicos_ideias.visibility = View.GONE

                if (reunioesList.isEmpty()) {
                    if (globalVariables.checkForInternet(requireContext())) {
                        getReunioes(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    reunioesAdapter = AdapterReunioes(requireContext(), reunioesList)
                    recyclerView.adapter = reunioesAdapter
                }
            }

            2 -> { //Localidades
                txt_subtitulo.text = administrationList.find { it.NumOption == 2 }?.NameOption
                searchView.queryHint = "Localidade"
                searchView.setQuery("", false)
                linearLayout_utilizadores.visibility = View.GONE
                linearLayout_reunioes.visibility = View.GONE
                linearLayout_localidades.visibility = View.VISIBLE
                linearLayout_topicos_ideias.visibility = View.GONE

                if (localidadesList.isEmpty()) {
                    if (globalVariables.checkForInternet(requireContext())) {
                        getLocalidades(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    localidadesAdapter = AdapterLocalidades(requireContext(), localidadesList)
                    recyclerView.adapter = localidadesAdapter
                }
            }

            3 -> { // Tópicos das ideias
                txt_subtitulo.text = administrationList.find { it.NumOption == 3 }?.NameOption
                searchView.queryHint = "Nome"
                searchView.setQuery("", false)
                linearLayout_utilizadores.visibility = View.GONE
                linearLayout_reunioes.visibility = View.GONE
                linearLayout_localidades.visibility = View.GONE
                linearLayout_topicos_ideias.visibility = View.VISIBLE

                if (topicosIdeiasAdminList.isEmpty()) {
                    if (globalVariables.checkForInternet(requireContext())) {
                        getTopicosIdeiasAdmin(requireContext())
                    } else {
                        Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    topicosIdeiasAdminAdapter = AdapterTopicosIdeiasAdmin(requireContext(), topicosIdeiasAdminList)
                    recyclerView.adapter = topicosIdeiasAdminAdapter
                }
            }
        }
    }

    private fun JSONObject.getIntOrNull(key: String): Int? {
        return if (has(key) && !isNull(key)) {
            getInt(key)
        } else {
            null
        }
    }

    private fun getUtilizadores(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarios")
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
                                val message = jsonArray.getJSONObject(i)
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
                                if (DataNascimento.isNullOrBlank() || DataNascimento == "null") {
                                    DataNascimento = ""
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

                                utilizadoresList.add(
                                    ItemsUtilizadores(
                                        NUsuario,
                                        Nome,
                                        Email,
                                        NCargo,
                                        Telefone,
                                        Linkedin,
                                        CV,
                                        Foto,
                                        DataNascimento,
                                        Genero,
                                        Estado,
                                        Localidade,
                                        DataHoraRegisto
                                    )
                                )
                            }

                            utilizadoresAdapter = AdapterUtilizadores(context, utilizadoresList)
                            recyclerView.adapter = utilizadoresAdapter

                            if (utilizadoresList.isEmpty()) {
                                Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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

                            reunioesAdapter = AdapterReunioes(context, reunioesList)
                            recyclerView.adapter = reunioesAdapter

                            if (reunioesList.isEmpty()) {
                                Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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

    private fun getLocalidades(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/localidades")
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
                                val NLocalidade = get.getInt("NLocalidade")
                                var Localidade = get.getString("Localidade")

                                if (Localidade.isNullOrBlank() || Localidade == "null") {
                                    Localidade = ""
                                }

                                localidadesList.add(
                                    ItemsLocalidades(
                                        NLocalidade,
                                        Localidade
                                    )
                                )
                            }

                            localidadesAdapter = AdapterLocalidades(context, localidadesList)
                            recyclerView.adapter = localidadesAdapter

                            if (localidadesList.isEmpty()) {
                                Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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

    private fun getTopicosIdeiasAdmin(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/topicoideias")
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
                                val NTopicoIdeia = get.getInt("NTopicoIdeia")
                                val NomeTopico = get.getString("NomeTopico")

                                topicosIdeiasAdminList.add(
                                    ItemsTopicosIdeiasAdmin(
                                        NTopicoIdeia,
                                        NomeTopico
                                    )
                                )
                            }

                            topicosIdeiasAdminAdapter = AdapterTopicosIdeiasAdmin(requireContext(), topicosIdeiasAdminList)
                            recyclerView.adapter = topicosIdeiasAdminAdapter

                            if (topicosIdeiasAdminList.isEmpty()) {
                                Toast.makeText(context, "Sem dados para exibir.", Toast.LENGTH_LONG).show()
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
}