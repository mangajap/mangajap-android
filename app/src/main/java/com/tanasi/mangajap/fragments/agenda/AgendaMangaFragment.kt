package com.tanasi.mangajap.fragments.agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.mangajap.R
import com.tanasi.mangajap.adapters.AppAdapter
import com.tanasi.mangajap.databinding.FragmentRecyclerViewBinding
import com.tanasi.mangajap.models.MangaEntry
import com.tanasi.mangajap.ui.SpacingItemDecoration
import kotlinx.coroutines.launch

class AgendaMangaFragment : Fragment() {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AgendaMangaViewModel>()

    private val appAdapter = AppAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeAgendaManga()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { state ->
                when (state) {
                    AgendaMangaViewModel.State.Loading -> binding.isLoading.apply {
                        root.visibility = View.VISIBLE
                    }

                    is AgendaMangaViewModel.State.SuccessLoading -> {
                        displayAgendaManga(state.readingManga)
                        binding.isLoading.root.visibility = View.GONE
                    }

                    is AgendaMangaViewModel.State.FailedLoading -> {
                        when (state.error) {
                            is JsonApiResponse.Error.ServerError -> state.error.body.errors.map {
                                Toast.makeText(requireContext(), it.title, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is JsonApiResponse.Error.NetworkError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()

                            is JsonApiResponse.Error.UnknownError -> Toast.makeText(
                                requireContext(),
                                state.error.error.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }


    private fun initializeAgendaManga() {
        binding.recyclerView.apply {
            adapter = appAdapter
            addItemDecoration(
                SpacingItemDecoration(
                    spacing = resources.getDimension(R.dimen.agenda_spacing).toInt()
                )
            )
            layoutManager = LinearLayoutManager(requireContext())
            setPadding(
                resources.getDimension(R.dimen.agenda_spacing).toInt(),
                resources.getDimension(R.dimen.agenda_spacing).toInt(),
                resources.getDimension(R.dimen.agenda_spacing).toInt(),
                resources.getDimension(R.dimen.agenda_spacing).toInt(),
            )
        }
    }

    private fun displayAgendaManga(readingManga: List<MangaEntry>) {
        appAdapter.submitList(
            readingManga
                .filter { it.manga?.let { manga -> it.getProgress(manga) < 100 } ?: false }
                .onEach {
                    it.itemType = AppAdapter.Type.MANGA_ENTRY_TO_READ_ITEM
                }
        )
    }
}