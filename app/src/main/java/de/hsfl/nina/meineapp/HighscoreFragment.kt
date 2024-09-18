package de.hsfl.nina.meineapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.nina.meineapp.databinding.FragmentGameBinding
import de.hsfl.nina.meineapp.databinding.FragmentHighscoreBinding

class HighscoreFragment : Fragment() {

    val mainViewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHighscoreBinding.inflate(inflater, container, false)
        binding.vm = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val backButton = binding.backBt
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_highscoreFragment_to_startFragment)
        }

        val recyclerView: RecyclerView = binding.highscoreRecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val adapter = HighscoreViewAdapter(emptyList<Highscore>().toMutableList())
        recyclerView.adapter = adapter

        mainViewModel.getHighscoreList().observe(viewLifecycleOwner) {
            Log.d("HighscoreFragment", "HighscoreList: $it")
            adapter.updateData(it)
        }

        return binding.root
    }

}
