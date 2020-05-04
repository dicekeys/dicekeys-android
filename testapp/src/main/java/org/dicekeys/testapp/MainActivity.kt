package org.dicekeys.testapp

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dicekeys.api.DiceKeysIntentApiClient
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_KEYSQR = 1
        const val RC_DISPLAY_DICE = 2
    }
    private lateinit var buttonStart: Button
    private lateinit var diceKeysApiClient: DiceKeysIntentApiClient
    private lateinit var resultTextView: TextView
    val derivationOptionsJson = "{}"
    val testMessage = "The secret ingredient is dihydrogen monoxide"
    val testMessageByteArray = testMessage.toByteArray(Charsets.UTF_8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diceKeysApiClient = DiceKeysIntentApiClient.create(this)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text)
        buttonStart = findViewById(R.id.btn_start)

        buttonStart.setOnClickListener{ GlobalScope.launch(Dispatchers.Main) { try {
            val seed = diceKeysApiClient.getSecret(derivationOptionsJson)
            resultTextView.text = "Seed=${Base64.encodeToString(seed.secretBytes, Base64.DEFAULT)}"
            val packagedSealedMessage = diceKeysApiClient.sealWithSymmetricKey(
                derivationOptionsJson,
                testMessageByteArray
            )
            resultTextView.text = "${resultTextView.text}\nSymmetrically sealed message '${testMessage}' as ciphertext '${Base64.encodeToString(packagedSealedMessage.ciphertext, Base64.DEFAULT)}'"
            val plaintext = diceKeysApiClient.unsealWithSymmetricKey(packagedSealedMessage)
            resultTextView.text =
                "${resultTextView.text}\nUnsealed '${String(plaintext, Charsets.UTF_8)}'"
            val sig = diceKeysApiClient.generateSignature(derivationOptionsJson, testMessageByteArray)
            resultTextView.text =
                "${resultTextView.text}\nSigned test message '${Base64.encodeToString(sig.signature, Base64.DEFAULT)}'"
            val signatureVerificationKey = diceKeysApiClient.getSignatureVerificationKey(derivationOptionsJson)
            val keysMatch = signatureVerificationKey == sig.signatureVerificationKey
            val verified = signatureVerificationKey.verifySignature(testMessageByteArray, sig.signature)
            resultTextView.text =
                "${resultTextView.text}\nVerification key match=${keysMatch}, verification result=${verified}"
            val publicKey = diceKeysApiClient.getSealingKey(derivationOptionsJson)
            val packagedSealedPkMessage = publicKey.seal(testMessageByteArray, """{
               |  "requireUsersConsent": {
               |     "question": "Do you want use \"8fsd8pweDmqed\" as your SpoonerMail account password and remove your current password?",
               |     "actionButtonLabels": {
               |         "allow": "Make my password \"8fsd8pweDmqed\"",
               |         "deny": "No" 
               |     }
               |  }   
               |}""".trimMargin())
            resultTextView.text = "${resultTextView.text}\ngetPublicKey publicKey='${publicKey.toJson()}' as ciphertext='${Base64.encodeToString(packagedSealedPkMessage.ciphertext, Base64.DEFAULT)}'"
            val pkPlaintext = diceKeysApiClient.unsealWithUnsealingKey(packagedSealedPkMessage)
            resultTextView.text = "${resultTextView.text}\nUnsealed '${String(pkPlaintext, Charsets.UTF_8)}'"
            resultTextView.text = "${resultTextView.text}\nTests complete"
        } catch (e: Exception) {
            resultTextView.text = "${resultTextView.text}\nException $e"
        }}}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { diceKeysApiClient.handleOnActivityResult(it) }
    }


}
