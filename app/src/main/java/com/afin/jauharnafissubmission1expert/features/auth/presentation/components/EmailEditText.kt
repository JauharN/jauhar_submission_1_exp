package com.afin.jauharnafissubmission1expert.features.auth.presentation.components

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.afin.jauharnafissubmission1expert.R

// Custom EditText untuk validasi email sesuai kriteria

class EmailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun validateEmail() {
        val email = text.toString()
        error = when {
            email.isEmpty() -> null
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                context.getString(R.string.error_email_invalid)

            else -> null
        }
    }
}