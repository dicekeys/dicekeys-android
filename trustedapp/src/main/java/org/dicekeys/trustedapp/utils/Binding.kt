package org.dicekeys.trustedapp.utils

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter


@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}
