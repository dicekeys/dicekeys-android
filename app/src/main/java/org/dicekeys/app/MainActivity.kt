package org.dicekeys.app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.Api


class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_READ_KEYSQR = 1
        const val RC_DISPLAY_DICE = 2
    }
    private lateinit var buttonStart: Button
    private lateinit var basicApi: org.dicekeys.ActivityApi
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basicApi = org.dicekeys.ActivityApi(this)
        setContentView(R.layout.activity_main)
        resultTextView = findViewById(R.id.result_text)
        buttonStart = findViewById(R.id.btn_start)

        buttonStart.setOnClickListener{
            // basicApi.ensureKeyLoaded()
            basicApi.getSeed("", object: Api.GetSeedCallback {
                override fun onGetSeedSuccess(seed: ByteArray, originalIntent: Intent) {
                    resultTextView.text = Base64.encodeToString(seed, Base64.DEFAULT)
                }
            })
        }
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
