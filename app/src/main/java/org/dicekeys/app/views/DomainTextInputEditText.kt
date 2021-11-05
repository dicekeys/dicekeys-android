package org.dicekeys.app.views

import android.content.Context
import android.util.AttributeSet
import org.dicekeys.app.utils.copyToClipboard
import org.dicekeys.app.utils.getClipboard
import org.dicekeys.app.utils.getWildcardOfRegisteredDomainFromCandidateWebUrl

class DomainTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
) : PlaceholderTextInputEditText(context, attrs, defStyleAttr) {

    override fun onTextContextMenuItem(id: Int): Boolean {
        return if(id == android.R.id.paste || id == android.R.id.pasteAsPlainText){
            val originalClipboardData = getClipboard(context)

            // Replace domain
            getWildcardOfRegisteredDomainFromCandidateWebUrl(originalClipboardData)?.let {
                var wildcardDomain = it
                if(text?.isNotEmpty() == true && text?.trim()?.endsWith(',') == false){
                    wildcardDomain = ",$wildcardDomain"
                }

                copyToClipboard("Domain", wildcardDomain, context)

                super.onTextContextMenuItem(id).also {
                    // restore clipboard
                    copyToClipboard("Domain", originalClipboardData, context)
                }
            } ?: super.onTextContextMenuItem(id)
        }else{
            super.onTextContextMenuItem(id)
        }
    }
}