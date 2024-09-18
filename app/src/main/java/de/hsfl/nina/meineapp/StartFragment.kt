package de.hsfl.nina.meineapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.nina.meineapp.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    val mainViewModel: MainViewModel by activityViewModels()
    var allowRetry: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStartBinding.inflate(inflater)
        binding.vm = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        mainViewModel.getMapPointsList().observe(viewLifecycleOwner) {
            allowRetry = it.isNotEmpty()
            updateButtonState(binding.retryBt, binding.createBt, binding.resetBt, binding.scorelistBt)
        }


        binding.nameEt.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val currentText = binding.nameEt.text.toString()
                if (currentText == "Hier Namen eingeben") {
                    binding.nameEt.text.clear()
                }
                var namesArray = mainViewModel.getNameArray()

                if(namesArray.size > 0){
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Vorher verwendete Namen")
                    namesArray = namesArray.plus("Neuer Name")
                    builder.setItems(namesArray) { _, which ->
                        val selectedName = namesArray[which]

                        if (selectedName == "Neuer Name"){
                            binding.nameEt.text.clear()
                            binding.nameEt.requestFocus()

                        } else {
                            binding.nameEt.setText(selectedName)
                        }
                    }
                    builder.create().show()
                }
            }
        }



        binding.nameEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.nameEt.windowToken, 0)
                binding.nameEt.clearFocus()
                true
            } else {
                false
            }
        }



        val switch = binding.switch1
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val scorelist_bt = binding.scorelistBt
        scorelist_bt.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_highscoreFragment)
        }


        val reset_bt = binding.resetBt
        reset_bt.setOnClickListener {
            mainViewModel.reset()
        }

        val create_bt = binding.createBt
        create_bt.setOnClickListener {
            if(mainViewModel.getName().value == "Hier Namen eingeben"){
                val message = "Bitte gib erst einen Namen ein!"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            } else {
                findNavController().navigate(R.id.action_startFragment_to_createFragment)
            }

        }

        val retry_bt = binding.retryBt
        retry_bt.setOnClickListener {
            if (mainViewModel.getName().value != "Hier Namen eingeben"){
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Bestätigung")
                    .setMessage("Ist ${mainViewModel.getName().value} dein Name?")
                    .setPositiveButton("Ja") { _, _ ->
                        findNavController().navigate(R.id.action_startFragment_to_gameFragment)
                        mainViewModel.startGame()
                    }
                    .setNegativeButton("Nein") { _, _ ->
                        binding.nameEt.text.clear()
                        binding.nameEt.requestFocus()
                    }
                    .show()
            } else {
                val message = "Bitte gib erst einen Namen ein!"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }


        }

        updateButtonState(retry_bt, create_bt, reset_bt, scorelist_bt)

        return binding.root
    }

    private fun updateButtonState(retryButton: Button, createButton: Button, resetButton: Button, listButton: Button) {
        retryButton.isEnabled = allowRetry
        createButton.isEnabled = !allowRetry
        resetButton.visibility = if (allowRetry) View.VISIBLE else View.GONE
        listButton.visibility = if (allowRetry) View.VISIBLE else View.GONE
    }
}

