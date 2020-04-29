package org.dicekeys.trustedapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import org.dicekeys.crypto.seeded.UnsealingKey
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.state.KeySqrState


class DisplayPublicKeyActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var backButton: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_public_key)
        image = findViewById(R.id.image_view_public_key_qr_code)
        backButton = findViewById(R.id.btn_back)
        textView = findViewById(R.id.text_view_json_public_key)

        backButton.setOnClickListener{
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

        private fun render() {
        KeySqrState.keySqr?.let{ Thread(Runnable {
            try {
                val publicKey = UnsealingKey.deriveFromSeed(
                        it.toKeySeed(false),
                        ""//""""{"keyType":"Public"}"""
                ).getPublicKey()
                val publicKeyJson = publicKey.toJson()
                runOnUiThread {
                    image.setImageDrawable(publicKey.getJsonQrCode().toDrawable(resources))
                    image.contentDescription = publicKeyJson
                    textView.text = publicKeyJson
                }
            } catch (e: Exception) {
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                e.printStackTrace(pw)
                val stackTrace: String = sw.toString()
                android.util.Log.e("We caught exception", stackTrace)
                runOnUiThread {
                    textView.text = stackTrace
                    textView.text = stackTrace
                }
            }
        }).run()}
    }



}
