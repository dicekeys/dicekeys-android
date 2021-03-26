package org.dicekeys.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.dicekeys.api.ApiStrings
import org.dicekeys.api.UnsealingInstructions
import org.dicekeys.api.UserDeclinedToAuthorizeOperation
import org.dicekeys.app.R
import org.dicekeys.app.apicommands.permissionchecked.PermissionCheckedUrlCommands
import org.dicekeys.app.databinding.ApiRequestFragmentBinding
import org.dicekeys.app.encryption.EncryptedDiceKey
import org.dicekeys.app.extensions.clearNavigationResult
import org.dicekeys.app.extensions.getNavigationResult
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.viewmodels.ApiRequestViewModel
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.SimpleDiceKey

@AndroidEntryPoint
class ApiRequestFragment : AbstractListDiceKeysFragment<ApiRequestFragmentBinding>(R.layout.api_request_fragment, menuRes = 0) {

    private lateinit var urlCommand: PermissionCheckedUrlCommands

    private val viewModel: ApiRequestViewModel by viewModels()

    override val staticViewsCount: Int = 1

    override val linearLayoutContainer: LinearLayout
        get() = binding.container

    override fun clickOnDiceKey(view: View, encryptedDiceKey: EncryptedDiceKey) {
        val diceKey = diceKeyRepository.get(encryptedDiceKey.keyId)

        // Needs Decryption
        if(diceKey == null){
            biometricsHelper.decrypt(encryptedDiceKey, this) { diceKey ->
                setDiceKey(diceKey)
            }
        }else{
            setDiceKey(diceKey)
        }
    }

    override fun longClickOnDiceKey(view: View, encryptedDiceKey: EncryptedDiceKey) { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{
            val intent = requireArguments().getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)

            // Our API commands don't get a copy of the raw DiceKey seed, but only an accessor
            // which must be passed parameters to check.
            urlCommand = PermissionCheckedUrlCommands(
                    intent!!.data!!, ::loadDiceKeyAsync, ::requestUsersConsentAsync, requireActivity()
            )



        }catch (e: Exception){
            toast(e)
            findNavController().popBackStack()
        }

        getNavigationResult<String>(ScanFragment.READ_DICEKEY)?.observe(viewLifecycleOwner) { humanReadableOrNull ->
            humanReadableOrNull?.let { humanReadable ->
                clearNavigationResult(ScanFragment.READ_DICEKEY)
                setDiceKey(DiceKey.fromHumanReadableForm(humanReadable))
            }
        }

        binding.vm = viewModel

        // TODO implement recreate
        val create = getString(R.string.s_create)
        val host = urlCommand.getHost()?.let {
            when (it) {
                "apple.com" -> "Apple"
                "live.com", "microsoft.com" -> "Microsoft"
                "bitwarden.com" -> "BitWarden"
                "1password.com" -> "1Password"
                else -> it
            }
        }

