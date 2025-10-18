package com.example.fypdeadreckoning.ui.debug

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fypdeadreckoning.activity.DirectionTestActivity
import com.example.fypdeadreckoning.activity.StepTestActivity
import com.example.fypdeadreckoning.databinding.FragmentDebugBinding

class DebugFragment : Fragment() {

    private var _binding: FragmentDebugBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val debugViewModel =
            ViewModelProvider(this)[DebugViewModel::class.java]

        _binding = FragmentDebugBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDebug
        debugViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stepDebugButton.setOnClickListener {
            val intent = Intent(requireContext(), StepTestActivity::class.java)
            startActivity(intent)
        }

        binding.directionDebugButton.setOnClickListener {
            val intent = Intent(requireContext(), DirectionTestActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}