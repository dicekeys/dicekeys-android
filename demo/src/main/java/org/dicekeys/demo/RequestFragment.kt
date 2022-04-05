package org.dicekeys.demo

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dicekeys.demo.databinding.DialogEditTextBinding
import org.dicekeys.demo.databinding.FragmentRequestBinding
import java.net.URLEncoder
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RequestFragment : Fragment() {

    private var requestId = 0
    private lateinit var binding: FragmentRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRequestBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetSealingKey.setOnClickListener {
            sendRequest(constructRequest("getSealingKey"))
        }

        binding.buttonSealWithSymmetricKey.setOnClickListener {
            getUserInput(hint = "Plaintext", text = "this is the plaintext") {
                sendRequest(constructRequest("sealWithSymmetricKey", plaintext = it))
            }
        }
        binding.buttonUnsealWithSymmetricKey.setOnClickListener {
            getUserInput(hint = "Packaged Sealed Message JSON") {
                sendRequest(
                    constructRequest(
                        "unsealWithSymmetricKey",
                        withRecipe = false,
                        packagedSealedMessageJson = it
                    )
                )
            }
        }

        binding.buttonGetPassword.setOnClickListener {
            sendRequest(constructRequest("getPassword"))
        }

        binding.buttonGetSecret.setOnClickListener {
            sendRequest(constructRequest("getSecret"))
        }

        binding.buttonGetUnsealingKey.setOnClickListener {
            sendRequest(constructRequest("getUnsealingKey", true, clientMayRetrieveKey = true))
        }

        binding.buttonGetSymmetricKey.setOnClickListener {
            sendRequest(constructRequest("getSymmetricKey", true, clientMayRetrieveKey = true))
        }
    }

    private fun getUserInput(hint: String, text: String = "", callback: ((input: String) -> Unit)) {
        val dialogEditText = DialogEditTextBinding.inflate(layoutInflater).also {
            it.textInputEditText.setText(text)
            it.textInputLayout.hint = hint
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("User input")
            .setView(dialogEditText.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                callback(dialogEditText.textInputEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun constructRequest(
        command: String,
        withRecipe: Boolean = true,
        clientMayRetrieveKey: Boolean = false,
        plaintext: String? = null,
        packagedSealedMessageJson: String? = null
    ): String {
        val respondTo = URLEncoder.encode("https://dicekeys.app/--derived-secret-api--/", "UTF-8")
        val base64Plaintext = if (!plaintext.isNullOrBlank()) {
            Base64.encodeToString(plaintext.toByteArray(), Base64.NO_WRAP)?.let {
                "&plaintext=$it"
            }
        } else ""

        val packagedSealedMessageJsonUrlEncoded = if (!packagedSealedMessageJson.isNullOrBlank()) {
            URLEncoder.encode(packagedSealedMessageJson, "UTF-8").let {
                "&packagedSealedMessageJson=$it"
            }
        } else ""

        val recipe = if (withRecipe) {
            URLEncoder.encode(
                if (clientMayRetrieveKey) {
                    "{\"allow\":[{\"host\":\"dicekeys.app\"}],\"clientMayRetrieveKey\":true}"
                } else {
                    "{\"allow\":[{\"host\":\"dicekeys.app\"}]}"
                }, "UTF-8"
            ).let {
                "&recipe=$it"
            }
        } else ""

        return "https://dicekeys.app/?command=$command&requestId=${requestId++}${recipe}&respondTo=${respondTo}${base64Plaintext}${packagedSealedMessageJsonUrlEncoded}&recipeMayBeModified=false".also {
            println(it)
        }
    }

    private fun sendRequest(request: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.data = Uri.parse(request)
        startActivity(browserIntent)
    }

}