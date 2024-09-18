package de.hsfl.nina.meineapp

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.res.TypedArrayUtils.getResourceId
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.nina.meineapp.databinding.FragmentCreateBinding
import de.hsfl.nina.meineapp.databinding.FragmentGameBinding

class CreateFragment : Fragment(), MapPointsAdapter.MapPointClickListener{

    val mainViewModel : MainViewModel by activityViewModels()
    var allowStart: Boolean = false
    private var touchStartTime: Long = 0
    private var isLongPress = false

    private lateinit var mymapview: MyMapView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentCreateBinding.inflate(inflater)
        binding.vm = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        mymapview = binding.myMapViewCreate

        mainViewModel.getXPos().observe(viewLifecycleOwner, { x ->
            mymapview.setXPos(x)
            mymapview.invalidate()
        })

        mainViewModel.getYPos().observe(viewLifecycleOwner, { y ->
            mymapview.setYPos(y)
            mymapview.invalidate()
        })

        mainViewModel.getMapPointsList().observe(viewLifecycleOwner, {
            mymapview.setPointsList(it)
            mymapview.invalidate()
        })



        val recyclerView: RecyclerView = binding.recyclerViewCreate
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        val adapter = MapPointsAdapter((mainViewModel.getMapPointsList().value ?: emptyList()).toMutableList(), true)
        recyclerView.adapter = adapter
        adapter.setMapPointClickListener(this)


        mainViewModel.getMapPointsList().observe(viewLifecycleOwner, { pointsList ->
            adapter.updateData(pointsList)
            if(pointsList == emptyList<MapPoint>().toMutableList()){
                binding.startBt.isEnabled = false
            } else {
                binding.startBt.isEnabled = true
            }
        })

        val start_bt = binding.startBt
        start_bt.setOnClickListener {
            findNavController().navigate(R.id.action_createFragment_to_gameFragment)
            mainViewModel.startGame()
        }

        val setFlag_bt = binding.setFlagBt
        setFlag_bt.setOnClickListener {
        mainViewModel.addMapPoint()
        }

        val cancel_bt = binding.cancelBt
        cancel_bt.setOnClickListener {
            mainViewModel.reset()
            findNavController().navigate(R.id.action_createFragment_to_startFragment)
        }

        mymapview.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleLongPress(event.x, event.y)
                }
            }
            true
        }
        return binding.root
    }

    private fun handleLongPress(x: Float, y: Float) {
        val mapBitmap = BitmapFactory.decodeResource(resources, R.drawable.campuskarte)
        val scale = mymapview.width.toFloat() / mapBitmap.width


        val mapHeight = mapBitmap.height * scale
        val mapWidth = mapBitmap.width * scale

        val mapX = (x - mymapview.x) / mapWidth
        val mapY = ((y - mymapview.paddingTop) - (mymapview.height - mapHeight)/2) / mapHeight
        mainViewModel.addPointWithXY(mapX, mapY)
    }

    override fun onDeleteButtonClick(position: Int) {
        mainViewModel.deletePoint(position)
    }
}
