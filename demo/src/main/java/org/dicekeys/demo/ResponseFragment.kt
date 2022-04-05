package org.dicekeys.demo

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import org.dicekeys.demo.databinding.FragmentResponseBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ResponseFragment : Fragment() {

    private val args: ResponseFragmentArgs by navArgs()

    lateinit var binding: FragmentResponseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentResponseBinding.inflate(inflater, container, false).also {
            binding = it
            binding.lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rawUrl = arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)?.data.toString()

        binding.requestId = args.requestId
        binding.exception = args.exception
        binding.message = args.message
        binding.passwordJson = args.passwordJson
        binding.secretJson = args.secretJson
        binding.sealingKeyJson = args.sealingKeyJson
        binding.unsealingKeyJson = args.unsealingKeyJson
        binding.symmetricKeyJson = args.symmetricKeyJson

        binding.packagedSealedMessageJson = args.packagedSealedMessageJson
        binding.base64urlDecode = args.plaintext?.let { String(Base64.decode(it, Base64.NO_WRAP)) }
    }

}