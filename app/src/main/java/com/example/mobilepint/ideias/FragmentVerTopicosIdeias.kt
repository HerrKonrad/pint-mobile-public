package com.example.mobilepint.ideias

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class FragmentVerTopicosIdeias : Fragment() {

    private lateinit var txt_titulo_da_ideia_label: TextView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var btn_guardar: Button
    private lateinit var topicosIdeiasAdapter: AdapterTopicosIdeias
    private lateinit var topicosIdeiasFullList: ArrayList<ItemsTopicosIdeiasFull>
    private lateinit var topicosIdeiaList: ArrayList<ItemsTopicosIdeia>
    private var globalVariables = GlobalVariables()
    private var loopCounter: Int = 0
    private var postCounter: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ver_topicos_ideias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_titulo_da_ideia_label = view.findViewById(R.id.topicos_ideias_txt_titulo_da_ideia_label)
        recyclerView = view.findViewById(R.id.topicos_ideias_recyclerview) as RecyclerView
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        btn_guardar = view.findViewById(R.id.topicos_ideias_btn_guardar)

        topicosIdeiasFullList = ArrayList()
        topicosIdeiaList = ArrayList()
        topicosIdeiasAdapter = AdapterTopicosIdeias(requireContext(), topicosIdeiasFullList)
        txt_titulo_da_ideia_label.text = GlobalVariables.detalhesTituloIdeia

        if (globalVariables.checkForInternet(requireContext())) {
            Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show()
            getTopicosIdeiasFull(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        btn_guardar.setOnClickListener {
            btn_guardar.isEnabled = false
            Handler().postDelayed({
                btn_guardar.isEnabled = true
            }, 10000)

            loopCounter = 0
            postCounter = 0

            for (item in topicosIdeiasFullList) {
                if (item.IsChecked) {
                    loopCounter++
                }
            }

            if (globalVariables.checkForInternet(requireContext())) {
                deleteTopicosIdeiasFull(requireContext(), GlobalVariables.detalhesNumIdeia)
            } else {
                Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (GlobalVariables.criarIdeia) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentCriarIdeia())
                fragment.commit()
            }
        }
    }

    private fun getTopicosIdeiasFull(context: Context) {
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
                                topicosIdeiasFullList.add(
                                    ItemsTopicosIdeiasFull(
                                        NTopicoIdeia,
                                        NomeTopico,
                                        false
                                    )
                                )
                            }

                            if (globalVariables.checkForInternet(context)) {
                                getTopicosIdeia(context, GlobalVariables.detalhesNumIdeia)
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

    private fun getTopicosIdeia(context: Context, numIdeia: Int) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/topicosdasideias?nideia=" + numIdeia)
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
                                val NIdeia = get.getInt("NIdeia")
                                topicosIdeiaList.add(
                                    ItemsTopicosIdeia(
                                        NTopicoIdeia,
                                        NIdeia
                                    )
                                )
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

                        for (item in topicosIdeiaList) {
                            val matchingItem = topicosIdeiasFullList.find { it.NTopicoIdeia == item.NTopicoIdeia }
                            if (matchingItem != null) {
                                matchingItem.IsChecked = true
                            }
                        }

                        topicosIdeiasAdapter = AdapterTopicosIdeias(requireContext(), topicosIdeiasFullList)
                        recyclerView.adapter = topicosIdeiasAdapter
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

    private fun deleteTopicosIdeiasFull(context: Context, numIdeia: Int) {
        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/topicosdasideias?nideia=" + numIdeia)
            .delete()
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

                        val msgBuilder = if (success) {
                            val message = jsonObject.getString("message")
                            println("Response body message: $message")
                            "Os tópicos da ideia \'${GlobalVariables.detalhesTituloIdeia}\' foram excluídos com sucesso."
                        } else {
                            val message = jsonObject.getString("message")
                            println("Response body message: $message")
                            "A ideia \'${GlobalVariables.detalhesTituloIdeia}\' não possui nenhum tópico para exclusão."
                        }

                        if (loopCounter == 0) {
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(msgBuilder)
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                if (GlobalVariables.criarIdeia) {
                                    val fragment = parentFragmentManager.beginTransaction()
                                    fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                                    fragment.commit()
                                } else {
                                    val fragment = parentFragmentManager.beginTransaction()
                                    fragment.replace(R.id.fragment_container, FragmentCriarIdeia())
                                    fragment.commit()
                                }
                            }
                            builder.show()
                        } else {
                            if (globalVariables.checkForInternet(context)) {
                                for (item in topicosIdeiasFullList) {
                                    if (item.IsChecked) {
                                        postTopicoIdeia(context, item.NTopicoIdeia, GlobalVariables.detalhesNumIdeia)
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                            }
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

    private fun postTopicoIdeia(context: Context, numTopicoIdeia: Int, numIdeia: Int) {
        data class Post(val NTopicoIdeia: Int, val NIdeia: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numTopicoIdeia, numIdeia)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), Gson().toJson(post))

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/topicosdasideias?ntopicoideia=" + numTopicoIdeia + "&nideia=" + numIdeia)
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
                            val NTopicoIdeia = message.getInt("NTopicoIdeia")
                            val NIdeia = message.getInt("NIdeia")

                            postCounter++
                            var nomeTopico = ""

                            for (item in topicosIdeiasFullList) {
                                if (item.NTopicoIdeia == numTopicoIdeia) {
                                    nomeTopico = item.NomeTopico
                                    break
                                }
                            }

                            if (nomeTopico.isNotBlank()) {
                                println("O tópico \'$nomeTopico\' da ideia \'${GlobalVariables.detalhesTituloIdeia}\' foi adicionado com sucesso.")
                            } else {
                                println("O tópico da ideia \'${GlobalVariables.detalhesTituloIdeia}\' foi adicionado com sucesso.")
                            }

                            if (postCounter == loopCounter) {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Aviso")
                                builder.setIcon(R.drawable.ic_information)
                                builder.setMessage("Os tópicos da ideia \'${GlobalVariables.detalhesTituloIdeia}\' foram guardados com sucesso.")
                                builder.setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                    if (GlobalVariables.criarIdeia) {
                                        val fragment = parentFragmentManager.beginTransaction()
                                        fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                                        fragment.commit()
                                    } else {
                                        val fragment = parentFragmentManager.beginTransaction()
                                        fragment.replace(R.id.fragment_container, FragmentCriarIdeia())
                                        fragment.commit()
                                    }
                                }
                                builder.show()
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