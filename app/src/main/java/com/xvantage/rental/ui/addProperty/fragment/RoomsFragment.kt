package com.xvantage.rental.ui.addProperty.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.xvantage.rental.databinding.FragmentRoomsBinding
import com.xvantage.rental.network.response.PropertyDetailsData
import com.xvantage.rental.ui.addProperty.PropertyDetailsViewModel
import com.xvantage.rental.ui.addProperty.adapter.RoomAdapter
import com.xvantage.rental.ui.addProperty.tempFiles.Room
import kotlinx.coroutines.launch

class RoomsFragment : Fragment() {

    private var _binding: FragmentRoomsBinding? = null
    private val binding get() = _binding!!

    private lateinit var roomAdapter: RoomAdapter
    private var propertyId: String = ""

    // Keep current room list so we can reload after edit/delete
    private var currentPropertyData: PropertyDetailsData? = null

    private val viewModel: PropertyDetailsViewModel by activityViewModels()

    companion object {
        private const val ARG_PROPERTY_ID = "propertyId"

        fun newInstance(propertyId: String): RoomsFragment {
            val fragment = RoomsFragment()
            val args = Bundle()
            args.putString(ARG_PROPERTY_ID, propertyId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getString(ARG_PROPERTY_ID, "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeRoomActions()

        // Observe ViewModel state directly — more reliable than
        // fragmentResult which can fire before ViewPager is ready
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state is PropertyDetailsViewModel.State.Success) {
                    val property = state.details.data ?: return@collect
                    currentPropertyData = property
                    displayRooms(property)
                }
            }
        }

        // Also listen for explicit result (e.g. after room added)
        parentFragmentManager.setFragmentResultListener(
            "property_details",
            viewLifecycleOwner
        ) { _, bundle ->
            val json = bundle.getString("property_json") ?: return@setFragmentResultListener
            val property = Gson().fromJson(json, PropertyDetailsData::class.java)
            currentPropertyData = property
            displayRooms(property)
        }
    }

    private fun displayRooms(property: PropertyDetailsData) {
        val roomList = property.rooms.map {
            Room(
                id = it.id,
                number = it.room_no,
                type = property.propertyType ?: "",
                rent = it.rent,
                isOccupied = it.status.equals("OCCUPED", true)
            )
        }
        showEmptyState(roomList.isEmpty())
        roomAdapter.submitList(roomList)
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(
            onEditClicked = { room -> showEditRoomDialog(room) },
            onDeleteClicked = { room -> showDeleteConfirmDialog(room) }
        )
        binding.rvRooms.adapter = roomAdapter
    }

    private fun observeRoomActions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.roomDeleted.collect { deleted ->
                if (deleted) {
                    Toast.makeText(requireContext(), "Room deleted successfully", Toast.LENGTH_SHORT).show()
                    viewModel.loadPropertyDetails(propertyId)
                    viewModel.resetRoomStates()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.roomEdited.collect { edited ->
                if (edited) {
                    Toast.makeText(requireContext(), "Room updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.loadPropertyDetails(propertyId)
                    viewModel.resetRoomStates()
                }
            }
        }
    }

    private fun showEditRoomDialog(room: Room) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.activity_list_item, null)

        // Build custom dialog with EditTexts
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val etRoomNo = EditText(requireContext()).apply {
            hint = "Room Number"
            setText(room.number)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val etRent = EditText(requireContext()).apply {
            hint = "Monthly Rent (₹)"
            setText(room.rent.toInt().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(android.widget.TextView(requireContext()).apply {
            text = "Room Number"
            setTextColor(android.graphics.Color.parseColor("#1565C0"))
            textSize = 13f
        })
        layout.addView(etRoomNo)

        layout.addView(android.widget.TextView(requireContext()).apply {
            text = "Monthly Rent (₹)"
            setTextColor(android.graphics.Color.parseColor("#1565C0"))
            textSize = 13f
            setPadding(0, 16, 0, 0)
        })
        layout.addView(etRent)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Room ${room.number}")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newRoomNo = etRoomNo.text.toString().trim()
                val newRent = etRent.text.toString().trim()

                if (newRoomNo.isEmpty()) {
                    Toast.makeText(requireContext(), "Room number required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newRent.isEmpty()) {
                    Toast.makeText(requireContext(), "Rent amount required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.editRoom(room.id, newRoomNo, newRent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(room: Room) {
        if (room.isOccupied) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cannot Delete")
                .setMessage("Room ${room.number} is currently occupied. Please remove the tenant before deleting this room.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Room ${room.number}?")
            .setMessage("This will permanently delete Room ${room.number}. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRoom(room.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvRooms.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
