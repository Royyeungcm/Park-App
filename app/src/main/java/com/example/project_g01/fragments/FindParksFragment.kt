package com.example.project_g01.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project_g01.R
import com.example.project_g01.databinding.FragmentFindParksBinding
import com.example.project_g01.models.AllPark
import com.example.project_g01.models.ShowingPark
import com.example.project_g01.networking.ApiService
import com.example.project_g01.networking.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import retrofit2.Response


class FindParksFragment : Fragment(R.layout.fragment_find_parks), OnMapReadyCallback {
    private val TAG = "SCREENFINDPARK"

    //binding variables
    private var _binding: FragmentFindParksBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap

    private var firestore = FirebaseFirestore.getInstance()
    val statesCollectionRef = firestore.collection("states")

    var showingParkList: MutableList<ShowingPark> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // binding
        _binding = FragmentFindParksBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(binding.fragmentMap.id) as? SupportMapFragment
        mapFragment?.getMapAsync {
            Log.d(TAG, "Map fragment found")
        }
        if (mapFragment == null) {
            Log.d(TAG, "++++ map fragment is null")
        } else {
            Log.d(TAG, "++++ map fragment is NOT null")
            mapFragment?.getMapAsync(this)
        }

        binding.btnConfirm.setOnClickListener {
            val selectedStateFromUI = binding.spState.selectedItem.toString()
            mMap.clear()
            findDocument(selectedStateFromUI)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        mMap.isTrafficEnabled = true
        val uiSettings = googleMap.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isCompassEnabled = true
        val intialLocation = LatLng(45.8877, -101.1205)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(intialLocation, 3.0f))
    }


    private fun findDocument(selectedStateFromUI: String) {
        val stateDocumentRef = statesCollectionRef.document(selectedStateFromUI)
        stateDocumentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val foundStateAbbreviation = document.get("abbreviation").toString()
                lifecycleScope.launch {
                    Log.d(TAG, "step1")
                    var responseFromAPI: AllPark? = getAllParkFromAPI(foundStateAbbreviation)
                    if (responseFromAPI == null) {
                        Log.d(TAG, "ERROR with the getAllParkFromAPI() function")
                        return@launch
                    }
                    Log.d(TAG, "SUCCESS from the getAllParkFromAPI() function")
                    Log.d(TAG, responseFromAPI.data[1].fullName)


                    for (currPark in responseFromAPI.data) {
                        val currParkLatLng =
                            LatLng(currPark.latitude.toDouble(), currPark.longitude.toDouble())
                        mMap.addMarker(
                            MarkerOptions().position(currParkLatLng)
                                .title(currPark.fullName)
                                .snippet(currPark.addresses[0].line1)
                        )
                        val instance = ShowingPark(
                            currPark.fullName,
                            currPark.images[0].url,
                            currPark.addresses[0].line1,
                            currPark.url,
                            currPark.description
                        )
                        showingParkList.add(instance)
                    }
                    val firstParkLatLng = LatLng(
                        responseFromAPI.data[0].latitude.toDouble(),
                        responseFromAPI.data[0].longitude.toDouble()
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstParkLatLng, 5.0f))
                    mMap.setOnInfoWindowClickListener { marker ->
                        val selectedParkId = marker.id.substring(1).toInt()
                        val action =
                            FindParksFragmentDirections.actionFindParksFragmentToParkDetailsFragment(
                                showingParkList[selectedParkId]
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun getAllParkFromAPI(foundStateAbbreviation: String): AllPark? {
        val apiKey = "gp36bm39ndC7rSfRlQWHDLW4uoPRAZxvtDY81Obc"
        var apiService: ApiService = RetrofitInstance.retrofitService
        Log.d(TAG, foundStateAbbreviation)
        val response: Response<AllPark> = apiService.getAllPark(foundStateAbbreviation, apiKey)
        if (response.isSuccessful) {
            val dataFromAPI = response.body()   /// myresponseobject
            if (dataFromAPI == null) {
                Log.d("API", "No data from API or some other error")
                return null
            }
            Log.d(TAG, "Here is the data from the API")
            Log.d(TAG, dataFromAPI.toString())
            return dataFromAPI
        } else {
            Log.d(TAG, "An error occurred")
            return null
        }
    }
}
