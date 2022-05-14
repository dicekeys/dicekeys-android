package org.dicekeys.app.extensions

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.dicekeys.app.R


fun <T> Fragment.getNavigationResult(key: String = "result") = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)
fun Fragment.clearNavigationResult(key: String = "result") = findNavController().currentBackStackEntry?.savedStateHandle?.set(key, null)

fun <T> Fragment.setNavigationResult(
        result: T,
        key: String = "result",
        @IdRes destinationId: Int? = null
) {
    findNavController().apply {
        (if (destinationId != null) getBackStackEntry(destinationId) else previousBackStackEntry)
                ?.savedStateHandle
                ?.set(key, result)
    }
}

fun Fragment.hideKeyboard() {
    view?.let { context?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.errorDialog(throwable: Throwable, listener: (() -> Unit)? = null) {
    throwable.printStackTrace()
    errorDialog(error = throwable.cause?.message ?: throwable.message ?: "An exception occurred", listener = listener)
}

fun Fragment.errorDialog(error: String, listener: (() -> Unit)? = null) {
    MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setMessage(error)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener {
                listener?.invoke()
            }
            .show()
}

fun Fragment.dialog(title: Int, message: Int, listener: (() -> Unit)? = null) {
    dialog(getString(title), getString(message), listener)
}

fun Fragment.dialog(title: String, message: String, listener: (() -> Unit)? = null) {
    MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener {
                listener?.invoke()
            }
            .show()
}

fun Fragment.toast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), stringRes, duration).show()
}

fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, duration).show()
}

fun Fragment.toast(throwable: Throwable, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), throwable.cause?.message ?: throwable.message ?: throwable.toString(), duration).show()
}

fun Fragment.snackbar(resId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    snackbar(getString(resId, duration))
}

fun Fragment.snackbar(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
    view?.let{
        Snackbar.make(it.findViewById<CoordinatorLayout>(R.id.coordinator) ?: it, text, duration ).show()
    }
}

fun Fragment.showPopupMenu(view: View, @MenuRes menuRes: Int, fn: ((popupMenu: PopupMenu) -> Unit)? = null , listener: PopupMenu.OnMenuItemClickListener) {
    val popup = PopupMenu(requireContext(), view, Gravity.END)
    popup.menuInflater.inflate(menuRes, popup.menu)
    popup.setOnMenuItemClickListener(listener)
    fn?.invoke(popup)
    popup.show()
}