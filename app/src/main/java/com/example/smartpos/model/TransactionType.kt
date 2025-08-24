// model/TransactionType.kt
package com.example.smartpos.model

sealed class TransactionType {
    object Credito : TransactionType()
    object Debito  : TransactionType()
    data class Resultado(val sucesso: Boolean, val mensagem: String) : TransactionType()
}