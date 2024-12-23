package com.telekom.citykey.view.services

import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.ProgressButton
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.content.ServiceAction
import com.telekom.citykey.utils.extensions.dpToPixel

class DetailedServiceButtonsAdapter(
    private val actions: List<ServiceAction>,
    private val clickListener: (ServiceAction) -> Unit
) :
    RecyclerView.Adapter<DetailedServiceButtonsAdapter.ButtonViewHolder>() {

    companion object {
        private const val ACTION_DESIGN_FILLED = 1
        private const val ACTION_DESIGN_OUTLINE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder =
        ButtonViewHolder(
            ProgressButton(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val horizontalMargins = resources.getDimension(R.dimen.global_content_margins).toInt()
                    val verticalMargins = 15.dpToPixel(context)

                    clipChildren = false
                    setMargins(
                        horizontalMargins,
                        verticalMargins,
                        horizontalMargins,
                        verticalMargins
                    )
                    gravity = Gravity.CENTER
                }
            }
        )

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount() = actions.size

    inner class ButtonViewHolder(private val view: ProgressButton) : RecyclerView.ViewHolder(view) {

        fun bind(action: ServiceAction) {
            view.text = action.visibleText
            if (action.buttonDesign == ACTION_DESIGN_OUTLINE) {
                view.setupOutlineStyle(CityInteractor.cityColorInt)
            } else {
                view.setupNormalStyle(CityInteractor.cityColorInt)
            }

            view.setOnClickListener {
                clickListener(action)
            }
        }
    }
}
