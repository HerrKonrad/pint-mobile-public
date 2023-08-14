package com.example.mobilepint

enum class EnumCargos(val numCargo: Int, val nomeCargo: String) {
    ADMINISTRADOR(0, "Administrador"),
    UTILIZADOR_EXTERNO(1, "Utilizador Externo"),
    UTILIZADOR_INTERNO(2, "Utilizador Interno"),
    GESTOR_VENDAS(3, "Gestor Vendas"),
    GESTOR_IDEIAS(4, "Gestor Ideias"),
    RECURSOS_HUMANOS(5, "Recursos Humanos")
}