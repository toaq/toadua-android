package town.robin.toadua

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import town.robin.toadua.api.Entry
import town.robin.toadua.api.Note
import town.robin.toadua.api.SortOrder
import town.robin.toadua.databinding.CommentBinding
import town.robin.toadua.databinding.FragmentSearchBinding
import town.robin.toadua.databinding.EntryCardBinding
import android.widget.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.math.max
import kotlin.math.min

@FlowPreview
@ExperimentalCoroutinesApi
class SearchFragment : Fragment() {
    companion object {
        const val PARAM_QUERY = "query"
        private const val UI_BLANK = '◌'
        private const val API_BLANK = '▯'
        private const val OFFICIAL_USER = "official"
        private const val ALERT_DIALOG_DELAY: Long = 200
        private const val TRANSITION_LENGTH: Long = 150
    }
    private lateinit var binding: FragmentSearchBinding
    private val activityModel: ToaduaViewModel by activityViewModels {
        ToaduaViewModel.Factory(requireContext())
    }
    private val model: SearchViewModel by viewModels {
        SearchViewModel.Factory(activityModel.api, activityModel.prefs)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val resultsAdapter = ResultsAdapter(model.uiResults.value?.list ?: listOf())
        binding.results.apply {
            adapter = resultsAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator?.changeDuration = 0
        }
        binding.sortSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sort_options, R.layout.spinner_item,
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.createButton.visibility = if (activityModel.loggedIn.value) View.VISIBLE else View.GONE

        binding.menuButton.setOnClickListener {
            (requireActivity() as MainActivity).openNavDrawer()
        }
        binding.clearButton.setOnClickListener {
            binding.searchInput.text.clear()
        }
        binding.filterButton.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.navBar, ChangeBounds().apply { duration = TRANSITION_LENGTH })
            binding.filters.visibility = when (binding.filters.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            model.query.value = text?.toString() ?: ""
            binding.clearButton.visibility = if (text?.isNotEmpty() == true) View.VISIBLE else View.GONE
        }
        binding.userInput.doOnTextChanged { text, _, _, _ ->
            model.userFilter.value = text?.toString() ?: ""
        }
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.sortOrder.value = when (position) {
                    0 -> null
                    1 -> SortOrder.NEWEST
                    2 -> SortOrder.OLDEST
                    3 -> SortOrder.HIGHEST
                    4 -> SortOrder.LOWEST
                    else -> SortOrder.RANDOM
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                model.sortOrder.value = null
            }
        }
        binding.createButton.setOnClickListener {
            binding.createTermInput.text.clear()
            binding.createDefinitionInput.text.clear()
            model.createMode.value = true
        }
        binding.insertBlankButton.setOnClickListener {
            if (binding.createDefinitionInput.isFocused) {
                val start = max(binding.createDefinitionInput.selectionStart, 0)
                val end = max(binding.createDefinitionInput.selectionEnd, 0)
                binding.createDefinitionInput.text.replace(min(start, end), max(start, end), UI_BLANK.toString())
            }
        }
        binding.cancelButton.setOnClickListener {
            model.createMode.value = false
        }
        binding.submitButton.setOnClickListener {
            model.createEntry(
                binding.createTermInput.text.toString(),
                binding.createDefinitionInput.text.toString().replace(UI_BLANK, API_BLANK)
            )
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
                model.createMode.collect {
                    if (it) {
                        binding.results.visibility = View.GONE
                        binding.welcomeCard.visibility = View.GONE
                        binding.noResultsCard.visibility = View.GONE
                        binding.createCard.visibility = View.VISIBLE
                        binding.createButton.visibility = View.GONE
                        binding.searchInput.text.clear()
                        binding.userInput.text.clear()
                        binding.sortSpinner.setSelection(0)
                        focusInput(binding.createTermInput)
                    } else {
                        binding.results.visibility = View.VISIBLE
                        binding.welcomeCard.visibility = if (model.uiResults.value == null) View.VISIBLE else View.GONE
                        binding.noResultsCard.visibility = if (model.uiResults.value?.list?.isEmpty() == true) View.VISIBLE else View.GONE
                        binding.createCard.visibility = View.GONE
                        binding.createButton.visibility = if (activityModel.loggedIn.value) View.VISIBLE else View.GONE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.errors.collect { (type, message) ->
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(type.string, message ?: getString(R.string.cant_connect)))
                        .show()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.results.collect { results ->
                    model.uiResults.value = results?.let { LiveList(it, null, UpdateAction.ADD) }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.uiResults.collect {
                    resultsAdapter.apply {
                        results = it?.list ?: listOf()
                        when (it?.updateIndex) {
                            null -> {
                                // The entire list was changed
                                selected.value = null
                                notifyDataSetChanged()
                            }
                            else -> when (it.updateAction) {
                                // A single entry was changed
                                UpdateAction.ADD -> {
                                    if (selected.value != null && it.updateIndex <= selected.value!!)
                                        selected.value = selected.value!! + 1
                                    notifyItemInserted(it.updateIndex)
                                }
                                UpdateAction.REMOVE -> {
                                    selected.value = null
                                    notifyItemRemoved(it.updateIndex)
                                }
                                UpdateAction.MODIFY -> notifyItemChanged(it.updateIndex)
                            }
                        }
                    }

                    binding.welcomeCard.visibility =
                        if (!model.createMode.value && it == null) View.VISIBLE else View.GONE
                    binding.noResultsCard.visibility =
                        if (!model.createMode.value && it?.list?.isEmpty() == true) View.VISIBLE else View.GONE
                }
            }
        }

        arguments?.getString(PARAM_QUERY)?.let {
            binding.searchInput.setText(it)
        }

        return binding.root
    }

    private inner class ResultsAdapter(var results: List<Entry>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: EntryCardBinding) : RecyclerView.ViewHolder(binding.root)

        val selected = MutableLiveData<Int?>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(EntryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = results.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = results[position]
            holder.binding.apply {
                head.text = entry.head
                body.text = entry.body.replace(API_BLANK, UI_BLANK)
                author.text = entry.user
                score.text = if (entry.score > 0) "+${entry.score}" else if (entry.score < 0) "${entry.score}" else ""

                comments.apply {
                    adapter = CommentsAdapter(entry.notes)
                    layoutManager = LinearLayoutManager(context)
                }

                like.setImageResource(if (entry.vote == 1) R.drawable.ic_like_active else R.drawable.ic_like_inactive)
                dislike.setImageResource(if (entry.vote == -1) R.drawable.ic_dislike_active else R.drawable.ic_dislike_inactive)
                delete.visibility = if (entry.user == activityModel.prefs.username.value) View.VISIBLE else View.INVISIBLE

                // TODO: do this the non-deprecated way
                val color = resources.getColor(if (entry.user == OFFICIAL_USER) R.color.pink else R.color.blue)
                author.setTextColor(color)
                entryControls.setBackgroundColor(color)

                this.entry.setOnClickListener {
                    selected.value = when (selected.value) {
                        holder.adapterPosition -> null
                        else -> holder.adapterPosition
                    }
                }
                like.setOnClickListener {
                    model.voteOnEntry(holder.adapterPosition, if (entry.vote == 1) 0 else 1)
                }
                dislike.setOnClickListener {
                    model.voteOnEntry(holder.adapterPosition, if (entry.vote == -1) 0 else -1)
                }
                comment.setOnClickListener {
                    val input = EditText(requireContext()).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ).apply {
                            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                            marginStart = margin
                            marginEnd = margin
                        }
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.comment_title)
                        .setView(FrameLayout(requireContext()).apply { addView(input) })
                        .setPositiveButton(R.string.submit) { _, _ ->
                            model.commentOnEntry(holder.adapterPosition, input.text.toString())
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show().apply {
                            val button = getButton(AlertDialog.BUTTON_POSITIVE)
                            button.isEnabled = false
                            input.doOnTextChanged { text, _, _, _ ->
                                button.isEnabled = text?.isNotBlank() ?: false
                            }
                        }

                    input.postDelayed({ focusInput(input) }, ALERT_DIALOG_DELAY)
                }
                copy.setOnClickListener {
                    binding.createTermInput.setText(entry.head)
                    binding.createDefinitionInput.setText(entry.body)
                    model.createMode.value = true
                }
                delete.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.confirm_delete, entry.head))
                        .setPositiveButton(R.string.delete) { _, _ ->
                            model.deleteEntry(holder.adapterPosition)
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show()
                }
            }

            selected.observe(viewLifecycleOwner) {
                val thisSelected = it == holder.adapterPosition
                TransitionManager.beginDelayedTransition(holder.binding.root, ChangeBounds().apply { duration = TRANSITION_LENGTH })
                holder.binding.entryControls.visibility = if (thisSelected && activityModel.loggedIn.value) View.VISIBLE else View.GONE
                holder.binding.comments.visibility = if (thisSelected && entry.notes.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private inner class CommentsAdapter(val comments: List<Note>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
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

    private fun focusInput(view: View) {
        view.requestFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}