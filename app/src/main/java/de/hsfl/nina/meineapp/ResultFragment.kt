package de.hsfl.nina.meineapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.nina.meineapp.databinding.FragmentGameBinding
import de.hsfl.nina.meineapp.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    val mainViewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentResultBinding.inflate(inflater)
        binding.vm = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val done_bt = binding.doneBt
        done_bt.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startFragment)
        }

        return binding.root
    }

}
