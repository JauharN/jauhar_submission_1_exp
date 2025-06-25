package com.afin.jauharnafissubmission1expert.features.auth.presentation.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.afin.jauharnafissubmission1expert.R

// PasswordEditText untuk validasi password sesuai kriteria

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var showPasswordIcon: Drawable
    private var hidePasswordIcon: Drawable
    private var isPasswordVisible = false

    init {
        // Set icons
        showPasswordIcon = ContextCompat.getDrawable(context, R.drawable.ic_eye_open) as Drawable
        hidePasswordIcon = ContextCompat.getDrawable(context, R.drawable.ic_eye_closed) as Drawable

        // Setup
        setOnTouchListener(this)
        transformationMethod = PasswordTransformationMethod.getInstance()

        // Set default drawable
        setButtonDrawables(endOfTheText = hidePasswordIcon)

        // Add text watcher for validation
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            val toggleButtonStart: Float
            val toggleButtonEnd: Float
            var isToggleButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                toggleButtonEnd = (showPasswordIcon.intrinsicWidth + paddingStart).toFloat()
                when {
                    event.x < toggleButtonEnd -> isToggleButtonClicked = true
                }
            } else {
                toggleButtonStart = (width - paddingEnd - showPasswordIcon.intrinsicWidth).toFloat()
                when {
                    event.x > toggleButtonStart -> isToggleButtonClicked = true
                }
            }

            if (isToggleButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        togglePasswordVisibility()
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        transformationMethod = if (isPasswordVisible) {
            setButtonDrawables(endOfTheText = showPasswordIcon)
            HideReturnsTransformationMethod.getInstance()
        } else {
            setButtonDrawables(endOfTheText = hidePasswordIcon)
            PasswordTransformationMethod.getInstance()
        }
        setSelection(text?.length ?: 0)
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    private fun validatePassword() {
        val password = text.toString()
        error = when {
            password.isEmpty() -> null
            password.length < 8 -> context.getString(R.string.error_password_short)
            else -> null
        }
    }
}