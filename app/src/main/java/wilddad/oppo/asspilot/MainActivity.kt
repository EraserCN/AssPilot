package wilddad.oppo.asspilot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import wilddad.oppo.asspilot.databinding.ActivityMainBinding
import wilddad.oppo.asspilot.ui.ChatFragment
import wilddad.oppo.asspilot.ui.HistoryFragment
import wilddad.oppo.asspilot.ui.AboutFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Enable Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Handle Status Bar Inset for AppBar
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        // ✅ Handle Navigation Bar Inset for Bottom Navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(ChatFragment())
        }

        binding.fabNewChat.setOnClickListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (current is ChatFragment) {
                current.startNewChat()
            } else {
                binding.bottomNav.selectedItemId = R.id.nav_chat
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (current !is ChatFragment) {
                        loadFragment(ChatFragment())
                    }
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_about -> {
                    loadFragment(AboutFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (current is HistoryFragment) {
            current.refresh()
        }
    }
}
