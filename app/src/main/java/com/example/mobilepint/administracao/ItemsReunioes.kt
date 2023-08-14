package com.example.mobilepint.administracao

class ItemsReunioes(
    val NReunioes: Int,
    val NUsuarioCriador: Int,
    val Titulo: String,
    val Descricao: String,
    val Tipo: Int,
    val DataHoraInicio: String,
    val DataHoraFim: String,
    val NOportunidade: Int? = null,
    val NEntrevista: Int? = null,
    val DataHoraNotificacao: String,
    val NomeUsuarioCriador: String
)