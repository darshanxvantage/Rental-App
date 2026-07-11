package com.xvantage.rental.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.xvantage.rental.R

class AppSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(
    context,
    android.R.layout.simple_spinner_item,
    items
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = super.getView(position, convertView, parent) as TextView

        view.text = items[position]

        view.textSize = 16f

        view.setPadding(
            32,
            0,
            32,
            0
        )

        view.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.text_dark_blue
            )
        )

        return view
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = super.getDropDownView(
            position,
            convertView,
            parent
        ) as TextView

        view.text = items[position]

        view.textSize = 16f

        view.setPadding(
            40,
            34,
            40,
            34
        )

        view.setBackgroundColor(
            ContextCompat.getColor(
                context,
                android.R.color.transparent
            )
        )

        view.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.text_dark_blue
            )
        )

        return view
    }

}