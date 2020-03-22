package org.dicekeys.app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.Api
import org.dicekeys.keys.PublicKey
import org.dicekeys.keys.SignatureVerificationKey


class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_KEYSQR = 1
        const val RC_DISPLAY_DICE = 2
    }
    private lateinit var buttonStart: Button
    private lateinit var basicApi: org.dicekeys.Api
    private lateinit var resultTextView: TextView
    val keyDerivationOptionsJson = "{}"
    val testMessage = "The secret ingredient is dihydrogen monoxide"
    val testMessageByteArray = testMessage.toByteArray(Charsets.UTF_8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basicApi = org.dicekeys.Api.create(this)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text)
        buttonStart = findViewById(R.id.btn_start)



        buttonStart.setOnClickListener{
            // basicApi.ensureKeyLoaded()
            basicApi.getSeed(
                keyDerivationOptionsJson,
                object: Api.GetSeedCallback {
                override fun onGetSeedSuccess(seed: ByteArray, originalIntent: Intent) {
                    resultTextView.text = "Seed=${Base64.encodeToString(seed, Base64.DEFAULT)}"
                    encryptSymmetric()
                }
            })
        }
    }

    fun encryptSymmetric() {
        basicApi.sealWithSymmetricKey(
            keyDerivationOptionsJson,
            testMessageByteArray,
            object: Api.SealWithSymmetricKeyCallback{
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
        basicApi.unsealWithSymmetricKey(
            keyDerivationOptionsJson,
            ciphertext,
            object : Api.UnsealWithSymmetricKeyCallback {
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
        basicApi.generateSignature(keyDerivationOptionsJson, testMessageByteArray, object: Api.GenerateSignatureCallback {
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

    fun verifySignature(message: ByteArray, signature: ByteArray, claimedSignatureVerificationKey: SignatureVerificationKey) {
        basicApi.getSignatureVerificationKey(keyDerivationOptionsJson, object: Api.GetSignatureVerificationKeyCallback{
            override fun onGetSignatureVerificationKeySuccess(signatureVerificationKey: SignatureVerificationKey, originalIntent: Intent) {
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
        basicApi.getPublicKey(
                keyDerivationOptionsJson,
                object: Api.GetPublicKeyCallback{
                    override fun onGetPublicKeySuccess(publicKey: PublicKey, originalIntent: Intent) {
                        val ciphertext = publicKey.seal(testMessageByteArray)
                        resultTextView.text = "${resultTextView.text}\ngetPublicKey publicKey='${publicKey.toJson()}' as ciphertext='${Base64.encodeToString(ciphertext, Base64.DEFAULT)}'"
                        decryptPrivate(ciphertext, publicKey)
                    }
                    override fun onGetPublicKeyFail(exception: Exception, originalIntent: Intent) {
                        resultTextView.text = "getPublicKey failed:\n${exception.toString()}"
                    }
                })
    }

    fun decryptPrivate(ciphertext: ByteArray, publicKey: PublicKey) {
        basicApi.unsealWithPrivateKey(
                ciphertext,
                publicKey,
                object : Api.UnsealWithPrivateKeyCallback {
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
        basicApi.handleOnActivityResult(data)
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
