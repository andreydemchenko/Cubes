package ru.turbopro.cubes.ui.main.home.events

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import ru.turbopro.cubes.databinding.FragmentEventDetailBinding
import ru.turbopro.cubes.ui.DotsIndicatorDecoration
import ru.turbopro.cubes.ui.main.ImagesAdapter
import ru.turbopro.cubes.viewmodels.EventViewModel

class EventDetailFragment : Fragment() {

    inner class EventViewModelFactory(
        private val eventId: String,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(eventId, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    private lateinit var binding: FragmentEventDetailBinding
    private lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEventDetailBinding.inflate(layoutInflater)

        val eventId = arguments?.getString("eventId")

        if (activity != null && eventId != null) {
            val viewModelFactory = EventViewModelFactory(eventId, requireActivity().application)
            viewModel = ViewModelProvider(this, viewModelFactory).get(EventViewModel::class.java)
        }

        binding.loaderLayout.loaderFrameLayout.background =
            ResourcesCompat.getDrawable(resources, R.color.white, null)

        setObservers()

        return binding.root
    }

    private fun setObservers() {
        viewModel.eventDataStatus.observe(viewLifecycleOwner) {
            when (it) {
                StoreEventDataStatus.DONE -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                    binding.eventDetailsLayout.visibility = View.VISIBLE
                    setViews()
                }
                else -> {
                    binding.eventDetailsLayout.visibility = View.GONE
                    binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setViews() {
        binding.eventDetailsLayout.visibility = View.VISIBLE
        binding.eventDetailAppBar.topAppBar.title = viewModel.eventData.value?.name
        binding.eventDetailAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setImagesView()

        binding.eventDetailTitleTv.text = viewModel.eventData.value?.name ?: ""
        binding.eventDetailDateTv.text = viewModel.eventData.value?.date.toString()

        binding.eventDetailDescriptionTv.text = viewModel.eventData.value?.description ?: ""
    }

    private fun makeToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    private fun setImagesView() {
        if (context != null) {
            binding.eventDetailsImagesRecyclerview.isNestedScrollingEnabled = false
            val adapter = ImagesAdapter(
                requireContext(),
                viewModel.eventData.value?.images ?: emptyList()
            )
            binding.eventDetailsImagesRecyclerview.adapter = adapter
            val rad = resources.getDimension(R.dimen.radius)
            val dotsHeight = resources.getDimensionPixelSize(R.dimen.dots_height)
            val inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray)
            val activeColor = ContextCompat.getColor(requireContext(), R.color.blue_accent_300)
            val itemDecoration =
                DotsIndicatorDecoration(rad, rad * 4, dotsHeight, inactiveColor, activeColor)
            if (viewModel.getCountOfEventsList() > 1) binding.eventDetailsImagesRecyclerview.addItemDecoration(itemDecoration)
            PagerSnapHelper().attachToRecyclerView(binding.eventDetailsImagesRecyclerview)
        }
    }
}