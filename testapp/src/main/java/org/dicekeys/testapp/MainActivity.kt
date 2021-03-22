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
import org.dicekeys.api.Api
import org.dicekeys.api.DiceKeysWebApiClient
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_DICEKEY = 1
        const val RC_DISPLAY_DICE = 2
    }
    private lateinit var buttonStart: Button
    private lateinit var resultTextView: TextView
    private lateinit var api : DiceKeysWebApiClient
    val recipeJson = "{}"
    val testMessage = "The secret ingredient is dihydrogen monoxide"
    val testMessageByteArray = testMessage.toByteArray(Charsets.UTF_8)

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { api.handleResult( it ) }
    }
    // https://dicekeys.org/api?command=getSecret&respondTo=https%3A%2F%2Ftest-app.dicekeys.org%2Fapi&requestId=getSecret%3AkYMz1_uX57XjdgxSKOgocg%3D%3D%0A&recipeJson=%7B%7D

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      // api = DiceKeysIntentApiClient.create(this)
        api = DiceKeysWebApiClient.create(this, "https://test-app.dicekeys.org/api", "https://dicekeys.org/api")
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text)
        buttonStart = findViewById(R.id.btn_start)

        buttonStart.setOnClickListener{ GlobalScope.launch(Dispatchers.Main) { try {
            val seed = api.getSecret(recipeJson)
            resultTextView.text = "Seed=${Base64.encodeToString(seed.secretBytes, Base64.DEFAULT)}"
            val packagedSealedMessage = api.sealWithSymmetricKey(
                recipeJson,
                testMessageByteArray
            )
            resultTextView.text = "${resultTextView.text}\nSymmetrically sealed message '${testMessage}' as ciphertext '${Base64.encodeToString(packagedSealedMessage.ciphertext, Base64.DEFAULT)}'"
            val plaintext = api.unsealWithSymmetricKey(packagedSealedMessage)
            resultTextView.text =
                "${resultTextView.text}\nUnsealed '${String(plaintext, Charsets.UTF_8)}'"
            val sig = api.generateSignature(recipeJson, testMessageByteArray)
            resultTextView.text =
                "${resultTextView.text}\nSigned test message '${Base64.encodeToString(sig.signature, Base64.DEFAULT)}'"
            val signatureVerificationKey = api.getSignatureVerificationKey(recipeJson)
            val keysMatch = signatureVerificationKey == sig.signatureVerificationKey
            val verified = signatureVerificationKey.verifySignature(testMessageByteArray, sig.signature)
            resultTextView.text =
                "${resultTextView.text}\nVerification key match=${keysMatch}, verification result=${verified}"
            val publicKey = api.getSealingKey(recipeJson)
            val packagedSealedPkMessage = publicKey.seal(testMessageByteArray, """{
               |  "requireUsersConsent": {
               |     "question": "Do you want use \"8fsd8pweDmqed\" as your SpoonerMail account password and remove your current password?",
               |     "actionButtonLabels": {
               |         "allow": "Make my password \"8fsd8pweDmqed\"",
               |         "deny": "No" 
               |     }
               |  }   
               |}""".trimMargin())
            resultTextView.text = "${resultTextView.text}\ngetSealingkey publicKey='${publicKey.toJson()}' as ciphertext='${Base64.encodeToString(packagedSealedPkMessage.ciphertext, Base64.DEFAULT)}'"
            val pkPlaintext = api.unsealWithUnsealingKey(packagedSealedPkMessage)
            resultTextView.text = "${resultTextView.text}\nUnsealed '${String(pkPlaintext, Charsets.UTF_8)}'"
            resultTextView.text = "${resultTextView.text}\nTests complete"
        } catch (e: Exception) {
            resultTextView.text = "${resultTextView.text}\nException $e"
        }}}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        // intent?.let { api.handleOnActivityResult(it) }
        intent?.data?.let { api.handleResult( it ) }
    }


}
