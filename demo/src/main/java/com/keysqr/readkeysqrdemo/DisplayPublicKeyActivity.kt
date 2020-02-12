package com.keysqr.readkeysqrdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.keysqr.FaceRead
import com.keysqr.KeySqr
import kotlinx.android.synthetic.main.activity_display_dice.*


class DisplayPublicKeyActivity : AppCompatActivity() {
    private var keySqr: KeySqr<FaceRead>? = null
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
            setResult(RESULT_OK, intent)
            // FIXME
            // determine if we need to wait for render to complete
            // or if android can handle this safely
            finish()
        }
        val keySqrAsJson = intent.extras?.getString("keySqrAsJson")

        if (keySqrAsJson != null && keySqrAsJson != "null") {
            keySqr = FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)
        }


        render()
    }

    private fun render() {
        keySqr?.let{ Thread(Runnable {
            try {
                val publicKey = it.getPublicKey(
                        """{"keyType":"Public"}"""
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
