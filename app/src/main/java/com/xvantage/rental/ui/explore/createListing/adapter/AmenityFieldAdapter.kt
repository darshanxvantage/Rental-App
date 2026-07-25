package com.xvantage.rental.ui.explore.createListing.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.xvantage.rental.databinding.ItemAmenityCheckboxBinding
import com.xvantage.rental.network.response.explore.ExploreCategoryFieldResponse

/**
 * Renders the category's dynamic fields (explore_category_fields) - the
 * fields that only apply to a specific category (e.g. "Shop Size" for
 * Commercial, "Furnishing Type" for Apartment).
 *
 * `initialValues` lets this pre-fill when editing an existing listing.
 * Call [getFieldValues] any time (e.g. from validateAndSave()) to read the
 * current answers as { category_field_fk -> value }.
 */
class AmenityFieldAdapter(
    private val fields: List<ExploreCategoryFieldResponse>,
    initialValues: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<AmenityFieldAdapter.FieldViewHolder>() {

    private val currentValues: MutableMap<String, String> = initialValues.toMutableMap()

    fun getFieldValues(): Map<String, String> = currentValues.toMap()

    inner class FieldViewHolder(private val binding: ItemAmenityCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(field: ExploreCategoryFieldResponse) {
            binding.rowBooleanField.visibility = View.GONE
            binding.layoutTextField.visibility = View.GONE
            binding.rowChoiceField.visibility = View.GONE

            val existingValue = currentValues[field.id]

            when (field.fieldType) {
                "boolean" -> {
                    binding.rowBooleanField.visibility = View.VISIBLE
                    binding.tvFieldLabelBoolean.text = field.label
                    binding.switchField.setOnCheckedChangeListener(null)
                    binding.switchField.isChecked = existingValue == "true"
                    binding.switchField.setOnCheckedChangeListener { _, isChecked ->
                        currentValues[field.id] = isChecked.toString()
                    }
                }

                "select", "multiselect" -> {
                    binding.rowChoiceField.visibility = View.VISIBLE
                    binding.tvFieldLabelChoice.text = field.label
                    binding.chipGroupField.removeAllViews()
                    binding.chipGroupField.isSingleSelection = field.fieldType == "select"

                    val selectedOptions = existingValue?.split(",")?.map { it.trim() }?.toSet() ?: emptySet()

                    (field.options ?: emptyList()).forEach { option ->
                        val chip = Chip(binding.root.context).apply {
                            text = option
                            isCheckable = true
                            isChecked = selectedOptions.contains(option)
                            setOnClickListener {
                                val selectedChips = (0 until binding.chipGroupField.childCount)
                                    .map { binding.chipGroupField.getChildAt(it) as Chip }
                                    .filter { it.isChecked }
                                    .map { it.text.toString() }
                                currentValues[field.id] = selectedChips.joinToString(",")
                            }
                        }
                        binding.chipGroupField.addView(chip)
                    }
                }

                else -> { // text | number | textarea
                    binding.layoutTextField.visibility = View.VISIBLE
                    binding.layoutTextField.hint = field.label
                    binding.etField.setText(existingValue ?: "")
                    binding.etField.inputType = if (field.fieldType == "number") {
                        android.text.InputType.TYPE_CLASS_NUMBER
                    } else {
                        android.text.InputType.TYPE_CLASS_TEXT or
                                if (field.fieldType == "textarea") android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0
                    }
                    binding.etField.doAfterTextChangedSafe { text ->
                        currentValues[field.id] = text
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val binding = ItemAmenityCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        holder.bind(fields[position])
    }

    override fun getItemCount(): Int = fields.size
}

/** Small local helper - avoids pulling in androidx.core ktx's doOnTextChanged just for this one call site. */
private fun com.google.android.material.textfield.TextInputEditText.doAfterTextChangedSafe(onChanged: (String) -> Unit) {
    this.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            onChanged(s?.toString().orEmpty())
        }
    })
}