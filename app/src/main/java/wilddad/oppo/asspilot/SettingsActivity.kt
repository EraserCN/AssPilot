package wilddad.oppo.asspilot

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import wilddad.oppo.asspilot.databinding.ActivitySettingsBinding
import wilddad.oppo.asspilot.utils.Prefs

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Enable Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Handle Status Bar Inset for Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        prefs = Prefs(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Load saved base URL
        binding.etBaseUrl.setText(
            prefs.baseUrl.let { if (it == Prefs.DEFAULT_BASE_URL) "" else it }
        )

        binding.btnSave.setOnClickListener {
            val baseUrl = binding.etBaseUrl.text.toString().trim()
            prefs.baseUrl = baseUrl.ifEmpty { Prefs.DEFAULT_BASE_URL }
            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
