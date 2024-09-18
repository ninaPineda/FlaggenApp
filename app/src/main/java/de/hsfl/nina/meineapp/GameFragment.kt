package de.hsfl.nina.meineapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.nina.meineapp.databinding.FragmentGameBinding

class GameFragment : Fragment() {
    val mainViewModel : MainViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentGameBinding.inflate(inflater)
        binding.vm = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val scrollView =  binding.scrollView3

        val mymapview = binding.myMapView

        val progressBar = binding.progressBar

        val recyclerView: RecyclerView = binding.recyclerViewGame
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        val adapter = MapPointsAdapter((mainViewModel.getMapPointsList().value ?: emptyList()).toMutableList(), false)
        recyclerView.adapter = adapter

        mainViewModel.getXPos().observe(viewLifecycleOwner, { x ->
            mymapview.setXPos(x)
            mymapview.invalidate()
        })

        mainViewModel.getYPos().observe(viewLifecycleOwner, { y ->
            mymapview.setYPos(y)
            mymapview.invalidate()
        })

        mainViewModel.getMapPointsList().observe(viewLifecycleOwner, { list ->
            mymapview.setPointsList(list)
            progressBar.progress = 100 - mainViewModel.getProgressPercent()
            adapter.updateData(list)
            recyclerView.post {
                adapter.updateData(list)
            }
            progressBar.invalidate()
            recyclerView.invalidate()
            scrollView.invalidate()
            mymapview.invalidate()
        })

        mainViewModel.getDone().observe(viewLifecycleOwner, { done ->
            if(done == true){
                findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
            }
        })

        mainViewModel.getShowToastEvent().observe(viewLifecycleOwner, { message ->
            if(message != ""){
                Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
            }

        })


        val leave_bt = binding.leaveBt
        leave_bt.setOnClickListener {
            mainViewModel.killGame()
            findNavController().navigate(R.id.action_gameFragment_to_startFragment)

        }

        return binding.root

    }
}
