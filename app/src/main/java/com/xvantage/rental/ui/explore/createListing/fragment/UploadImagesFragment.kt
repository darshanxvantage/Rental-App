package com.xvantage.rental.ui.explore.createListing.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.xvantage.rental.databinding.FragmentUploadImagesBinding
import com.xvantage.rental.ui.explore.common.ExploreConstants
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import com.xvantage.rental.ui.explore.createListing.adapter.SelectedImageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadImagesFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentUploadImagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()
    private lateinit var selectedImageAdapter: SelectedImageAdapter
    private var selectedImages: MutableList<Uri> = mutableListOf()

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ExploreConstants.MAX_IMAGES_PER_LISTING)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remainingSlots = ExploreConstants.MAX_IMAGES_PER_LISTING -
                    viewModel.formState.value.existingImageCount - selectedImages.size
            val toAdd = uris.take(remainingSlots.coerceAtLeast(0))
            if (toAdd.size < uris.size) {
                Toast.makeText(requireContext(), "Only added ${toAdd.size} - image limit reached", Toast.LENGTH_SHORT).show()
            }
            selectedImages.addAll(toAdd)
            selectedImageAdapter.setItems(selectedImages)
            updateCountHint()
        }
    }

    // owner's own KYC photo - separate from the gallery images above
    private var ownerAadharUri: Uri? = null
    private val pickOwnerAadharLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            ownerAadharUri = uri
            binding.ivOwnerAadharPreview.visibility = View.VISIBLE
            binding.ivOwnerAadharPreview.setImageURI(uri)
            binding.btnAddOwnerAadhar.text = "Change Aadhaar Photo"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedImages = viewModel.formState.value.newImageUris.toMutableList()

        selectedImageAdapter = SelectedImageAdapter { position ->
            if (position in selectedImages.indices) {
                selectedImages.removeAt(position)
                selectedImageAdapter.setItems(selectedImages)
                updateCountHint()
            }
        }

        binding.rvSelectedImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = selectedImageAdapter
        }
        selectedImageAdapter.setItems(selectedImages)
        updateCountHint()

        binding.btnAddPhotos.setOnClickListener {
            pickImagesLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // owner Aadhaar - single photo, separate from the gallery above
        ownerAadharUri = viewModel.formState.value.ownerAadharUri
        if (ownerAadharUri != null) {
            binding.ivOwnerAadharPreview.visibility = View.VISIBLE
            binding.ivOwnerAadharPreview.setImageURI(ownerAadharUri)
            binding.btnAddOwnerAadhar.text = "Change Aadhaar Photo"
        } else if (viewModel.formState.value.existingOwnerAadharImage != null) {
            // edit mode, already uploaded on a previous visit - just show the label, no local preview
            binding.btnAddOwnerAadhar.text = "Change Aadhaar Photo (already uploaded)"
        }
        binding.btnAddOwnerAadhar.setOnClickListener {
            pickOwnerAadharLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun updateCountHint() {
        val existingCount = viewModel.formState.value.existingImageCount
        val totalCount = existingCount + selectedImages.size
        binding.tvImageCountHint.text = if (existingCount > 0) {
            "$existingCount existing + ${selectedImages.size} new photo(s) • $totalCount/${ExploreConstants.MAX_IMAGES_PER_LISTING}"
        } else {
            "${selectedImages.size}/${ExploreConstants.MAX_IMAGES_PER_LISTING} photos added. The first photo is your cover image."
        }
    }

    override fun validateAndSave(): Boolean {
        val existingCount = viewModel.formState.value.existingImageCount
        if (selectedImages.isEmpty() && existingCount == 0) {
            Toast.makeText(requireContext(), "Please add at least one photo", Toast.LENGTH_SHORT).show()
            return false
        }
        viewModel.updateForm { copy(newImageUris = selectedImages.toList(), ownerAadharUri = ownerAadharUri) }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = UploadImagesFragment()
    }
}