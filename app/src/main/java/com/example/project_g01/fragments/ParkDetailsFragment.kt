package com.example.project_g01.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.project_g01.R
import com.example.project_g01.databinding.FragmentParkDetailsBinding
import okhttp3.HttpUrl.Companion.toHttpUrl

class ParkDetailsFragment : Fragment(R.layout.fragment_park_details) {
    private var _binding: FragmentParkDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: ParkDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // binding
        _binding = FragmentParkDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvFullName.text=args.selectedPark.parkName
        Glide.with(requireContext()).load(args.selectedPark.parkImage).into(binding.ivImage)
        binding.tvAddress.text=args.selectedPark.parkAddress
        binding.tvUrl.text=args.selectedPark.parkUrl
        binding.tvDescription.text=args.selectedPark.parkDescription
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}