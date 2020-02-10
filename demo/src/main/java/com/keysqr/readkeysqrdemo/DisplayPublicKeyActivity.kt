package com.keysqr.readkeysqrdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.keysqr.FaceRead


class DisplayPublicKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_public_key)

        findViewById<Button>(R.id.btn_back).setOnClickListener{
            var intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
        render()
    }

    private fun render() {
        val alt = intent.getStringExtra("keySqrAsJson")
        val keySqrAsJson = intent.extras.getString("keySqrAsJson")

        if (keySqrAsJson == null || keySqrAsJson == "null") {
            return
        }
        try {
            val keySqr = FaceRead.keySqrFromJsonFacesRead(keySqrAsJson)
            if (keySqr == null) {
                return
            }
            val image: ImageView = findViewById(R.id.image_view_public_key_qr_code)
            val publicKey = keySqr.getPublicKey(
                    """{"keyType":"Public"}"""
            )
            image.setImageDrawable(publicKey.getJsonQrCode().toDrawable(resources))
            image.contentDescription = publicKey.toJson()
            findViewById<TextView>(R.id.text_view_json_public_key).text = publicKey.toJson()

            //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
        } catch (e: Exception) {
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTrace: String = sw.toString()
            android.util.Log.e("We caught exception", stackTrace)
            findViewById<TextView>(R.id.text_view_json_public_key).text = stackTrace
        }

    }

}
