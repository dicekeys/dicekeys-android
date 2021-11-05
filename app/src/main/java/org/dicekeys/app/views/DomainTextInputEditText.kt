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

    // Override _paste_ operation on the domain text field to replace pasted URLs with
    // the registered domain in the URL's host field preceded by a wildcard.
    // (e.g., pasting `https://secure.3.example.com/somepath?a=b` actually pastes `*.example.com`.)
    override fun onTextContextMenuItem(id: Int): Boolean {
        return if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            val originalClipboardData = getClipboard(context)

            // If the originalClipboardData is web URL starting with http(s), this call will capture the
            // registered domain, prepend a wildcard (*.) and return it.
            var wildcardDomain = getWildcardOfRegisteredDomainFromCandidateWebUrl(originalClipboardData)
            if(wildcardDomain.isNullOrBlank()) {
                super.onTextContextMenuItem(id)
            }else{
                if (text?.isNotEmpty() == true && text?.trim()?.endsWith(',') == false) {
                    wildcardDomain = ",$wildcardDomain"
                }

                // Rather than override the paste, copy the wildcardDomain into the clipboard and the
                // restore the original clipboard contents after the paste
                copyToClipboard("Domain", wildcardDomain, context)
                super.onTextContextMenuItem(id)
                copyToClipboard("Domain", originalClipboardData, context)
                return true
            }
        } else {
            super.onTextContextMenuItem(id)
        }
    }
}