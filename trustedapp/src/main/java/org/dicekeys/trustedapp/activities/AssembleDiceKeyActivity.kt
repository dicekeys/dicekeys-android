package org.dicekeys.trustedapp.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.dicekeys.trustedapp.R
import org.dicekeys.trustedapp.databinding.ActivityAssembleDiceKeyBinding

class AssembleDiceKeyActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAssembleDiceKeyBinding
    private lateinit var pagerAdapter: AssemblePagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssembleDiceKeyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        pagerAdapter = AssemblePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = pagerAdapter
    }

    override fun onClick(view: View?) {
        if (view == binding.btnNext) {
            binding.viewPager.currentItem = binding.viewPager.currentItem + 1
        } else if (view == binding.btnPrev) {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
        updateUIState()
    }

    private fun updateUIState() {
        binding.btnNext.isEnabled = binding.viewPager.currentItem < pagerAdapter.count - 1
        binding.btnPrev.isEnabled = binding.viewPager.currentItem > 0
    }

    private class AssemblePagerAdapter(fm: FragmentManager)
        : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 6

        override fun getItem(position: Int): Fragment {

            return when(position) {
                0 -> AssembleFragment(R.layout.fragment_assemble_step1)
                1 -> AssembleFragment(R.layout.fragment_assemble_step2)
                2 -> AssembleFragment(R.layout.fragment_assemble_step3)
                3 -> AssembleFragment(R.layout.fragment_assemble_step4)
                4 -> AssembleFragment(R.layout.fragment_assemble_step5)
                else -> AssembleFragment(R.layout.fragment_assemble_step6)
            }
        }
    }

    class AssembleFragment(@LayoutRes contentLayoutId: Int): Fragment(contentLayoutId) {

    }
}