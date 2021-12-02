package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import town.robin.toadua.api.Entry
import town.robin.toadua.api.Note
import town.robin.toadua.api.ToaduaService
import town.robin.toadua.databinding.CommentBinding
import town.robin.toadua.databinding.FragmentSearchBinding
import town.robin.toadua.databinding.EntryCardBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val activityModel: ToaduaViewModel by activityViewModels {
        ToaduaViewModelFactory(requireContext())
    }
    private val model: SearchViewModel by viewModels {
        SearchViewModelFactory(ToaduaService.create(activityModel.prefs.server))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val resultsAdapter = ResultsAdapter(model.results.value)
        binding.results.apply {
            adapter = resultsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.sortSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sort_options, R.layout.spinner_item,
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.menuButton.setOnClickListener {
            findNavController().apply {
                popBackStack()
                navigate(R.id.gloss_fragment)
            }
        }
        binding.filterButton.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.navBar, ChangeBounds().apply { duration = 150 })
            binding.filters.visibility = when (binding.filters.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            model.query.value = text?.toString() ?: ""
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.loading.collect {
                    binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.results.collect {
                    resultsAdapter.apply {
                        results = it
                        selected.value = null
                        notifyDataSetChanged()
                    }

                    binding.welcomeCard.visibility =
                        if (it.isEmpty() && model.query.value.isBlank()) View.VISIBLE else View.GONE
                    binding.noResultsCard.visibility =
                        if (it.isEmpty() && model.query.value.isNotBlank()) View.VISIBLE else View.GONE
                }
            }
        }

        return binding.root
    }

    private inner class ResultsAdapter(var results: Array<Entry>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: EntryCardBinding) : RecyclerView.ViewHolder(binding.root)

        val selected = MutableLiveData<Int?>() // TODO: move to vm

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(EntryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = results.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = results[position]
            holder.binding.apply {
                head.text = entry.head
                body.text = entry.body.replace('▯', '◌')
                author.text = entry.user
                score.text = if (entry.score > 0) "+${entry.score}" else if (entry.score < 0) "${entry.score}" else ""

                comments.visibility = if (entry.notes.isEmpty()) View.GONE else View.VISIBLE
                delete.visibility = if (entry.user == activityModel.username) View.VISIBLE else View.INVISIBLE

                val color = resources.getColor(if (entry.user == "official") R.color.pink else R.color.blue)
                author.setTextColor(color)
                entryControls.setBackgroundColor(color)
            }

            holder.binding.comments.apply {
                adapter = CommentsAdapter(entry.notes)
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

    private inner class CommentsAdapter(val comments: Array<Note>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: CommentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(CommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = comments.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val comment = comments[position]

            holder.binding.apply {
                user.text = comment.user
                commentText.text = comment.content
            }
        }
    }
}