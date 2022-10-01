package ru.turbopro.cubes.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.turbopro.cubes.R
import ru.turbopro.cubes.databinding.FragmentAccountBinding
import ru.turbopro.cubes.ui.login.LoginActivity
import ru.turbopro.cubes.viewmodels.ShopViewModel

private const val TAG = "AccountFragment"

class AccountFragment : Fragment() {

	private lateinit var binding: FragmentAccountBinding
	private val viewModel: ShopViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentAccountBinding.inflate(layoutInflater)

		setViews()
		return binding.root
	}

	private fun setViews() {
		binding.accountTopAppBar.topAppBar.title = getString(R.string.account_fragment_title)
		binding.accountProfileTv.setOnClickListener {
			Log.d(TAG, "Profile Selected")
			findNavController().navigate(R.id.action_accountFragment_to_profileFragment)
		}
		binding.accountOrdersTv.setOnClickListener {
			Log.d(TAG, "Orders Selected")
			findNavController().navigate(R.id.action_accountFragment_to_ordersFragment)
		}
		binding.accountAddressTv.setOnClickListener {
			Log.d(TAG, "Address Selected")
			findNavController().navigate(R.id.action_accountFragment_to_addressFragment)
		}
		binding.accountSignOutTv.setOnClickListener {
			Log.d(TAG, "Sign Out Selected")
			showSignOutDialog()
		}
	}

	private fun showSignOutDialog() {
		context?.let {
			MaterialAlertDialogBuilder(it)
				.setTitle(getString(R.string.sign_out_dialog_title_text))
				.setMessage(getString(R.string.sign_out_dialog_message_text))
				.setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
					dialog.cancel()
				}
				.setPositiveButton(getString(R.string.dialog_sign_out_btn_text)) { dialog, _ ->
					viewModel.signOut()
					navigateToSignUpActivity()
					dialog.cancel()
				}
				.show()
		}
	}

	private fun navigateToSignUpActivity() {
		val homeIntent = Intent(context, LoginActivity::class.java)
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context?.startActivity(homeIntent)
		requireActivity().finish()
	}
}