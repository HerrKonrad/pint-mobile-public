package com.example.mobilepint.administracao

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.mobilepint.utilizadores.FragmentVerUtilizadoresReuniao
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date

class FragmentCriarReuniaoOutrosAdmin : Fragment() {
    data class ItemsTempoNotificacao(val NumMinutos: Int, val TempoNotificacao: String)

    private lateinit var txt_titulo_janela: TextView
    private lateinit var edtxt_titulo_reuniao: EditText
    private lateinit var edtxt_tipo_reuniao: EditText
    private lateinit var edtxt_descricao_reuniao: EditText
    private lateinit var edtxt_data_inicio_reuniao: EditText
    private lateinit var edtxt_hora_inicio_reuniao: EditText
    private lateinit var edtxt_data_fim_reuniao: EditText
    private lateinit var edtxt_hora_fim_reuniao: EditText
    private lateinit var tempoNotificacaoList: ArrayList<ItemsTempoNotificacao>
    private lateinit var chbx_notificar_participantes: CheckBox
    private lateinit var layout_notificar_participantes: LinearLayout
    private lateinit var spinner_aviso_previo_reuniao: Spinner
    private lateinit var btn_criar_reuniao_entrevista: Button
    private var globalVariables = GlobalVariables()
    private var strTituloReuniao: String = ""
    private var strDescricaoReuniao: String = ""
    private var strDataHoraInicioReuniao: String = ""
    private var strDataHoraFimReuniao: String = ""
    private var strDataHoraNotificacao: String? = null
    private var positionAvisoPrevioReuniao: Int = 0
    private var msgPosts: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_reuniao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        var strDataInicioReuniao = ""
        var strHoraInicioReuniao = ""
        var strDataFimReuniao = ""
        var strHoraFimReuniao = ""
        var msgErrors = ""

