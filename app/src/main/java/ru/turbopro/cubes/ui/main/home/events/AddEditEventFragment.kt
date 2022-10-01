package ru.turbopro.cubes.ui.main.home.events

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.utils.AddEditEventErrors
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import ru.turbopro.cubes.databinding.FragmentAddEditEventBinding
import ru.turbopro.cubes.ui.AddEditEventViewErrors
import ru.turbopro.cubes.ui.MyOnFocusChangeListener
import ru.turbopro.cubes.ui.main.AddImagesAdapter
import ru.turbopro.cubes.viewmodels.AddEditEventViewModel
import kotlin.properties.Delegates

private const val TAG = "AddEditEventFragment"

class AddEditEventFragment : Fragment() {

    private lateinit var binding : FragmentAddEditEventBinding
    private val viewModel by viewModels<AddEditEventViewModel>()
    private val focusChangeListener = MyOnFocusChangeListener()

    // arguments
    private var isEdit by Delegates.notNull<Boolean>()
    private lateinit var eventId: String

    private var imgList = mutableListOf<Uri>()

    private val getImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            imgList.addAll(result)
            if (imgList.size > 3) {
                imgList = imgList.subList(0, 3)
                makeToast("Maximum 3 images are allowed!")
            }
            val adapter = context?.let { AddImagesAdapter(it, imgList) }
            binding.addEditEventImagesRv.adapter = adapter
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddEditEventBinding.inflate(layoutInflater)

        isEdit = arguments?.getBoolean("isEdit") == true
        eventId = arguments?.getString("eventId").toString()

        initViewModel()

        setViews()

        setObservers()

        return binding.root
    }

    private fun initViewModel() {
        Log.d(TAG, "init view model, isedit = $isEdit")

        viewModel.setIsEdit(isEdit)
        if (isEdit) {
            Log.d(TAG, "init view model, isedit = true, $eventId")
            viewModel.setEventData(eventId)
        }
    }

    private fun setObservers() {
        viewModel.errorStatus.observe(viewLifecycleOwner) { err ->
            modifyErrors(err)
        }
        viewModel.eventDataStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                StoreEventDataStatus.LOADING -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
                    binding.loaderLayout.circularLoader.showAnimationBehavior
                }
                StoreEventDataStatus.DONE -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                    fillDataInAllViews()
                }
                else -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                    makeToast("Error getting Data, Try Again!")
                }
            }
        }
        viewModel.addEditEventErrors.observe(viewLifecycleOwner) { status ->
            when (status) {
                AddEditEventErrors.ADDING -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.VISIBLE
                    binding.loaderLayout.circularLoader.showAnimationBehavior
                }
                AddEditEventErrors.ERR_ADD_IMG-> {
                    setAddEventErrors(getString(R.string.add_error_img_upload))
                }
                AddEditEventErrors.ERR_ADD -> {
                    setAddEventErrors(getString(R.string.add_event_insert_error))
                }
                AddEditEventErrors.NONE -> {
                    binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
                    binding.loaderLayout.circularLoader.hideAnimationBehavior
                }
            }
        }
    }

    private fun setAddEventErrors(errText: String) {
        binding.loaderLayout.loaderFrameLayout.visibility = View.GONE
        binding.loaderLayout.circularLoader.hideAnimationBehavior
        binding.addEditEventErrorTextView.visibility = View.VISIBLE
        binding.addEditEventErrorTextView.text = errText
    }

    private fun fillDataInAllViews() {
        viewModel.eventData.value?.let { event ->
            Log.d(TAG, "fill data in views")
            binding.addEditEventAppBar.topAppBar.title = "Edit Event - ${event.name}"
            binding.addEditEventNameEditText.setText(event.name)
            binding.addEditEventDateEditText.setText(event.date.toString())
            binding.addEditEventDescriptionEditText.setText(event.description)

            imgList = event.images.map { it.toUri() } as MutableList<Uri>
            val adapter = AddImagesAdapter(requireContext(), imgList)
            binding.addEditEventImagesRv.adapter = adapter

            binding.addEditEventBtn.setText(R.string.edit_product_btn_text)
        }

    }

    private fun setViews() {
        Log.d(TAG, "set views")

        if (!isEdit) {
            binding.addEditEventAppBar.topAppBar.title = "Add Event"

            val adapter = AddImagesAdapter(requireContext(), imgList)
            binding.addEditEventImagesRv.adapter = adapter
        }
        binding.addEditEventImagesBtn.setOnClickListener {
            getImages.launch("image/*")
        }

        binding.addEditEventAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.loaderLayout.loaderFrameLayout.visibility = View.GONE

        binding.addEditEventErrorTextView.visibility = View.GONE
        binding.addEditEventNameEditText.onFocusChangeListener = focusChangeListener
        binding.addEditEventDateEditText.onFocusChangeListener = focusChangeListener
        binding.addEditEventDescriptionEditText.onFocusChangeListener = focusChangeListener

        binding.addEditEventBtn.setOnClickListener {
            onAddEditEvent()
            if (viewModel.errorStatus.value == AddEditEventViewErrors.NONE) {
                viewModel.addEditEventErrors.observe(viewLifecycleOwner) { err ->
                    if (err == AddEditEventErrors.NONE) {
                        findNavController().navigate(R.id.action_addEditEventFragment_to_eventsFragment)
                    }
                }
            }
        }
    }

    private fun onAddEditEvent() {
        val name = binding.addEditEventNameEditText.text.toString()
        val date = binding.addEditEventDateEditText.text.toString()
        val desc = binding.addEditEventDescriptionEditText.text.toString()
        Log.d(TAG, "onAddEditEvent: Add/Edit event initiated, $name, $date, $desc, $imgList")
        viewModel.submitEvent(
            name, date, desc, imgList
        )
    }

    private fun modifyErrors(err: AddEditEventViewErrors) {
        when (err) {
            AddEditEventViewErrors.NONE -> binding.addEditEventErrorTextView.visibility = View.GONE
            AddEditEventViewErrors.EMPTY -> {
                binding.addEditEventErrorTextView.visibility = View.VISIBLE
                binding.addEditEventErrorTextView.text = getString(R.string.add_error_string)
            }
        }
    }

    private fun makeToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}