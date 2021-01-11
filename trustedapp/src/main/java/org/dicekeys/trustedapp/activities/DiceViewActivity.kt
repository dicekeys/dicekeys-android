package org.dicekeys.trustedapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.dicekeys.dicekey.Face
import org.dicekeys.trustedapp.DieFaceView
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityDiceViewBinding
import org.dicekeys.trustedapp.databinding.ActivityMainBinding

class DiceViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiceViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiceViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val dieFaceView = DieFaceView(this)
        dieFaceView.face = Face('A', '3')
        dieFaceView.dieSize = 300F
        binding.content.addView(dieFaceView)
    }
}