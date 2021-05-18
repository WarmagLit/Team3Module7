package com.example.graphiceditor.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.graphiceditor.R
import com.example.graphiceditor.SplashScreenActivity
import kotlinx.android.synthetic.main.fragment_other.*


class otherFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonFace.setOnClickListener {
            val intent = Intent(getActivity(), SplashScreenActivity::class.java)
            startActivity(intent)
        }
        buttonCube.setOnClickListener {
            val intent = Intent(getActivity(), SplashScreenActivity::class.java)
            startActivity(intent)
        }
    }


}