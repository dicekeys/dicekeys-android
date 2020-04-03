package org.dicekeys.app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.api.DiceKeysApi
import org.dicekeys.api.SignatureVerificationKey


class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_KEYSQR = 1
        const val RC_DISPLAY_DICE = 2
    }
    private lateinit var buttonStart: Button
    private lateinit var diceKeysApi: DiceKeysApi
    private lateinit var resultTextView: TextView
    val keyDerivationOptionsJson = "{}"
    val testMessage = "The secret ingredient is dihydrogen monoxide"
    val testMessageByteArray = testMessage.toByteArray(Charsets.UTF_8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diceKeysApi = DiceKeysApi.create(this)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text)
        buttonStart = findViewById(R.id.btn_start)



        buttonStart.setOnClickListener{
            // basicApi.ensureKeyLoaded()
            diceKeysApi.getSeed(
                keyDerivationOptionsJson,
                object: DiceKeysApi.GetSeedCallback {
                override fun onGetSeedSuccess(seed: ByteArray, originalIntent: Intent) {
                    resultTextView.text = "Seed=${Base64.encodeToString(seed, Base64.DEFAULT)}"
                    encryptSymmetric()
                }
            })
        }
    }

    fun encryptSymmetric() {
        diceKeysApi.sealWithSymmetricKey(
            keyDerivationOptionsJson,
            testMessageByteArray,
            object: DiceKeysApi.SealWithSymmetricKeyCallback{
                override fun onSealWithSymmetricKeySuccess(ciphertext: ByteArray, originalIntent: Intent) {
                    resultTextView.text = "${resultTextView.text}\nSymmetrically sealed message '${testMessage}' as ciphertext '${Base64.encodeToString(ciphertext, Base64.DEFAULT)}'"
                    decryptSymmetric(ciphertext)
                }

                override fun onSealWithSymmetricKeyFail(exception: Exception, originalIntent: Intent) {
                    resultTextView.text = "Symmetric seal failed:\n${exception.toString()}"
                }
        })
    }

    fun decryptSymmetric(ciphertext: ByteArray) {
        diceKeysApi.unsealWithSymmetricKey(
            keyDerivationOptionsJson,
            ciphertext,
            object : DiceKeysApi.UnsealWithSymmetricKeyCallback {
                override fun onUnsealSymmetricSuccess(plaintext: ByteArray, originalIntent: Intent) {
                    resultTextView.text =
                            "${resultTextView.text}\nUnsealed '${String(plaintext, Charsets.UTF_8)}'"
                    sign()
                }
                override fun onUnsealSymmetricFail(exception: Exception, originalIntent: Intent) {
                    resultTextView.text = "Symmetric unseal failed:\n${exception.toString()}"
                }
            })
    }

    fun sign() {
        diceKeysApi.generateSignature(keyDerivationOptionsJson, testMessageByteArray, object: DiceKeysApi.GenerateSignatureCallback {
            override fun onGenerateSignatureCallbackSuccess(signature: ByteArray, signatureVerificationKey: SignatureVerificationKey, originalIntent: Intent) {
                resultTextView.text =
                        "${resultTextView.text}\nSigned test message '${Base64.encodeToString(signature, Base64.DEFAULT)}'"
                verifySignature(testMessageByteArray, signature, signatureVerificationKey)
            }
            override fun onGenerateSignatureCallbackFail(exception: Exception, originalIntent: Intent) {
                resultTextView.text = "generateSignature failed:\n${exception.toString()}"
            }
        } )
    }

    fun verifySignature(message: ByteArray, signature: ByteArray, claimedSignatureVerificationKey: org.dicekeys.api.SignatureVerificationKey) {
        diceKeysApi.getSignatureVerificationKey(keyDerivationOptionsJson, object: DiceKeysApi.GetSignatureVerificationKeyCallback{
            override fun onGetSignatureVerificationKeySuccess(signatureVerificationKey: org.dicekeys.api.SignatureVerificationKey, originalIntent: Intent) {
                val keysMatch = signatureVerificationKey == claimedSignatureVerificationKey
                val verified = signatureVerificationKey.verifySignature(message, signature)
                resultTextView.text =
                        "${resultTextView.text}\nVerification key match=${keysMatch}, verification result=${verified}"
                encryptPublic()
            }
            override fun onGetSignatureVerificationKeyFail(exception: Exception, originalIntent: Intent) {
                resultTextView.text = "getSignatureVerificationKey failed:\n${exception.toString()}"
            }
        })
    }


    fun encryptPublic() {
        diceKeysApi.getPublicKey(
                keyDerivationOptionsJson,
                object: DiceKeysApi.GetPublicKeyCallback{
                    override fun onGetPublicKeySuccess(publicKey: org.dicekeys.api.PublicKey, originalIntent: Intent) {
                        val ciphertext = publicKey.seal(testMessageByteArray)
                        resultTextView.text = "${resultTextView.text}\ngetPublicKey publicKey='${publicKey.toJson()}' as ciphertext='${Base64.encodeToString(ciphertext, Base64.DEFAULT)}'"
                        decryptPrivate(ciphertext, publicKey)
                    }
                    override fun onGetPublicKeyFail(exception: Exception, originalIntent: Intent) {
                        resultTextView.text = "getPublicKey failed:\n${exception.toString()}"
                    }
                })
    }

    fun decryptPrivate(ciphertext: ByteArray, publicKey: org.dicekeys.api.PublicKey) {
        diceKeysApi.unsealWithPrivateKey(
                ciphertext,
                publicKey,
                object : DiceKeysApi.UnsealWithPrivateKeyCallback {
                    override fun onUnsealAsymmetricSuccess(plaintext: ByteArray, originalIntent: Intent) {
                        resultTextView.text =

                                "${resultTextView.text}\nUnsealed '${String(plaintext, Charsets.UTF_8)}'"
                    }
                    override fun onUnsealAsymmetricFail(exception: Exception, originalIntent: Intent) {
                        resultTextView.text = "unsealWithPrivateKey failed:\n${exception.toString()}"
                    }
                })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        diceKeysApi.handleOnActivityResult(data)
//    if (requestCode == RC_READ_KEYSQR && resultCode == Activity.RESULT_OK && data!=null) {
//        // After a DiceKey has been returned by the ReadKeySqrActivity,
//        // launch the DisplayDiceKey activity to display it
//        val keySqrAsJson: String? = data.getStringExtra("keySqrAsJson")
//        if (keySqrAsJson != null && keySqrAsJson != "null") {
//            val intent = Intent(this, DisplayDiceActivity::class.java)
//            intent.putExtra("keySqrAsJson", keySqrAsJson)
//            startActivityForResult(intent, RC_DISPLAY_DICE)
//        }
//    }
}


}
