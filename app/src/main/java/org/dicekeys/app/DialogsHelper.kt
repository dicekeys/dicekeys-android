package org.dicekeys.app

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun openDialogDeleteDiceKey(context: Context, fn: (() -> Unit)) {
    MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete)
            .setMessage(R.string.ask_dicekey_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                fn.invoke()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
}