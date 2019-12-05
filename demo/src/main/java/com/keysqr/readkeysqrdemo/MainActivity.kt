package com.keysqr.readkeysqrdemo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import com.keysqr.readkeysqr.KeySqrDrawable
import com.keysqr.readkeysqr.ReadKeySqrActivity
import com.keysqr.readkeysqr.keySqrFromJsonFacesRead

class MainActivity : AppCompatActivity() {

    val RC_READ_KEYSQR = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            val intent = Intent(this, ReadKeySqrActivity::class.java)
            startActivityForResult(intent, RC_READ_KEYSQR)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_READ_KEYSQR && resultCode == Activity.RESULT_OK && data!=null) {
            val keySqrAsJson: String? = data.getStringExtra("keySqrAsJson")
            if (keySqrAsJson != null && keySqrAsJson != "null") {
                val keySqr = keySqrFromJsonFacesRead(keySqrAsJson)
                if (keySqr != null) {
                    val humanReadableForm: String = keySqr.toCanonicalRotation().toHumanReadableForm(true)
                    findViewById<TextView>(R.id.txt_json).text = humanReadableForm

                    val myDrawing = KeySqrDrawable(this, keySqr)
                    val image: ImageView = findViewById(R.id.keysqr_view)
                    image.setImageDrawable(myDrawing)
                    image.contentDescription = humanReadableForm

                    //val imageView = findViewById<KeySqrDrawable>(R.id.keysqr_canvas_container)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}
