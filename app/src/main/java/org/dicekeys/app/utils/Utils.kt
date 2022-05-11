package org.dicekeys.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import org.dicekeys.app.R
import org.dicekeys.app.extensions.pulse

fun openBrowser(context: Context, url: String) {

    try {
        val builder = CustomTabsIntent.Builder()
        builder.setShowTitle(true)
        builder.setUrlBarHidingEnabled(false)
        builder.setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.md_theme_light_primary))
                .setNavigationBarColor(ContextCompat.getColor(context, R.color.md_theme_light_primary))
                .build()
        )

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getClipboard(context: Context): String? =
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).let {
            it.primaryClip?.getItemAt(0)?.text?.toString()
        }

fun copyToClipboard(label: String, content: String?, context: Context, viewToPulse : View? = null) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).let {
        it.setPrimaryClip(ClipData.newPlainText(label, content))
    }
    viewToPulse?.pulse()
}