        viewModel.title.value = when (urlCommand.command) {
            ApiStrings.Commands.getPassword -> {
                getString(R.string.may_use_password, host, create)
            }
            ApiStrings.Commands.getSecret -> {
                getString(R.string.may_use_secret_security_code, host, create)
            }
            ApiStrings.Commands.getUnsealingKey -> {
                getString(R.string.may_use_keys_encode_decode_secrets, host, create)
            }
            ApiStrings.Commands.getSymmetricKey -> {
                getString(R.string.may_use_key_encode_decode_secrets, host, create)
            }
            ApiStrings.Commands.sealWithSymmetricKey -> {
                getString(R.string.may_use_encode_secret, host)
            }
            ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                getString(R.string.may_use_decode_secret, host)
            }
            ApiStrings.Commands.getSigningKey -> {
                getString(R.string.may_use_keys_sign_data, create, host)
            }
            ApiStrings.Commands.generateSignature -> {
                getString(R.string.may_use_digital_signature, host)
            }
            ApiStrings.Commands.getSignatureVerificationKey -> {
                getString(R.string.may_use_key_verify, create, host)
            }
            ApiStrings.Commands.getSealingKey -> {
                getString(R.string.may_use_keys_store_secrets, create, host)
            }
            else -> ""
        }

        viewModel.approve.value = when (urlCommand.command) {
            ApiStrings.Commands.getPassword -> {
                getString(R.string.send_password)
            }
            ApiStrings.Commands.getSecret -> {
                getString(R.string.send_secret)
            }
            ApiStrings.Commands.getUnsealingKey, ApiStrings.Commands.getSealingKey, ApiStrings.Commands.getSigningKey -> {
                getString(R.string.send_keys)
            }
            ApiStrings.Commands.getSymmetricKey, ApiStrings.Commands.getSignatureVerificationKey -> {
                getString(R.string.send_key)
            }
            ApiStrings.Commands.sealWithSymmetricKey -> {
                getString(R.string.send_encoded_message)
            }
            ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                getString(R.string.send_encoded_message)
            }
            else -> getString(R.string.approve) // ApiStrings.Commands.generateSignature
        }

        viewModel.recipe.value = urlCommand.recipe

        binding.load.setOnClickListener {
            navigate(ListDiceKeysFragmentDirections.actionGlobalScanFragment(showEnterByHand = true))
        }

        binding.buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                urlCommand.send()
                findNavController().popBackStack()
            }
        }

        binding.buttonCancel.setOnClickListener {
            urlCommand.sendException(UserDeclinedToAuthorizeOperation("User Declined To Authorize Operation"))
            findNavController().popBackStack()
        }

    }

    private fun setDiceKey(diceKey: DiceKey<Face>){
        diceKeyRepository.set(diceKey)
        viewModel.diceKey.value = diceKey

        lifecycleScope.launchWhenCreated {
            urlCommand.executeCommand()

            if(urlCommand.hasException()){
                urlCommand.sendException()
                findNavController().popBackStack()
            }else{
                viewModel.createLabel.value = when (urlCommand.command) {
                    ApiStrings.Commands.getPassword -> {
                        getString(R.string.password_created)
                    }
                    ApiStrings.Commands.getSecret -> {
                        getString(R.string.secret_created)
                    }
                    ApiStrings.Commands.getUnsealingKey, ApiStrings.Commands.getSealingKey, ApiStrings.Commands.getSigningKey -> {
                        getString(R.string.key_created)
                    }
                    ApiStrings.Commands.getSymmetricKey, ApiStrings.Commands.getSignatureVerificationKey -> {
                        getString(R.string.key_created)
                    }
                    ApiStrings.Commands.sealWithSymmetricKey -> {
                        getString(R.string.message_to_seal)
                    }
                    ApiStrings.Commands.unsealWithSymmetricKey, ApiStrings.Commands.unsealWithUnsealingKey -> {
                        getString(R.string.key_created)
                    }
                    else -> getString(R.string.message_to_sign) // ApiStrings.Commands.generateSignature
                }

                viewModel.dataCreated.value = urlCommand.createdDataOrPlainText
            }
        }
    }

    private fun loadDiceKeyAsync(): Deferred<SimpleDiceKey> = (
                    CompletableDeferred<SimpleDiceKey>().also { completableDeferred ->
                        diceKeyRepository.getActiveDiceKey()?.also {
                            completableDeferred.complete(it)
                        } ?: run {
                            completableDeferred.completeExceptionally(Throwable("DiceKey not found in Repository"))
                        }
                    }
            )

    /**
     * Ask the user for consent to unseal a message if the UnsealingInstructions
     * encountered on an `unseal` operation require the user's consent
     */
    private fun requestUsersConsentAsync(
            requestForUsersConsent: UnsealingInstructions.RequestForUsersConsent
    ): Deferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse> =
            CompletableDeferred<UnsealingInstructions.RequestForUsersConsent.UsersResponse>().also {
                deferredDialogResult -> requireActivity().runOnUiThread {
                    MaterialAlertDialogBuilder(requireContext())
                            .setMessage(requestForUsersConsent.question)
                            .setCancelable(false)
                            .setPositiveButton(requestForUsersConsent.actionButtonLabels.allow) { _, _ ->
                                deferredDialogResult.complete(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Allow)
                            }
                            .setNegativeButton(requestForUsersConsent.actionButtonLabels.deny) { _, _ ->
                                deferredDialogResult.complete(UnsealingInstructions.RequestForUsersConsent.UsersResponse.Deny)
                            }
                            .show()
                }
            }
}

