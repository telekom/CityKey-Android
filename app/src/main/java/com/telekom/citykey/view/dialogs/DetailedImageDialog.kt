package com.telekom.citykey.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DetailedImageDialogBinding
import com.telekom.citykey.utils.extensions.loadFromURL
import com.telekom.citykey.utils.extensions.viewBinding

class DetailedImageDialog : DialogFragment() {

    private val args: DetailedImageDialogArgs by navArgs()
    private val binding by viewBinding(DetailedImageDialogBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.BlackTransparentDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.detailed_image_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.eventDetailedImageView.loadFromURL(args.imageUrl)

        binding.eventDetailedImageCredit.text = args.imageCredit

        binding.eventDetailedImageClose.setOnClickListener {
            dismiss()
        }
    }
}
