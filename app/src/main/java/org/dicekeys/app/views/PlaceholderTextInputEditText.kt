package org.dicekeys.app.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dicekeys.app.R

class PlaceholderTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {
    private var placeholder: CharSequence? = null

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.PlaceholderTextInputEditText)

        attributes.getString(R.styleable.PlaceholderTextInputEditText_placeholder)?.let {
            placeholder = it
        }

        attributes.recycle()

        onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            onFocusChanged(hasFocus)
        }
    }

    private val isSwitchable by lazy {
        try {
            val viewParent = parent.parent
            if (viewParent is TextInputLayout) {
                if(!viewParent.hint.isNullOrBlank()){
                    return@lazy true
                }
            }
        } catch (e: Exception) {
        }
        false
    }


    private fun onFocusChanged(hasFocus: Boolean) {
        if (isSwitchable) {
            if (hasFocus) {
                postDelayed({ hint = placeholder }, 150)
            } else {
                setHint("")
            }
        }
    }

    fun setPlaceholder(hint: CharSequence?) {
        placeholder = hint
        if (isSwitchable) {
            if (hasFocus()) {
                setHint(placeholder)
            } else {
                setHint("")
            }
        } else {
            setHint(placeholder)
        }
    }
}