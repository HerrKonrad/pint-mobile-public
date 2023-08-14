package com.example.mobilepint.configuracoes

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mobilepint.GlobalVariables
import com.example.mobilepint.ItemsUtilizadoresReunioes
import com.example.mobilepint.R
import com.example.mobilepint.administracao.ItemsReunioes
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class FragmentEventosCalendario : Fragment() {

    private lateinit var toast: Toast
    private lateinit var currentDate: Date
    private lateinit var formattedDate: String
    private lateinit var txt_data_selecionada: TextView
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var utilizadoresReunioesList: ArrayList<ItemsUtilizadoresReunioes>
    private lateinit var reunioesList: ArrayList<ItemsReunioes>
    private var lastBackPressTime: Long = 0
    private var globalVariables = GlobalVariables()
    private var eventDays = mutableSetOf<LocalDate>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_eventos_calendario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        txt_data_selecionada = view.findViewById(R.id.eventos_txt_data_selecionada)
        calendarView = view.findViewById(R.id.eventos_materialCalendarView)
        utilizadoresReunioesList = ArrayList()
        reunioesList = ArrayList()

        if (globalVariables.checkForInternet(requireContext())) {
            getReunioesUtilizador(requireContext())
        } else {
            Toast.makeText(requireContext(), "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
        }

        currentDate = Date()
        formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)
        txt_data_selecionada.text = formattedDate
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
        calendarView.addDecorator(TodayDecorator())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentTime = System.currentTimeMillis()
            if (lastBackPressTime < currentTime - 4000) {
                toast = Toast.makeText(requireContext(), "Pressione novamente para sair.", Toast.LENGTH_LONG)
                toast.show()
                lastBackPressTime = currentTime
            } else {
                toast.cancel()
                requireActivity().finish()
            }
        }
    }

    inner class DateSelectedListener : OnDateSelectedListener {
        override fun onDateSelected(
            widget: MaterialCalendarView,
            date: CalendarDay,
            selected: Boolean
        ) {
            val selectedDate = date.date.toLocalDate()
            formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.date)
            txt_data_selecionada.text = formattedDate

            val reunioesDoDia = reunioesList.filter {
                val dataHoraInicio = LocalDateTime.parse(it.DataHoraInicio, DateTimeFormatter.ISO_DATE_TIME)
                val dataInicio = dataHoraInicio.toLocalDate()
                dataInicio == selectedDate
            }

            val detalhes = if (reunioesDoDia.isNotEmpty()) {
                val timeZoneUTC = ZoneId.of("UTC")
                val timeZonePortugal = ZoneId.of("Europe/Lisbon")
                "<b>$selectedDate</b><br><br>" + reunioesDoDia.joinToString("<br><br>") {
                    val dataHoraInicioUTC = LocalDateTime.parse(it.DataHoraInicio, DateTimeFormatter.ISO_DATE_TIME)
                    val dataHoraInicioPortugal = ZonedDateTime.of(dataHoraInicioUTC, timeZoneUTC).withZoneSameInstant(timeZonePortugal)
                    val horaInicio = dataHoraInicioPortugal.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val dataHoraFimUTC = LocalDateTime.parse(it.DataHoraFim, DateTimeFormatter.ISO_DATE_TIME)
                    val dataHoraFimPortugal = ZonedDateTime.of(dataHoraFimUTC, timeZoneUTC).withZoneSameInstant(timeZonePortugal)
                    val horaFim = dataHoraFimPortugal.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    "- Título: ${it.Titulo}<br>- Início: $horaInicio<br>- Fim: $horaFim"
                }
            } else {
                "<b>$selectedDate</b><br><br>Não há eventos ou reuniões neste dia."
            }

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Reuniões do dia")
            builder.setMessage(Html.fromHtml(detalhes))
            builder.setIcon(R.drawable.ic_information)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val currentDate = LocalDate.now()
            if (selectedDate >= currentDate) {
                builder.setNegativeButton("Criar evento") { dialog, _ ->
                    GlobalVariables.dataFormatadaCalendario = formattedDate
                    val fragment = parentFragmentManager.beginTransaction()
                    fragment.replace(R.id.fragment_container, FragmentCriarReuniaoOutros())
                    fragment.commit()
                    dialog.dismiss()
                }
            }
            builder.show()
        }
    }

    inner class EventDecorator(private val day: CalendarDay) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == this.day
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(10f, resources.getColor(R.color.event_color)))
        }
    }

    inner class TodayDecorator : DayViewDecorator {
        private val today = CalendarDay.today()

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == today
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(StyleSpan(Typeface.BOLD))
            view.addSpan(ForegroundColorSpan(resources.getColor(R.color.today_color)))
        }
    }

    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun JSONObject.getIntOrNull(key: String): Int? {
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
                                val NReunioes = message.getInt("NReunioes")

                                utilizadoresReunioesList.add(
                                    ItemsUtilizadoresReunioes(
                                        NUsuario,
                                        NReunioes
                                    )
                                )
                            }

                            if (globalVariables.checkForInternet(requireContext())) {
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

                            for (reuniao in reunioesList) {
                                val dataHoraInicio = LocalDateTime.parse(reuniao.DataHoraInicio, DateTimeFormatter.ISO_DATE_TIME)
                                val dataReuniao = dataHoraInicio.toLocalDate()
                                eventDays.add(dataReuniao)
                            }

                            for (eventDay in eventDays) {
                                val calendarDay = CalendarDay.from(eventDay.year, eventDay.monthValue - 1, eventDay.dayOfMonth)
                                calendarView.addDecorator(EventDecorator(calendarDay))
                            }

                            calendarView.setOnDateChangedListener(DateSelectedListener())
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