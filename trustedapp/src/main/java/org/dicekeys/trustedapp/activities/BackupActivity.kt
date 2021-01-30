package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.R

class BackupActivity : AppCompatActivity() {
    companion object {
        val EXTRA_DICEKEY_KEY = "dicekey"
        val EXTRA_USE_STICKEYS_KEY = "use_stickeys"

        fun startBackupWithStickeys(context: Activity, requestCode: Int, diceKey: DiceKey<Face>) {
            val intent = Intent(context, BackupActivity::class.java)
            intent.putExtra(EXTRA_DICEKEY_KEY, diceKey.toHumanReadableForm())
            intent.putExtra(EXTRA_USE_STICKEYS_KEY, true)
            context.startActivityForResult(intent, requestCode)
        }

        fun startBackupWithDiceKit(context: Activity, requestCode: Int, diceKey: DiceKey<Face>) {
            val intent = Intent(context, BackupActivity::class.java)
            intent.putExtra(EXTRA_DICEKEY_KEY, diceKey.toHumanReadableForm())
            intent.putExtra(EXTRA_USE_STICKEYS_KEY, false)
            context.startActivityForResult(intent, requestCode)
        }
    }

    var diceKey: DiceKey<Face> = DiceKey.example
    var useStickeys: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        if (intent.hasExtra(EXTRA_DICEKEY_KEY)) {
            diceKey = DiceKey.fromHumanReadableForm(intent.getStringExtra(EXTRA_DICEKEY_KEY)!!)
        }
        if (intent.hasExtra(EXTRA_USE_STICKEYS_KEY)) {
            useStickeys = intent.getBooleanExtra(EXTRA_USE_STICKEYS_KEY, false)
        }
    }
}