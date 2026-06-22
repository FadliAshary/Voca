package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.voca.databinding.FragmentSettingsBinding
import com.example.voca.utils.SessionManager
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        setupUI()

        binding.btnMyProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        binding.btnCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }

        // Initialize switch state without triggering listener
        binding.switchDarkMode.isChecked = session.isDarkMode()
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.switchGoalReminder.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Notifikasi tabungan $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        binding.tvCurrentCurrency.text = session.getCurrency()
        val lang = session.getLanguage()
        binding.tvCurrentLanguage.text = when (lang) {
            "id" -> "Indonesia"
            "in" -> "Indonesia"
            else -> "English"
        }
    }

    private fun showCurrencyDialog() {
        val currencies = arrayOf("IDR", "USD", "EUR", "JPY")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Mata Uang")
            .setItems(currencies) { _, which ->
                val selected = currencies[which]
                session.setCurrency(selected)
                binding.tvCurrentCurrency.text = selected
                Toast.makeText(requireContext(), "Mata uang diubah ke $selected", Toast.LENGTH_SHORT).show()
                activity?.recreate()
            }
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Indonesia", "English")
        val codes = arrayOf("id", "en") // Changed 'in' to 'id' for better compatibility
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Bahasa")
            .setItems(languages) { _, which ->
                val selectedCode = codes[which]
                session.setLanguage(selectedCode)
                binding.tvCurrentLanguage.text = languages[which]
                setLocale(selectedCode)
            }
            .show()
    }

    private fun setLocale(langCode: String) {
        val appLocale: androidx.core.os.LocaleListCompat = androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // AppCompatDelegate.setApplicationLocales automatically handles activity recreation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}