package com.example.mobilepint.ideias

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.R
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

class FragmentEnviarRelatorioIdeia: Fragment() {

    private var globalVariables = GlobalVariables()
    private var bundleNumIdeia: Int = -1
    private var bundleTituloIdeia: String = ""
    private var bundleEstadoIdeia: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_enviar_relatorio_ideia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var naoNotificarAdmin: Int
        var strRelatorioAutor = ""
        var strRelatorioAdministrador = ""
        var msgErrors = ""

        val bundle = arguments
        if (bundle != null) {
            bundleNumIdeia = bundle.getInt("NumIdeia")
            bundleTituloIdeia = bundle.getString("TituloIdeia").toString()
            bundleEstadoIdeia = bundle.getString("EstadoIdeia").toString()
        }

        val txt_titulo_ideia = view.findViewById(R.id.relatorio_ideia_txt_titulo_ideia) as TextView
        val txt_observacao_relatorio = view.findViewById(R.id.relatorio_ideia_txt_observacao_relatorio) as TextView
        val chbx_informar_autor = view.findViewById(R.id.relatorio_ideia_chbx_informar_autor) as CheckBox
        val txt_relatorio_autor = view.findViewById(R.id.relatorio_ideia_txt_relatorio_autor) as TextView
        val edtxt_relatorio_autor = view.findViewById(R.id.relatorio_ideia_edtxt_relatorio_autor) as EditText
        val chbx_informar_administrador = view.findViewById(R.id.relatorio_ideia_chbx_informar_administrador) as CheckBox
        val txt_relatorio_administrador = view.findViewById(R.id.relatorio_ideia_txt_relatorio_administrador) as TextView
        val edtxt_relatorio_administrador = view.findViewById(R.id.relatorio_ideia_edtxt_relatorio_administrador) as EditText
        val btn_enviar_relatorio = view.findViewById(R.id.relatorio_ideia_btn_enviar_relatorio) as Button
        val tipoEstadoIdeia = if (bundleEstadoIdeia == "Aceite") 1 else 0

        txt_titulo_ideia.text = bundleTituloIdeia
        if (chbx_informar_autor.isChecked) {
            txt_relatorio_autor.visibility = View.VISIBLE
            edtxt_relatorio_autor.visibility = View.VISIBLE
        } else {
            txt_relatorio_autor.visibility = View.GONE
            edtxt_relatorio_autor.visibility = View.GONE
        }
        if (chbx_informar_administrador.isChecked) {
            txt_relatorio_administrador.visibility = View.VISIBLE
            edtxt_relatorio_administrador.visibility = View.VISIBLE
        } else {
            txt_relatorio_administrador.visibility = View.GONE
            edtxt_relatorio_administrador.visibility = View.GONE
        }

        if (tipoEstadoIdeia == 1) {
            txt_observacao_relatorio.text = Html.fromHtml("<b><u>Observação:</u></b> " + getString(R.string.str_observacao_ideia_aceite))
        } else {
            txt_observacao_relatorio.text = Html.fromHtml("<b><u>Observação:</u></b> " + getString(R.string.str_observacao_ideia_rejeitada))
            chbx_informar_administrador.isEnabled = false
            txt_relatorio_administrador.visibility = View.GONE
            edtxt_relatorio_administrador.visibility = View.GONE
        }

        chbx_informar_autor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                txt_relatorio_autor.visibility = View.VISIBLE
                edtxt_relatorio_autor.visibility = View.VISIBLE
            } else {
                txt_relatorio_autor.visibility = View.GONE
                edtxt_relatorio_autor.visibility = View.GONE
            }
        }

        chbx_informar_administrador.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                txt_relatorio_administrador.visibility = View.VISIBLE
                edtxt_relatorio_administrador.visibility = View.VISIBLE
            } else {
                txt_relatorio_administrador.visibility = View.GONE
                edtxt_relatorio_administrador.visibility = View.GONE
            }
        }

        btn_enviar_relatorio.setOnClickListener {
            btn_enviar_relatorio.isEnabled = false
            Handler().postDelayed({
                btn_enviar_relatorio.isEnabled = true
            }, 10000)

            if (chbx_informar_autor.isChecked) {
                if (edtxt_relatorio_autor.text.toString().isNotBlank()) {
                    strRelatorioAutor = edtxt_relatorio_autor.text.toString()
                } else {
                    msgErrors += "O campo \'Relatório para o autor\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
                }
            } else {
                strRelatorioAutor = ""
            }

            if (chbx_informar_administrador.isChecked) {
                naoNotificarAdmin = 0
                if (edtxt_relatorio_administrador.text.toString().isNotBlank()) {
                    strRelatorioAdministrador = edtxt_relatorio_administrador.text.toString()
                } else {
                    msgErrors += "O campo \'Relatório para o administrador\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
                }
            } else {
                naoNotificarAdmin = 1
                strRelatorioAdministrador = ""
            }

            if (msgErrors.isBlank()) {
                if (globalVariables.checkForInternet(requireContext())) {
                    postRelatorioIdeia(
                        requireContext(),
                        naoNotificarAdmin,
                        bundleNumIdeia,
                        strRelatorioAutor,
                        strRelatorioAdministrador,
                        tipoEstadoIdeia
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
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Aviso")
            builder.setMessage("Tem a certeza que pretende sair sem enviar o relatório da ideia \'$bundleTituloIdeia\'?")
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerIdeias())
                fragment.commit()
            }
            builder.setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun postRelatorioIdeia(
        context: Context,
        naoNotificarAdmin: Int,
        numIdeia: Int,
        relatorioAutor: String,
        relatorioAdmin: String,
        tipo: Int
    ) {
        data class Post(
            val NIdeia: Int,
            val ApontamentosAutor: String,
            val ApontamentosAdm: String,
            val Tipo: Int
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numIdeia, relatorioAutor, relatorioAdmin, tipo)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/relatorioideia?nao_notificar_adm=" + naoNotificarAdmin)
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
                            val NRelatorioIdeia = message.getInt("NRelatorioIdeia")
                            val ApontamentosAdm = message.getString("ApontamentosAdm")
                            val ApontamentosAutor = message.getString("ApontamentosAutor")
                            val Tipo = message.getInt("Tipo")
                            val DataHora = message.getString("DataHora")
                            val NIdeia = message.getInt("NIdeia")

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage("O relatório da ideia \'$bundleTituloIdeia\' foi enviado com sucesso.")
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerIdeias())
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