        txt_titulo_janela = view.findViewById(R.id.criar_reuniao_txt_titulo_janela)
        edtxt_titulo_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_titulo_reuniao)
        edtxt_tipo_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_tipo_reuniao)
        edtxt_descricao_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_descricao_reuniao)
        edtxt_data_inicio_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_data_inicio_reuniao)
        edtxt_hora_inicio_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_hora_inicio_reuniao)
        edtxt_data_fim_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_data_fim_reuniao)
        edtxt_hora_fim_reuniao = view.findViewById(R.id.criar_reuniao_edtxt_hora_fim_reuniao)
        chbx_notificar_participantes = view.findViewById(R.id.criar_reuniao_chbx_notificar_participantes)
        layout_notificar_participantes = view.findViewById(R.id.criar_reuniao_linearLayout_notificar_participantes)
        spinner_aviso_previo_reuniao = view.findViewById(R.id.criar_reuniao_spinner_aviso_previo_reuniao)
        btn_criar_reuniao_entrevista = view.findViewById(R.id.criar_reuniao_btn_criar_reuniao)

        tempoNotificacaoList = arrayListOf(
            ItemsTempoNotificacao(15, "15 minutos"),
            ItemsTempoNotificacao(30, "30 minutos"),
            ItemsTempoNotificacao(45, "45 minutos"),
            ItemsTempoNotificacao(60, "1 hora"),
            ItemsTempoNotificacao(1440, "1 dia")
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tempoNotificacaoList.map { it.TempoNotificacao }.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_aviso_previo_reuniao.adapter = adapter
        spinner_aviso_previo_reuniao.isEnabled = tempoNotificacaoList.isNotEmpty()

        var formattedStartDate = ""
        var formattedEndDate = ""
        var formattedStartTime = ""
        var formattedEndTime = ""

        if (GlobalVariables.criarReuniaoOutros) {
            txt_titulo_janela.text = getString(R.string.str_criar_reuniao)
            btn_criar_reuniao_entrevista.text = getString(R.string.str_criar_reuniao)
            chbx_notificar_participantes.visibility = View.VISIBLE
            edtxt_tipo_reuniao.text = Editable.Factory.getInstance().newEditable(getString(R.string.str_outros))
        } else {
            txt_titulo_janela.text = getString(R.string.str_editar_reuniao)
            btn_criar_reuniao_entrevista.text = getString(R.string.str_editar_reuniao)
            chbx_notificar_participantes.visibility = View.GONE

            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            val strHorarioInicio = GlobalVariables.detalhesDataHoraInicioReuniao
            val strHorarioFim = GlobalVariables.detalhesDataHoraFimReuniao

            var startDate = ""
            var startTime = ""
            var endDate = ""
            var endTime = ""

            if (strHorarioInicio.isNotBlank() && strHorarioFim.isNotBlank()) {
                val startDateTime = LocalDateTime.parse(strHorarioInicio, inputFormatter)
                val endDateTime = LocalDateTime.parse(strHorarioFim, inputFormatter)
                startDate = startDateTime.format(outputDateFormatter)
                startTime = startDateTime.format(outputTimeFormatter)
                endDate = endDateTime.format(outputDateFormatter)
                endTime = endDateTime.format(outputTimeFormatter)
            }

            val strTipoReuniao = when (GlobalVariables.detalhesTipoReuniao) {
                0 -> {
                    "Reunião sobre entrevista"
                }

                1 -> {
                    "Reunião sobre oportunidade"
                }

                2 -> {
                    "Outros"
                }

                else -> {
                    "Outros"
                }
            }

            edtxt_titulo_reuniao.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesTituloReuniao)
            edtxt_tipo_reuniao.text = Editable.Factory.getInstance().newEditable(strTipoReuniao)
            edtxt_data_inicio_reuniao.text = Editable.Factory.getInstance().newEditable(startDate)
            edtxt_hora_inicio_reuniao.text = Editable.Factory.getInstance().newEditable(startTime)
            edtxt_data_fim_reuniao.text = Editable.Factory.getInstance().newEditable(endDate)
            edtxt_hora_fim_reuniao.text = Editable.Factory.getInstance().newEditable(endTime)
            edtxt_descricao_reuniao.text = Editable.Factory.getInstance().newEditable(GlobalVariables.detalhesDescricaoReuniao)

            formattedStartDate = startDate
            formattedStartTime = startTime
            formattedEndDate = endDate
            formattedEndTime = endTime
        }

        edtxt_data_inicio_reuniao.setOnClickListener {
            val calendar = Calendar.getInstance()
            val startDatePicker = DatePickerDialog(requireContext())
            startDatePicker.datePicker.minDate = calendar.timeInMillis
            startDatePicker.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = selectedCalendar.time
                formattedStartDate = globalVariables.dateFormat.format(selectedDate)
                edtxt_data_inicio_reuniao.setText(formattedStartDate)
                formattedEndDate = globalVariables.dateFormat.format(selectedDate)
                edtxt_data_fim_reuniao.setText(formattedEndDate)
            }
            startDatePicker.show()
        }

        edtxt_data_fim_reuniao.setOnClickListener {
            val calendar = Calendar.getInstance()
            val endDatePicker = DatePickerDialog(requireContext())
            endDatePicker.datePicker.minDate = calendar.timeInMillis
            endDatePicker.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = selectedCalendar.time
                formattedEndDate = globalVariables.dateFormat.format(selectedDate)
                edtxt_data_fim_reuniao.setText(formattedEndDate)
            }
            endDatePicker.show()
        }

        edtxt_hora_inicio_reuniao.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val time = calendar.time
                formattedStartTime = globalVariables.timeFormat.format(time)
                edtxt_hora_inicio_reuniao.setText(formattedStartTime)

                val startTime = LocalTime.parse(formattedStartTime)
                val endTime = startTime.plusMinutes(30)
                formattedEndTime = endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                edtxt_hora_fim_reuniao.setText(formattedEndTime)
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val startTimePicker = TimePickerDialog(requireContext(), timeSetListener, hour, minute, true)
            startTimePicker.show()
        }

        edtxt_hora_fim_reuniao.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (formattedEndTime.isNotEmpty()) {
                val time = globalVariables.timeFormat.parse(formattedEndTime) as Date
                calendar.time = time
            }
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val selectedTime = calendar.time
                formattedEndTime = globalVariables.timeFormat.format(selectedTime)
                edtxt_hora_fim_reuniao.setText(formattedEndTime)
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val endTimePicker = TimePickerDialog(requireContext(), timeSetListener, hour, minute, true)
            endTimePicker.show()
        }

        if (chbx_notificar_participantes.isChecked) {
            layout_notificar_participantes.visibility = View.VISIBLE
        } else {
            layout_notificar_participantes.visibility = View.GONE
        }

        chbx_notificar_participantes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layout_notificar_participantes.visibility = View.VISIBLE
            } else {
                layout_notificar_participantes.visibility = View.GONE
            }
        }

        spinner_aviso_previo_reuniao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                positionAvisoPrevioReuniao = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btn_criar_reuniao_entrevista.setOnClickListener {
            btn_criar_reuniao_entrevista.isEnabled = false
            Handler().postDelayed({
                btn_criar_reuniao_entrevista.isEnabled = true
            }, 10000)

            var horarioInicio: Date? = null
            var horarioFim: Date? = null
            var numMinutos: Long = 0

            if (edtxt_titulo_reuniao.text.toString().isNotBlank()) {
                strTituloReuniao = edtxt_titulo_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Título da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_data_inicio_reuniao.text.toString().isNotBlank()) {
                strDataInicioReuniao = edtxt_data_inicio_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Data de início da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_hora_inicio_reuniao.text.toString().isNotBlank()) {
                strHoraInicioReuniao = edtxt_hora_inicio_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Hora de início da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_data_fim_reuniao.text.toString().isNotBlank()) {
                strDataFimReuniao = edtxt_data_fim_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Data de fim da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_hora_fim_reuniao.text.toString().isNotBlank()) {
                strHoraFimReuniao = edtxt_hora_fim_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Hora de fim da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            if (edtxt_descricao_reuniao.text.toString().isNotBlank()) {
                strDescricaoReuniao = edtxt_descricao_reuniao.text.toString()
            } else {
                msgErrors += "O campo \'Descrição da reunião\' não contém nenhum caractere ou contém apenas caracteres de espaço em branco.\n"
            }

            try {
                val dataInicio = LocalDate.parse(strDataInicioReuniao, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val horaInicio = LocalTime.parse(strHoraInicioReuniao, DateTimeFormatter.ofPattern("HH:mm:00"))
                val startDateTime = LocalDateTime.of(dataInicio, horaInicio)

                val timeZonePortugal = ZoneId.of("Europe/Lisbon")
                val zonedDateTimePortugal = ZonedDateTime.of(startDateTime, timeZonePortugal)
                val zonedDateTimeUTC = zonedDateTimePortugal.withZoneSameInstant(ZoneOffset.UTC)
                strDataHoraInicioReuniao = zonedDateTimeUTC.toLocalDateTime().format(dateTimeFormat)

                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val dataHora = LocalDateTime.parse(strDataHoraInicioReuniao, formatter)
                horarioInicio = Date.from(dataHora.toInstant(java.time.ZoneOffset.UTC))
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                msgErrors += "O formato da data ou da hora de início da reunião é inválido. Certifique-se de que a data esteja no formato \"AAAA-MM-DD\" e a hora esteja no formato \"HH:MM:SS\" e tente novamente.\n"
            } catch (exception: Exception) {
                exception.printStackTrace()
                msgErrors += "Ocorreu um erro inesperado ao processar a data e a hora de início da reunião. Verifique se o formato da data e da hora está correto e tente novamente.\n"
            }

            try {
                val dataFim = LocalDate.parse(strDataFimReuniao, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val horaFim = LocalTime.parse(strHoraFimReuniao, DateTimeFormatter.ofPattern("HH:mm:00"))
                val endDateTime = LocalDateTime.of(dataFim, horaFim)

                val timeZonePortugal = ZoneId.of("Europe/Lisbon")
                val zonedDateTimePortugal = ZonedDateTime.of(endDateTime, timeZonePortugal)
                val zonedDateTimeUTC = zonedDateTimePortugal.withZoneSameInstant(ZoneOffset.UTC)
                strDataHoraFimReuniao = zonedDateTimeUTC.toLocalDateTime().format(dateTimeFormat)

                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val dataHora = LocalDateTime.parse(strDataHoraFimReuniao, formatter)
                horarioFim = Date.from(dataHora.toInstant(java.time.ZoneOffset.UTC))
            } catch (dateTimeParseException: DateTimeParseException) {
                dateTimeParseException.printStackTrace()
                msgErrors += "O formato da data ou da hora de fim da reunião é inválido. Certifique-se de que a data esteja no formato \"AAAA-MM-DD\" e a hora esteja no formato \"HH:MM:SS\" e tente novamente.\n"
            } catch (exception: Exception) {
                exception.printStackTrace()
                msgErrors += "Ocorreu um erro inesperado ao processar a data e a hora de fim da reunião. Verifique se o formato da data e da hora está correto e tente novamente.\n"
            }

            if (horarioFim != null && horarioInicio != null) {
                val now = Date()

                if (horarioInicio.before(now)) {
                    msgErrors += "O horário de início da reunião é inferior ao horário atual. Por favor, coloque um horário de início da reunião superior ao horário atual.\n"
                    println("O horário de início da reunião é inferior ao horário atual.")
                } else if (horarioFim.after(horarioInicio)) {
                    println("O horário de fim da reunião é superior ao horário de início.")
                } else if (horarioFim.before(horarioInicio)) {
                    msgErrors += "O horário de fim da reunião é inferior ao horário de início. Por favor, coloque um horário de fim da reunião superior ao horário de início.\n"
                    println("O horário de fim da reunião é inferior ao horário de início.")
                } else {
                    msgErrors += "O horário de fim da reunião é igual ao horário de início. Por favor, coloque um horário de fim da reunião superior ao horário de início.\n"
                    println("O horário de fim da reunião é igual ao horário de início.")
                }
            }

            if (chbx_notificar_participantes.isChecked) {
                if (tempoNotificacaoList.isNotEmpty()) {
                    numMinutos = (tempoNotificacaoList[positionAvisoPrevioReuniao].NumMinutos).toLong()
                }
            } else {
                strDataHoraNotificacao = null
            }

            if (msgErrors.isBlank()) {
                if (chbx_notificar_participantes.isChecked && numMinutos > 0) {
                    try {
                        val dateTime = LocalDateTime.parse(strDataHoraInicioReuniao, dateTimeFormat)
                        val dateTimeNotification = dateTime.minusMinutes(numMinutos)

                        val timeZonePortugal = ZoneId.of("Europe/Lisbon")
                        val zonedDateTimePortugal = ZonedDateTime.of(dateTimeNotification, timeZonePortugal)
                        val zonedDateTimeUTC = zonedDateTimePortugal.withZoneSameInstant(ZoneOffset.UTC)
                        strDataHoraNotificacao = zonedDateTimeUTC.toLocalDateTime().format(dateTimeFormat)
                    } catch (dateTimeParseException: DateTimeParseException) {
                        dateTimeParseException.printStackTrace()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }

                if (globalVariables.checkForInternet(requireContext())) {
                    if (GlobalVariables.criarReuniaoOutros) {
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show()
                        postReuniao(
                            requireContext(),
                            GlobalVariables.idUtilizadorAutenticado,
                            strTituloReuniao,
                            strDescricaoReuniao,
                            strDataHoraInicioReuniao,
                            strDataHoraFimReuniao,
                            strDataHoraNotificacao
                        )
                    } else {
                        Toast.makeText(requireContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show()
                        putReuniao(
                            requireContext(),
                            strTituloReuniao,
                            strDescricaoReuniao,
                            strDataHoraInicioReuniao,
                            strDataHoraFimReuniao
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
            if (GlobalVariables.criarReuniaoOutros) {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
                fragment.commit()
            } else {
                val fragment = parentFragmentManager.beginTransaction()
                fragment.replace(R.id.fragment_container, FragmentDetalhesReuniaoAdmin())
                fragment.commit()
            }
        }
    }

    private fun postReuniao(
        context: Context,
        numUtilizador: Int,
        titulo: String,
        descricao: String,
        dataHoraIncio: String,
        dataHoraFim: String,
        dataHoraNotificacao: String?
    ) {
        data class Post(
            val NUsuarioCriador: Int,
            val Titulo: String,
            val Descricao: String,
            val Tipo: Int,
            val DataHoraInicio: String,
            val DataHoraFim: String,
            val DataHoraNotificacao: String?
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(
            numUtilizador,
            titulo,
            descricao,
            2,
            dataHoraIncio,
            dataHoraFim,
            dataHoraNotificacao
        )
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/reunioes")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Falha na solicitação de criação da reunião: ${e.message}")

                try {
                    requireActivity().runOnUiThread {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Erro")
                        builder.setMessage("Falha na solicitação de criação da reunião: ${e.message}")
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
                            val NReunioes = message.getInt("NReunioes")
                            val NUsuarioCriador = message.getInt("NUsuarioCriador")
                            val Tipo = message.getInt("Tipo")
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

                            GlobalVariables.detalhesNumReuniao = NReunioes
                            GlobalVariables.detalhesTituloReuniao = Titulo
                            GlobalVariables.detalhesTipoReuniao = Tipo
                            GlobalVariables.detalhesDescricaoReuniao = Descricao
                            GlobalVariables.detalhesDataHoraInicioReuniao = DataHoraInicio
                            GlobalVariables.detalhesDataHoraFimReuniao = DataHoraFim
                            GlobalVariables.detalhesDataHoraNotificacaoReuniao = DataHoraNotificacao
                            GlobalVariables.detalhesNumUsuarioCriadorReuniao = NUsuarioCriador
                            GlobalVariables.detalhesNomeUsuarioCriadorReuniao = NomeUsuarioCriador

                            msgPosts += if (Titulo.isBlank()) {
                                "<b>Criação da reunião:</b><br>A reunião foi criada com sucesso.<br>"
                            } else {
                                "<b>Criação da reunião:</b><br>A reunião \'${Titulo}\' foi criada com sucesso.<br>"
                            }

                            if (globalVariables.checkForInternet(context)) {
                                postUtilizadorCriadorReuniao(
                                    context,
                                    NUsuarioCriador,
                                    NReunioes
                                )
                            } else {
                                Toast.makeText(context, "Sem conexão à Internet.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val message = jsonObject.getString("message")
                            msgPosts += "<b>Criação da reunião:</b><br><b>Falha na criação da reunião:</b> $message<br>"

                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(Html.fromHtml(msgPosts))
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
                                fragment.commit()
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

    private fun postUtilizadorCriadorReuniao(context: Context, numUtilizador: Int, numReuniao: Int) {
        data class Post(val NUsuario: Int, val NReunioes: Int)

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(numUtilizador, numReuniao)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/usuarioreunioes")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Falha na solicitação de agregação do utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' à reunião: ${e.message}")

                try {
                    requireActivity().runOnUiThread {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Erro")
                        builder.setMessage("Falha na solicitação de agregação do utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' à reunião: ${e.message}")
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
                            val NReunioes = message.getInt("NReunioes")

                            msgPosts += "<b>Agregação do utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' à reunião:</b><br>O utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' foi adicionado à lista de participantes da reunião com sucesso.<br>"
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(Html.fromHtml(msgPosts))
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerUtilizadoresReuniao())
                                fragment.commit()
                            }
                            builder.show()
                        } else {
                            val message = jsonObject.getString("message")
                            msgPosts += "<b>Agregação do utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' à reunião:</b><br><b>Falha na agregação do utilizador \'${GlobalVariables.nomeUtilizadorAutenticado}\' à reunião:</b> $message<br>"
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Aviso")
                            builder.setMessage(Html.fromHtml(msgPosts))
                            builder.setIcon(R.drawable.ic_information)
                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                val fragment = parentFragmentManager.beginTransaction()
                                fragment.replace(R.id.fragment_container, FragmentVerAdministracao())
                                fragment.commit()
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

    private fun putReuniao(
        context: Context,
        titulo: String,
        descricao: String,
        dataHoraIncio: String,
        dataHoraFim: String
    ) {
        data class Post(
            val Titulo: String,
            val Descricao: String,
            val DataHoraInicio: String,
            val DataHoraFim: String
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(GlobalVariables.DefaultContentTypeInterceptor())
            .build()

        val post = Post(titulo, descricao, dataHoraIncio, dataHoraFim)
        val requestBody = Gson().toJson(post).toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(GlobalVariables.serverUrl + "/api/reunioes/" + GlobalVariables.detalhesNumReuniao)
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
                            builder.setMessage("A reunião foi atualizada com sucesso.")
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