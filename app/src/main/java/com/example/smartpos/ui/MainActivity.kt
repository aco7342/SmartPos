package com.example.smartpos.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.smartpos.databinding.ActivityMainBinding
//import com.example.smartpos.model.PaymentMethod
import com.example.smartpos.model.TransactionState
import com.example.smartpos.R
import com.example.smartpos.model.PaymentMethod
import kotlinx.coroutines.launch
import com.example.smartpos.ui.CurrencyTextWatcher
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCredito.setOnClickListener { viewModel.onSelecionaMetodo(PaymentMethod.CREDITO) }
        binding.btnDebito.setOnClickListener { viewModel.onSelecionaMetodo(PaymentMethod.DEBITO) }
        binding.btnRealizar.setOnClickListener { viewModel.onRealizarTransacao() }

        // Branding text via resValue/string dos flavors
        val brand = getString(R.string.brand_name)
        val slogan = getString(R.string.brand_slogan)
        binding.txtBranding.text = "$brand — $slogan"
		
        // Currency watcher
        val watcher = CurrencyTextWatcher(binding.edtAmount) { cents ->
            viewModel.onValorAlterado(cents)
        }
        binding.edtAmount.addTextChangedListener(watcher)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ui.collect { ui ->
                    binding.btnRealizar.visibility = if (ui.podeProcessar) View.VISIBLE else View.GONE

                    when (ui.estado) {
                        is TransactionState.COLETA -> {
                            binding.txtEstado.text = getString(R.string.estado_coleta)
                        }
                        is TransactionState.PROCESSO -> {
                            binding.txtEstado.text = getString(R.string.estado_processo)
                        }
                        is TransactionState.PROCESSADO -> {
                            binding.txtEstado.text =
                                if (ui.estado.sucesso) getString(R.string.estado_processado_sucesso)
                                else getString(R.string.estado_processado_erro)
                            mostrarDialog(ui.estado.mensagem, ui.estado.sucesso )
                            binding.btnRealizar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun mostrarDialog(msg: String, sucesso: Boolean) {
        val sucesso = viewModel.getSucesso()
		val dlgTextColor = ContextCompat.getColor(this,if (sucesso) R.color.dialogTitleAndMessage else R.color.dialogTitleAndMessageError)

		// Create SpannableStrings
		val title_ = if (sucesso) "Sucesso" else "Falha"
		val title = SpannableString(title_).apply {
			setSpan(ForegroundColorSpan(dlgTextColor), 0, length, 0)
		}

		val messageDlg = SpannableString(msg).apply {
			setSpan(ForegroundColorSpan(dlgTextColor), 0, length, 0)
		}
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(messageDlg)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetEstado()
                val editText: TextInputEditText = findViewById(R.id.edtAmount)
                editText.text?.clear()
            }
            //.show()
			.create()
		
			dialog.setOnShowListener {
				// Background color android.R.color.holo_blue_light
				dialog.window?.setBackgroundDrawableResource(if (sucesso) R.color.dialogBrandPrimary else R.color.dialogBrandPrimaryError)

				val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

				positiveButton.setTextColor(dlgTextColor)

				positiveButton.background.setTint(if (sucesso) ContextCompat.getColor(this, R.color.dialogButtonPrimary) else ContextCompat.getColor(this, R.color.dialogButtonPrimaryError))

			}
			dialog.show()
		
    }
}
