package com.androiddevs.runtrackerapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.runtrackerapp.db.Run
import com.androiddevs.runtrackerapp.service.Polyline
import com.androiddevs.runtrackerapp.service.TrackingService
import com.androiddevs.runtrackerapp.utils.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runtrackerapp.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runtrackerapp.utils.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runtrackerapp.utils.Constants.MAP_ZOOM
import com.androiddevs.runtrackerapp.utils.Constants.POLYLINE_COLOR
import com.androiddevs.runtrackerapp.utils.Constants.POLYLINE_WIDTH
import com.androiddevs.runtrackerapp.utils.TrackingUtility
import com.androiddevs.runtrackerapp.viewmodels.MainViewModel
import com.example.runtrackerapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel : MainViewModel by viewModels()

    private var isTracking = false
    private var map : GoogleMap? = null
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu : Menu? = null

    @set:Inject
    var weight = 80f



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }
        subscribeToObserver()

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        btnFinishRun.setOnClickListener {
                zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.toolbar_tracking_menu , menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){

        CancelTrackingDialog().apply{
                setYesListener {
                    stopRun()
                }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun subscribeToObserver(){
        TrackingService.isTracking.observe(viewLifecycleOwner , Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner , Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner , Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis , true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
    if (isTracking){
        menu?.getItem(0)?.isVisible = true
        sendCommandToService(ACTION_PAUSE_SERVICE)
    } else {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }
    }

    private fun updateTracking(isTracking : Boolean){

        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis >0L){
            btnToggleRun.text = "START"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking){
            menu?.getItem(0)?.isVisible = true
            btnToggleRun.text = "STOP"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last() , MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .width(POLYLINE_WIDTH)
                .color(POLYLINE_COLOR)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size >1){

            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }


    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot { bmp->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
                val avgSpeed = round((distanceInMeters /1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
                val dateTimeStamp = Calendar.getInstance().timeInMillis
                val caloriesBurned = ((distanceInMeters /1000f) *weight).toInt()

            val run = Run(bmp , dateTimeStamp , avgSpeed , distanceInMeters , curTimeInMillis , caloriesBurned)

            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun sendCommandToService(action : String) =
        Intent(requireContext() , TrackingService::class.java).also {
            it.action = action

            requireContext().startService(it)
        }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}