package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import town.robin.toadua.databinding.CommentBinding
import town.robin.toadua.databinding.FragmentSearchBinding
import town.robin.toadua.databinding.SearchCardBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchResults.apply {
            adapter = SearchResultsAdapter()
            layoutManager = LinearLayoutManager(context)
        }

        binding.filterButton.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.searchBar, ChangeBounds().apply { duration = 150 })
            binding.filters.visibility = when (binding.filters.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.sortSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sort_options, R.layout.spinner_item,
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        return binding.root
    }

    private inner class SearchResultsAdapter : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: SearchCardBinding) : RecyclerView.ViewHolder(binding.root)

        val selected = MutableLiveData<Int?>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(SearchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = 16

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.comments.apply {
                adapter = CommentsAdapter()
                layoutManager = LinearLayoutManager(context)
            }

            holder.binding.entry.setOnClickListener {
                selected.value = when (holder.binding.entryControls.visibility) {
                    View.VISIBLE -> null
                    else -> position
                }
            }

            selected.observe(viewLifecycleOwner) {
                TransitionManager.beginDelayedTransition(holder.binding.root, ChangeBounds().apply { duration = 150 })
                holder.binding.entryControls.visibility = if (it == position) View.VISIBLE else View.GONE
            }
        }
    }

    private inner class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: CommentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(CommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) { }
    }
}