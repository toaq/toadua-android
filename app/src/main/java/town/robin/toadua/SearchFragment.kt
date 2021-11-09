package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import town.robin.toadua.databinding.FragmentSearchBinding
import town.robin.toadua.databinding.SearchCardBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val searchResultAdapter = SearchResultsAdapter()
        binding.searchResults.adapter = searchResultAdapter
        binding.searchResults.apply {
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(context)
        }

        return binding.root
    }

    private inner class SearchResultsAdapter : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
        inner class ViewHolder(binding: SearchCardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(SearchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = 16

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }
    }
}