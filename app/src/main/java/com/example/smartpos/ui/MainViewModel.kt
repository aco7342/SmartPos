package com.example.smartpos.ui

import android.icu.math.BigDecimal
import android.icu.text.NumberFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpos.model.PaymentMethod
import com.example.smartpos.model.TransactionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.smartpos.ui.CurrencyTextWatcher
import java.util.Locale

class MainViewModel : ViewModel() {

    data class UiModel(
        val valorCentavos: Long = 0L,
        val metodo: PaymentMethod? = null,
        val estado: TransactionState = TransactionState.COLETA,
        val podeProcessar: Boolean = false,
		var sucesso: Boolean = false
    )

    private val _ui = MutableStateFlow(UiModel())
    val ui: StateFlow<UiModel> = _ui.asStateFlow()
	

    fun onValorAlterado(valor: Long) {
        _ui.update {
            val value:Long = valor

            it.copy(
                valorCentavos = value,
                podeProcessar = valor > 0 && it.metodo != null && it.estado is TransactionState.COLETA
            )
        }
    }

    fun getSucesso(): Boolean {
        return _ui.value.sucesso
    }

    fun onSelecionaMetodo(metodo: PaymentMethod) {
        _ui.update {
            it.copy(
                metodo = metodo,
                podeProcessar = it.valorCentavos > 0 && it.estado is TransactionState.COLETA
            )
        }
    }

    fun onRealizarTransacao() {
        val snapshot = _ui.value
        if (snapshot.valorCentavos <= 0 || snapshot.metodo == null) return

        _ui.update {
            it.copy(
                estado = TransactionState.PROCESSO(snapshot.valorCentavos,
                    snapshot.metodo as com.example.smartpos.model.PaymentMethod
                ),
                podeProcessar = false
            )
        }

        viewModelScope.launch {
            val snapshot = _ui.value
            val timeinmsecs = Random.nextInt(1, 3)
            val locale = Locale("pt", "BR")
            val currency = NumberFormat.getCurrencyInstance(locale)
            delay(timeinmsecs * 1000L)
            val formatted = currency.format(snapshot.valorCentavos/ 100.0)
            val sucesso_ = if (Random.nextInt(10) < 7)  true else false // approved less 7
            val amount = formatted ?: "R$ 0,00"
            val trnNome:String = if (snapshot.metodo == com.example.smartpos.model.PaymentMethod.CREDITO) "Crédito" else "Débito"
            val msg = "Transação "+ trnNome + if (sucesso_) " aprovada Valor:"+ amount else " recusada"
            _ui.update { it.copy(
                estado = TransactionState.PROCESSADO(sucesso_, msg),
                sucesso = sucesso_
            ) }
        }
    }

    fun resetParaColeta() {
        _ui.value = UiModel()
    }
    fun resetEstado(){
        _ui.update {
            it.copy(
                metodo = null,
                podeProcessar = false,
				sucesso = false,
                valorCentavos =0L,
                estado = TransactionState.COLETA
            )
        }
    }
}