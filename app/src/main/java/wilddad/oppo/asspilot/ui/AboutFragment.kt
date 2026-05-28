package wilddad.oppo.asspilot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set app version
        val versionName = try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: Exception) { "1.0.0" }

        binding.tvVersion.text = getString(R.string.about_version, versionName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
