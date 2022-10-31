package ru.turbopro.cubes.ui.main.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.utils.extensions.cutEmailFromNickname
import ru.turbopro.cubes.databinding.FragmentHomeBinding
import ru.turbopro.cubes.ui.main.home.qrscanner.QRScannerActivity
import ru.turbopro.cubes.viewmodels.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()

    var isImageFitToScreen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        //viewModel.getUserData()
        setViews()

        binding.qrcodeHomeCardview.setOnClickListener{
            val intent = Intent (activity, QRScannerActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.settingsHomeImgBtn.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        binding.eventsHomeCardview.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_eventsFragment)
        }

        binding.editHomeImgBtn.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.ordersHomeCardview.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_ordersFragment)
        }

        binding.addressesHomeCardview.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_addressFragment)
        }

        binding.pointsHomeTv.setOnClickListener {
            it.animate().scaleX(1.4f).scaleY(1.4f).duration = 500
            it.animate().scaleX(1f).scaleY(1f).duration = 500
        }

        binding.profileImageHome.setOnClickListener{
            /*if (isImageFitToScreen) {
                isImageFitToScreen = false
                it.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                //it.setAdjustViewBounds(true)
            } else {
                isImageFitToScreen = true
                it.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                //it.setScaleType(ImageView.ScaleType.FIT_XY)
            }*/
        }

        return binding.root
    }

    private fun setViews() {
        viewModel.userData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.nameHomeTv.text = it.name
                binding.loginHomeTv.text = it.email.cutEmailFromNickname()
                binding.pointsHomeTv.text = it.points.toString()
                val imgUrl: Uri = Uri.parse(it.userImageUrl)
                activity?.let { it1 ->
                    Glide.with(it1)
                        .asBitmap()
                        .load(imgUrl.buildUpon().scheme("https").build())
                        .into(binding.profileImageHome)
                }
            }
        }
    }
}
