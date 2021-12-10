package town.robin.toadua

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import town.robin.toadua.api.Entry
import town.robin.toadua.databinding.FragmentGlossBinding
import town.robin.toadua.databinding.GlossCardBinding

class GlossFragment : Fragment() {
    private lateinit var binding: FragmentGlossBinding
    private val activityModel: ToaduaViewModel by activityViewModels {
        ToaduaViewModel.Factory(requireContext())
    }
    private val model: GlossViewModel by viewModels {
        GlossViewModel.Factory(activityModel.api, activityModel.prefs)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlossBinding.inflate(inflater, container, false)

        val resultsAdapter = ResultsAdapter(model.results.value ?: listOf())
        binding.results.apply {
            adapter = resultsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        //new start
        val navigation = binding.mynavigationview
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
                        //item.setTitle("Search")
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

        binding.glossInput.doOnTextChanged { text, _, _, _ ->
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
                        results = it ?: listOf()
                        notifyDataSetChanged()
                    }
                }
            }
        }

        arguments?.getString("query")?.let {
            binding.glossInput.setText(it)
        }

        return binding.root
    }

    private inner class ResultsAdapter(var results: List<Pair<String, Entry?>>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: GlossCardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(GlossCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = results.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (term, entry) = results[position]

            holder.binding.glossTerm.text = term
            holder.binding.glossDefinition.text = entry?.body ?: getString(R.string.missing_gloss)
        }
    }

    private fun focusInput(view: View) {
        view.requestFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}