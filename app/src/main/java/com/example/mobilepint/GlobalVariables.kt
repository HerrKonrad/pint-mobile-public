package com.example.mobilepint

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.ParseException
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Patterns
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*

class GlobalVariables {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:00", Locale.getDefault())

    fun checkForInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false
        return (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ))
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[a-zA-Z])(?=.*[0-9]).{6,}\$".toRegex()
        return password.matches(passwordPattern)
    }

    fun isValidDate(dateStr: String): Boolean {
        dateFormat.isLenient = false
        return try {
            val date = dateFormat.parse(dateStr)
            date != null
        } catch (parseException: ParseException) {
            false
        } catch (exception: Exception) {
            false
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.replace("[\\s\\-]".toRegex(), "")
        val pattern = Regex("^([26789])\\d{8}$")
        return pattern.matches(cleanPhone)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    companion object {
        var serverUrl: String = "https://pint-2023-api.onrender.com"

        var preferencesLogin: SharedPreferences? = null
        var LOGIN: String = "login"
        var LOGIN_EMAIL: String = "email"
        var LOGIN_PASS: String = "pass"

        var token: String = ""
        var idUtilizadorAutenticado: Int = 0
        var idCargoUtilizadorAutenticado: Int = 0
        var nomeUtilizadorAutenticado: String = ""
        var emailUtilizadorAutenticado: String = ""
        var fotoUtilizadorAutenticado: String = ""

        // ------------ ver os detalhes da vaga ------------
        var detalhesNumVaga: Int = 0
        var detalhesNomeVaga: String = ""
        var detalhesSubtituloVaga: String = ""
        var detalhesDescricaoVaga: String = ""
        var detalhesNumLocalidadeVaga: Int = 0
        var detalhesNumTipoVaga: Int = 0
        var detalhesEstadoVaga: Int = 0
        var detalhesLocalidadeVaga: String = ""
        var detalhesTipoVaga: String = ""
        // ------------ ver os detalhes da vaga ------------

        // ------------ ver os detalhes da candidatura ------------
        var detalhesNumCandidatura: Int = 0
        var detalhesNumVagaCandidatura: Int = 0
        var detalhesNumUsuarioCandidatura: Int = 0
        var detalhesDataCandidatura: String = ""
        var detalhesPretencaoSalarial: String = ""
        var detalhesMensagemCandidatura: String = ""
        var detalhesEstadoCandidatura: Int = 0
        var detalhesEstagioCandidatura: String = ""
        var detalhesNomeUsuarioCandidatura: String = ""
        var detalhesNomeVagaCandidatura: String = ""
        var detalhesSubtituloVagaCandidatura: String = ""
        var detalhesEmailUsuarioCandidatura: String = ""
        // ------------ ver os detalhes da candidatura ------------

        // ------------ ver os detalhes da entrevista ------------
        var detalhesNumEntrevista: Int = 0
        var detalhesDescricaoEntrevista: String = ""
        var detalhesEstadoEntrevista: String = ""
        // ------------ ver os detalhes da entrevista ------------

        // ------------ ver os detalhes da nota ------------
        var detalhesNumNota: Int = 0
        var detalhesTextoNota: String = ""
        var detalhesAutorNota: String = ""
        var detalhesDataNota: String = ""
        // ------------ ver os detalhes da nota ------------

        // ------------ ver os detalhes da reunião ------------
        var detalhesNumReuniao: Int = 0
        var detalhesTituloReuniao: String = ""
        var detalhesTipoReuniao: Int = 0
        var detalhesDescricaoReuniao: String = ""
        var detalhesDataHoraInicioReuniao: String = ""
        var detalhesDataHoraFimReuniao: String = ""
        var detalhesDataHoraNotificacaoReuniao: String = ""
        var detalhesNumUsuarioCriadorReuniao: Int = 0
        var detalhesNomeUsuarioCriadorReuniao: String = ""
        // ------------ ver os detalhes da reunião ------------

        // ------------ ver os detalhes do benefício ------------
        var detalhesNumBeneficio: Int = 0
        var detalhesNomeBeneficio: String = ""
        var detalhesSubtituloBeneficio: String = ""
        var detalhesDescricaoBeneficio: String = ""
        var detalhesEnderecoImagemBeneficio: String = ""
        // ------------ ver os detalhes do benefício ------------

        // ------------ ver os detalhes da ideia ------------
        var detalhesNumIdeia: Int = 0
        var detalhesNumUsuarioIdeia: Int = 0
        var detalhesTituloIdeia: String = ""
        var detalhesDataIdeia: String = ""
        var detalhesEstadoIdeia: String = ""
        var detalhesDescricaoIdeia: String = ""
        var detalhesNomeUsuarioIdeia: String = ""
        // ------------ ver os detalhes da ideia ------------

        // ------------ ver os detalhes da oportunidade ------------
        var detalhesNumOportunidade: Int = 0
        var detalhesTituloOportunidade: String = ""
        var detalhesValorOportunidade: String = ""
        var detalhesDescricaoOportunidade: String = ""
        var detalhesDataCriacaoOportunidade: String = ""
        var detalhesNumEtiquetaOportunidade: Int = 0
        var detalhesNomeEtiquetaOportunidade: String = ""
        var detalhesNumTipoProjetoOportunidade: Int = 0
        var detalhesNomeTipoProjetoOportunidade: String = ""
        var detalhesNumEstagioOportunidade: Int = 0
        var detalhesNomeEstagioOportunidade: String = ""
        var detalhesNumUsuarioCriadorOportunidade: Int = 0
        var detalhesNomeUsuarioCriadorOportunidade: String = ""
        // ------------ ver os detalhes da oportunidade ------------

        // ------------ ver os detalhes da relação estabelecida ------------
        var detalhesNumStatus: Int = 0
        var detalhesTituloStatus: String = ""
        var detalhesDescricaoStatus: String = ""
        var detalhesEnderecoAnexoStatus: String = ""
        var detalhesDataHoraCriacaoStatus: String = ""
        // ------------ ver os detalhes da relação estabelecida ------------

        // ------------ ver os detalhes do cliente ------------
        var detalhesNumCliente: Int = 0
        var detalhesNomeCliente: String = ""
        var detalhesEmailCliente: String = ""
        var detalhesTelefoneCliente: String = ""
        var detalhesDescricaoCliente: String = ""
        var detalhesNumUsuarioCriadorCliente: Int = 0
        var detalhesNomeUsuarioCriadorCliente: String = ""
        var detalhesDataCriacaoCliente: String = ""
        // ------------ ver os detalhes do cliente ------------

        // ------------ ver os detalhes do contacto ------------
        var detalhesNumContacto: Int = 0
        var detalhesTelefoneContacto: String = ""
        var detalhesEmailContacto: String = ""
        // ------------ ver os detalhes do contacto ------------

        // ------------ ver os detalhes do utilizador ------------
        var detalhesNumUtilizador: Int = 0
        var detalhesNomeUtilizador: String = ""
        var detalhesEmailUtilizador: String = ""
        var detalhesNumCargoUtilizador: Int = 0
        var detalhesTelefoneUtilizador: String = ""
        var detalhesLinkedinUtilizador: String = ""
        var detalhesDataNascimentoUtilizador: String = ""
        var detalhesGeneroUtilizador: String = ""
        var detalhesLocalidadeUtilizador: String = ""
        var detalhesEstadoUtilizador: Int = 0
        var detalhesDataCriacaoUtilizador: String = ""
        // ------------ ver os detalhes do utilizador ------------

        var dataFormatadaCalendario: String = ""

        var criarContaVerInicio = true // se falso -> ver login
        var criarVaga: Boolean = true // se falso -> editar vaga
        var criarIdeia: Boolean = true // se falso -> editar ideia
        var criarBeneficio: Boolean = true // se falso -> editar beneficio
        var criarCandidatura: Boolean = true // se falso -> abrir candidatura (detalhes)
        var criarReuniaoEntrevista: Boolean = true // se falso -> editar reunião
        var criarNotaEntrevista: Boolean = true // se falso -> editar nota
        var criarOportunidade: Boolean = true // se falso -> editar oportunidade
        var criarRelacaoCliente: Boolean = true // se falso -> editar relação
        var criarReuniaoOportunidade: Boolean = true // se falso -> editar reunião
        var criarCliente: Boolean = true // se falso -> editar cliente
        var criarContactoCliente: Boolean = true // se falso -> editar contacto
        var criarEtiquetaEstagio: Boolean = true // se falso -> editar etiqueta ou estágio
        var criarTipoProjeto: Boolean = true // se falso -> editar tipo de projeto
        var criarUtilizador: Boolean = true // se falso -> editar utilizador
        var criarReuniaoOutros: Boolean = true // se falso -> editar reunião
        var criarLocalidade: Boolean = true // se falso -> editar localidade
        var criarTopicoIdeias: Boolean = true // se falso -> editar tópico das ideias

        var administrationOption = 0 // Utilizadores
        var verReuniaoAdministracao = true // se falso -> ver reunião entrevistas ou oportunidades
        var verVagas = true // se falso -> editar vagas
        var verEtiquetas = true // se falso -> ver estágios
    }

    class DefaultContentTypeInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestWithHeaders = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .build()

            return chain.proceed(requestWithHeaders)
        }
    }
}