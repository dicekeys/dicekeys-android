package org.dicekeys.trustedapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.dicekeys.trustedapp.DiceKeyView
import org.dicekeys.trustedapp.databinding.ActivityDiceViewBinding

class DiceViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiceViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiceViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}