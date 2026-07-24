package com.xvantage.rental.ui.explore.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.databinding.FragmentFavoritesBinding
import com.xvantage.rental.ui.explore.details.ListingDetailsActivity
import com.xvantage.rental.ui.explore.favorites.adapter.FavoriteAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadFavorites(reset = true)
    }

    override fun onResume() {
        super.onResume()
        // refresh every time the tab is revisited - a listing might have
        // been favorited/unfavorited from the details screen in the meantime
        viewModel.loadFavorites(reset = true)
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(
            context = requireContext(),
            onItemClick = { favorite ->
                ListingDetailsActivity.start(requireContext(), favorite.listingFk)
            },
            onRemoveClick = { favorite, position ->
                viewModel.removeFavorite(favorite, position)
            }
        )
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshFavorites.setOnRefreshListener {
            viewModel.loadFavorites(reset = true)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favorites.collect { favorites ->
                favoriteAdapter.setItems(favorites)
                binding.emptyFavoritesLayout.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
                binding.rvFavorites.visibility = if (favorites.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressBarFavorites.visibility = if (loading) View.VISIBLE else View.GONE
                binding.swipeRefreshFavorites.isRefreshing = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.removedPosition.collect { position ->
                position?.let {
                    favoriteAdapter.removeAt(it)
                    if (favoriteAdapter.itemCount == 0) {
                        binding.emptyFavoritesLayout.visibility = View.VISIBLE
                        binding.rvFavorites.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}