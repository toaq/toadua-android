package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import town.robin.toadua.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {
    private lateinit var binding: FragmentAuthBinding

    private enum class AuthType { SIGN_IN, CREATE_ACCOUNT }
    private var authType = AuthType.SIGN_IN

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.auth_to_search)
        }
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.auth_to_search)
        }

        binding.createAccountButton.setOnClickListener {
            when(authType) {
                AuthType.SIGN_IN -> {
                    authType = AuthType.CREATE_ACCOUNT
                    binding.authTitle.text = getString(R.string.create_account)
                    binding.createAccountButton.text = getString(R.string.use_existing_account)
                }
                AuthType.CREATE_ACCOUNT -> {
                    authType = AuthType.SIGN_IN
                    binding.authTitle.text = getString(R.string.sign_in)
                    binding.createAccountButton.text = getString(R.string.create_account)
                }
            }
        }

        return binding.root
    }
}