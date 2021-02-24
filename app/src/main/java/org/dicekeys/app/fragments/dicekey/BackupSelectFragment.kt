package org.dicekeys.app.fragments.dicekey

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.derivationRecipeTemplates
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.BackupSelectFragmentBinding
import org.dicekeys.app.viewmodels.DiceKeyViewModel

@AndroidEntryPoint
class BackupSelectFragment: AbstractDiceKeyFragment<BackupSelectFragmentBinding>(R.layout.backup_select_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        binding.wrapStickeys.setOnClickListener {
            navigate(BackupSelectFragmentDirections.actionBackupSelectToBackupFragment(viewModel.diceKey.value!!.keyId, true))
        }

        binding.wrapDiceKey.setOnClickListener {
            navigate(BackupSelectFragmentDirections.actionBackupSelectToBackupFragment(viewModel.diceKey.value!!.keyId, false))
        }
    }
}