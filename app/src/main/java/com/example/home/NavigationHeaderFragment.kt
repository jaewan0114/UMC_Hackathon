package com.example.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.home.databinding.FragmentHomeBinding
import com.example.home.databinding.FragmentNavigationHeaderBinding

class NavigationHeaderFragment : Fragment() {

    lateinit var binding:FragmentNavigationHeaderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavigationHeaderBinding.inflate(inflater, container, false)


        return binding.root

    }



}