package ru.turbopro.cubes.ui.main.home.events

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import ru.turbopro.cubes.databinding.FragmentEventsBinding
import ru.turbopro.cubes.viewmodels.EventsHomeViewModel

private const val TAG = "EventsFragment"

class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private val viewModel: EventsHomeViewModel by activityViewModels()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(layoutInflater)
        setViews()
        setObservers()
        return binding.root
    }

    private fun setViews() {
        if (context != null) {
            setEventsAdapter(viewModel.allEvents.value)
            binding.eventsRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = eventAdapter
               /* val itemDecoration = RecyclerViewPaddingItemDecoration(requireContext())
                if (itemDecorationCount == 0) {
                    addItemDecoration(itemDecoration)
                }*/
            }
        }

        if (!viewModel.isUserASeller) {
            binding.eventsFabAddEvent.visibility = View.GONE
        }
        binding.eventsAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.eventsFabAddEvent.setOnClickListener {
            navigateToAddEditEventFragment(isEdit = false)
        }
        binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
        binding.loaderLayout.circularLoader.showAnimationBehavior
    }

    private fun setObservers() {
        viewModel.storeEventDataStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                StoreEventDataStatus.LOADING -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
                    binding.loaderLayout.circularLoader.showAnimationBehavior
                    binding.eventsRecyclerView.visibility = View.GONE
                }
                else -> {
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                    binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                }
            }
            if (status != null && status != StoreEventDataStatus.LOADING) {
                viewModel.allEvents.observe(viewLifecycleOwner) { eventsList ->
                    if (eventsList.isNotEmpty()) {
                        binding.loaderLayout.circularLoader.hideAnimationBehavior
                        binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                        binding.eventsRecyclerView.visibility = View.VISIBLE
                        binding.eventsRecyclerView.adapter?.apply {
                            eventAdapter.data = eventsList
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        viewModel.allEvents.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                viewModel.setDataLoaded()
            }
        }
    }

    private fun setEventsAdapter(eventsList: List<Event>?) {
        eventAdapter = EventAdapter(eventsList ?: emptyList(), requireContext())
        eventAdapter.onClickListener = object : EventAdapter.OnClickListener {
            override fun onClick(eventData: Event) {
                findNavController().navigate(
                    R.id.action_eventsFragment_to_eventDetailFragment,
                    bundleOf("eventId" to eventData.eventId)
                )
            }

            override fun onDeleteClick(eventData: Event) {
                Log.d(TAG, "onDeleteEvent: initiated for ${eventData.eventId}")
                showDeleteDialog(eventData.name, eventData.eventId)
            }

            override fun onEditClick(eventId: String) {
                Log.d(TAG, "onEditEvent: initiated for $eventId")
                navigateToAddEditEventFragment(isEdit = true, eventId = eventId)
            }
        }
    }

    private fun showDeleteDialog(eventName: String, eventId: String) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.delete_dialog_title_text))
                .setMessage(getString(R.string.delete_event_dialog_message_text, eventName))
                .setNegativeButton(getString(R.string.pro_cat_dialog_cancel_btn)) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(getString(R.string.delete_dialog_delete_btn_text)) { dialog, _ ->
                    viewModel.deleteEvent(eventId)
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun navigateToAddEditEventFragment(isEdit: Boolean, eventId: String? = null) {
        findNavController().navigate(
            R.id.action_eventsFragment_to_addEditEventFragment,
            bundleOf("isEdit" to isEdit, "eventId" to eventId)
        )
    }

    private fun getMixedDataList(data: List<Event>, adsList: List<Int>): List<Any> {
        val itemsList = mutableListOf<Any>()
        itemsList.addAll(data.sortedBy { it.date })
        /*var currPos = 0
        if (itemsList.size >= 4) {
            adsList.forEach label@{ ad ->
                if (itemsList.size > currPos + 1) {
                    itemsList.add(currPos, ad)
                } else {
                    return@label
                }
                currPos += 5
            }
        }*/
        return itemsList
    }

    /*private fun getAdsList(): List<Int> {
        return listOf(R.drawable.ad_ex_2, R.drawable.ad_ex_1, R.drawable.ad_ex_3)
    }*/
}