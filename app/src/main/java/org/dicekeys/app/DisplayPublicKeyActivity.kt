package org.dicekeys.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable

class DisplayPublicKeyActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var backButton: Button
    private lateinit var textView: TextView
    private var publicKeyJson: String? = null

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
        publicKeyJson = intent.extras?.getString("publicKeyJson")
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        publicKeyJson?.let{
            try {
                val publicKeyJson = it
                image.contentDescription = publicKeyJson
                textView.text = publicKeyJson
                org.dicekeys.keys.PublicKey.fromJson(publicKeyJson)?.let { publicKey ->
                    image.setImageDrawable(publicKey.getJsonQrCode().toDrawable(resources))
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
        }
    }



}
