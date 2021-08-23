package com.giftmusic.mugip.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import com.giftmusic.mugip.R

class UploadFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout =  inflater.inflate(R.layout.fragment_upload, container, false)
        val addTagButton = layout.findViewById<TextView>(R.id.add_tag_button)
        val addTagScrollView = layout.findViewById<ScrollView>(R.id.tag_scroll_view)

        addTagButton.setOnClickListener {
            when(addTagScrollView.visibility){
                View.VISIBLE -> addTagScrollView.visibility = View.INVISIBLE
                View.INVISIBLE -> addTagScrollView.visibility = View.VISIBLE
            }
        }
        return layout
    }

}