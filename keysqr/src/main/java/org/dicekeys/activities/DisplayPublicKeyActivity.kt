package org.dicekeys.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import org.dicekeys.KeySqr
import org.dicekeys.FaceRead
import org.dicekeys.R.id
import org.dicekeys.state.KeySqrState


class DisplayPublicKeyActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var backButton: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(org.dicekeys.R.layout.activity_display_public_key)
        image = findViewById(id.image_view_public_key_qr_code)
        backButton = findViewById(id.btn_back)
        textView = findViewById(id.text_view_json_public_key)

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
                val publicKey = it.getPublicKey(
                        ""//""""{"keyType":"Public"}"""
                )
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
