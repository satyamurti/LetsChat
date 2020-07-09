package com.mrspd.letschat.fragments.ArSefie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.ArselfeFragmentBinding
import com.mrspd.letschat.databinding.ArselfieHomeFragmentBinding
import kotlinx.android.synthetic.main.arselfie_home_fragment.*


class ARSelfieFragmentHome : Fragment() {
    private lateinit var binding: ArselfieHomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.arselfie_home_fragment, container, false)

        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

       cdArSelfie.setOnClickListener {
           findNavController().navigate(R.id.action_ARSelfieFragmentHome_to_ARSelfieFragment2)
       }
       cdAROnFloor.setOnClickListener {
           findNavController().navigate(R.id.action_ARSelfieFragmentHome_to_ARFloorFragment)

       }
       cdNormalSelfie.setOnClickListener {
           findNavController().navigate(R.id.action_ARSelfieFragmentHome_to_selfieFragment)
       }
    }

}