package com.example.smartpos.model

sealed class TransactionState {
    data object COLETA : TransactionState()
    data class PROCESSO(val valorCentavos: Long, val metodo: PaymentMethod) : TransactionState()
    data class PROCESSADO(val sucesso: Boolean, val mensagem: String) : TransactionState()
}