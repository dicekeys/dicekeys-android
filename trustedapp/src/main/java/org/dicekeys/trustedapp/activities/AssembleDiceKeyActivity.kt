package org.dicekeys.trustedapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.ReadDiceKeyActivity
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityAssembleDiceKeyBinding
import org.dicekeys.trustedapp.state.DiceKeyState

class AssembleDiceKeyActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {
    private var diceKey: DiceKey<Face> = DiceKey.example
    private lateinit var binding: ActivityAssembleDiceKeyBinding
    private lateinit var pagerAdapter: AssemblePagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssembleDiceKeyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pagerAdapter = AssemblePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.addOnPageChangeListener(this)
        binding.progressBar.max = pagerAdapter.count - 1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private class AssemblePagerAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 7

        override fun getItem(position: Int): Fragment {

            return when(position) {
                0 -> AssembleFragment(R.layout.fragment_assemble_step1)
                1 -> AssembleFragment(R.layout.fragment_assemble_step2)
                2 -> AssembleFragment(R.layout.fragment_assemble_step3)
                3 -> AssembleFragment(R.layout.fragment_assemble_step4)
                4 -> AssembleFragment(R.layout.fragment_assemble_step5)
                5 -> AssembleFragment(R.layout.fragment_assemble_step6)
                6 -> AssembleFragment(R.layout.fragment_assemble_step7)
                else -> AssembleFragment(R.layout.fragment_assemble_step1)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        binding.progressBar.progress = position
        binding.textWarning.visibility = if (position in listOf(0, 5, 6)) View.INVISIBLE else View.VISIBLE
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
                resultCode == Activity.RESULT_OK &&
                data != null &&
                data.hasExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)
        ) {
            data.getStringExtra(ReadDiceKeyActivity.Companion.Parameters.Response.diceKeyAsJson)?.let { diceKeyAsJson ->
                FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                    this.diceKey = DiceKey(faces = diceKey.faces.map {
                        Face(letter = it.letter, digit = it.digit, orientationAsLowercaseLetterTrbl = it.orientationAsLowercaseLetterTrbl)
                    })
                }
            }
        }
    }

    fun onSkipStep(view: View?) {
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    fun onScan(view: View?) {
        val intent = Intent(this, ReadDiceKeyActivity::class.java)
        startActivityForResult(intent, 0)
    }

    fun onUseStickeysKit(view: View?) {
        BackupActivity.startBackupWithStickeys(this, 0, diceKey)
    }

    fun onUseDiceKeyKit(view: View?) {
        BackupActivity.startBackupWithDiceKit(this, 0, diceKey)
    }

    class AssembleFragment(@LayoutRes contentLayoutId: Int): Fragment(contentLayoutId) {

    }
}