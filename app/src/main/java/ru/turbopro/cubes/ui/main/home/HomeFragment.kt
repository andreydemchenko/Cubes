package ru.turbopro.cubes.ui.main.home

/*import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp*/
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.turbopro.cubes.R
import ru.turbopro.cubes.databinding.FragmentHomeBinding
import ru.turbopro.cubes.ui.main.home.qrscanner.QRScannerActivity
import ru.turbopro.cubes.viewmodels.HomeViewModel


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()

    var isImageFitToScreen = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserData()
        setViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        /*ComposeView(requireContext()).apply {
            setContent {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    circularProgressBar(percentage = 0.8f, number = 100)
                }
            }
        }*/

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
                binding.loginHomeTv.text = it.email
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

    /*@Composable
    fun circularProgressBar(
        percentage: Float,
        number: Int,
        fontSize: TextUnit = 28.sp,
        radius: Dp = 50.dp,
        color: Color = Color.Green,
        strokeWidth: Dp = 8.dp,
        animDuration: Int = 1000,
        animDelay: Int = 0
    ) {
        var animationPlayed by remember {
            mutableStateOf(false)
        }
        val curPercentage = animateFloatAsState(
            targetValue = if (animationPlayed) percentage else 0f,
            animationSpec = tween(
                    durationMillis = animDuration,
                    delayMillis = animDelay
            )
        )
        LaunchedEffect(key1 = true) {
            animationPlayed = true
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(radius * 2f)
        ) {
            Canvas (modifier = Modifier.size(radius * 2f)) {
                drawArc(
                    color = color,
                    -90f,
                    360 * curPercentage.value,
                    useCenter = false,
                    style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = (curPercentage.value * number).toInt().toString(),
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }

    }*/
}
