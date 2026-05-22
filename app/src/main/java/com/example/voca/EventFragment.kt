package com.example.voca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voca.adapter.EventAdapter
import com.example.voca.databinding.FragmentEventBinding
import com.example.voca.model.Event
import com.example.voca.repository.EventRepository
import com.example.voca.viewmodel.EventViewModel
import com.example.voca.viewmodel.EventViewModelFactory

class EventFragment : Fragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = EventRepository(requireContext())
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())

        viewModel.events.observe(viewLifecycleOwner) { events ->
            val adapter = EventAdapter(events) { event ->
                showEventDetailDialog(event)
            }
            binding.rvEvents.adapter = adapter
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun showEventDetailDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle(event.name)
            .setMessage("Tanggal: ${event.date}\nLokasi: ${event.location}\nHarga: ${event.getFormattedPrice()}")
            .setPositiveButton("Daftar") { _, _ ->
                viewModel.registerEvent(event.id)
            }
            .setNegativeButton("Hapus") { _, _ ->
                viewModel.deleteEvent(event.id)
            }
            .setNeutralButton("Tutup", null)
            .show()
    }

    private fun showAddEventDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_event, null)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Event Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.etEventName).text.toString().trim()
                val date = dialogView.findViewById<EditText>(R.id.etEventDate).text.toString().trim()
                val location = dialogView.findViewById<EditText>(R.id.etEventLocation).text.toString().trim()
                val priceStr = dialogView.findViewById<EditText>(R.id.etEventPrice).text.toString().trim()
                val price = priceStr.toIntOrNull() ?: 0

                if (name.isEmpty() || date.isEmpty() || location.isEmpty()) {
                    Toast.makeText(requireContext(), "Isi semua field wajib!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.addEvent(Event(0, name, date, location, price))
                Toast.makeText(requireContext(), "Event berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
