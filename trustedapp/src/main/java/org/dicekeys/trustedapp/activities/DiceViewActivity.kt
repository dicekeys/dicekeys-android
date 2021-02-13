package org.dicekeys.trustedapp.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.dicekeys.trustedapp.view.DiceKeyView
import org.dicekeys.trustedapp.databinding.ActivityDiceViewBinding
import org.dicekeys.trustedapp.view.DiceOverlayView

class DiceViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiceViewBinding
    private lateinit var overlayView: DiceOverlayView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiceViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}