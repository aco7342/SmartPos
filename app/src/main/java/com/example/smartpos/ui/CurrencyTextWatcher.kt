package com.example.smartpos.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import java.util.Locale
import java.text.NumberFormat

class CurrencyTextWatcher(editText: EditText, private val onCentsChanged: (Long) -> Unit) : TextWatcher {
    private val editRef = WeakReference(editText)
    private val locale = Locale("pt", "BR")
    private val currency = NumberFormat.getCurrencyInstance(locale)
    private var selfChange = false
    private var lastCents = 0L

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (selfChange) return
        val raw = s?.toString()?.replace("\\D".toRegex(), "") ?: "0"
        val cents = raw.toLongOrNull() ?: 0L
        lastCents = cents
        val formatted = currency.format(cents / 100.0)
        val et = editRef.get() ?: return
        selfChange = true
        et.setText(formatted)
        et.setSelection(formatted.length)
        selfChange = false
        onCentsChanged(cents)
    }
}