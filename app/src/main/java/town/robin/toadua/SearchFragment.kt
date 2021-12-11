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
import androidx.navigation.fragment.findNavController
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
import android.view.Gravity
import android.widget.*


import com.google.android.material.internal.NavigationMenu


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var binding2: NavigationMenu
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
        binding.createButton.visibility = if (activityModel.loggedIn) View.VISIBLE else View.INVISIBLE

        //new start
        val navigation = binding.mynavigationview
        val header = navigation.getHeaderView(0)
        if (activityModel.loggedIn) {
            header.findViewById<TextView>(R.id.auth_status).setText(R.string.logged_in_as)
            header.findViewById<TextView>(R.id.username).text = activityModel.prefs.username
        } else {
            header.findViewById<TextView>(R.id.auth_status).setText(R.string.connected_to)
            header.findViewById<TextView>(R.id.username).visibility = View.GONE
        }
        header.findViewById<TextView>(R.id.server).text = activityModel.serverName
        navigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_language-> {                // Handle menu click
                    val input = EditText(requireContext()).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ).apply {
                            setText(activityModel.prefs.language)
                            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                            marginStart = margin
                            marginEnd = margin
                        }
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.language)
                        .setView(FrameLayout(requireContext()).apply { addView(input) })
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            activityModel.prefs.language = input.text.toString()
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show().apply {
                            val button = getButton(AlertDialog.BUTTON_POSITIVE)
                            input.doOnTextChanged { text, _, _, _ ->
                                button.isEnabled = text?.isNotBlank() ?: false
                            }
                        }

                    input.postDelayed({ focusInput(input) },200)
                    true}
                R.id.nav_glosser ->    {             // Handle settings click
                    findNavController().apply {
                        //getActivity()?.setTitle("Search")
                        popBackStack()
                        navigate(R.id.gloss_fragment)
                    }
                    true}
                R.id.nav_search ->    {             // Handle settings click
                    findNavController().apply {
                        //getActivity()?.setTitle("Search")
                        popBackStack()
                        navigate(R.id.search_fragment)
                    }
                    true}
                R.id.nav_logout -> {                // Handle logout click
                    activityModel.logOut()
                    findNavController().navigate(R.id.auth_fragment)
                    true}

                else -> false
            }
        }
        //new end
        binding.menuButton.setOnClickListener(View.OnClickListener {
            val navDrawer = binding.myDrawerLayout
            // If the navigation drawer is not open then open it, if its already open then close it.
            if (!navDrawer.isDrawerOpen(Gravity.START)) navDrawer.openDrawer(Gravity.START) else navDrawer.closeDrawer(
                Gravity.END
            )
        })


        binding.clearButton.setOnClickListener {
            binding.searchInput.text.clear()
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
        binding.cancelButton.setOnClickListener {
            model.createMode.value = false
        }
        binding.submitButton.setOnClickListener {
            model.createEntry(binding.createTermInput.text.toString(), binding.createDefinitionInput.text.toString())
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
                        binding.createButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.results.collect { results ->
                    model.uiResults.value = results?.let { LiveList(it, null, UpdateAction.ADD) }
                    // TODO: cancel any pending actions
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

        return binding.root
    }

    private inner class ResultsAdapter(var results: List<Entry>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
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

                comments.apply {
                    adapter = CommentsAdapter(entry.notes)
                    layoutManager = LinearLayoutManager(context)
                }

                like.setImageResource(if (entry.vote == 1) R.drawable.ic_like_active else R.drawable.ic_like_inactive)
                dislike.setImageResource(if (entry.vote == -1) R.drawable.ic_dislike_active else R.drawable.ic_dislike_inactive)
                delete.visibility = if (entry.user == activityModel.prefs.username) View.VISIBLE else View.INVISIBLE

                // TODO: do this the non-deprecated way
                val color = resources.getColor(if (entry.user == "official") R.color.pink else R.color.blue)
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

                    input.postDelayed({ focusInput(input) },200)
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
                TransitionManager.beginDelayedTransition(holder.binding.root, ChangeBounds().apply { duration = 150 })
                holder.binding.entryControls.visibility = if (thisSelected && activityModel.loggedIn) View.VISIBLE else View.GONE
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