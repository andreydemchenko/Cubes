package ru.turbopro.cubes.ui.main.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.turbopro.cubes.R
import ru.turbopro.cubes.databinding.FragmentProfileBinding
import ru.turbopro.cubes.viewmodels.ShopViewModel

class ProfileFragment : Fragment() {

	private lateinit var binding: FragmentProfileBinding
	private val viewModel: ShopViewModel by activityViewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentProfileBinding.inflate(layoutInflater)
		binding.profileTopAppBar.topAppBar.title = getString(R.string.account_profile_label)
		binding.profileTopAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.getUserData()
		setViews()
	}

	private fun setViews() {
		viewModel.userData.observe(viewLifecycleOwner) {
			if (it != null) {
				binding.profileNameTv.text = it.name
				binding.profileEmailTv.text = it.email
				binding.profileMobileTv.text = it.mobile
				val imgUrl: Uri = Uri.parse(it.userImageUrl)
				activity?.let { it1 ->
					Glide.with(it1)
						.asBitmap()
						.load(imgUrl.buildUpon().scheme("https").build())
						.into(binding.profileImageView)
				}
			}
		}
	}
}