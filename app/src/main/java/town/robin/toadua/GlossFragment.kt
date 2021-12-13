package town.robin.toadua

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import town.robin.toadua.api.Entry
import town.robin.toadua.databinding.FragmentGlossBinding
import town.robin.toadua.databinding.GlossCardBinding

@FlowPreview
@ExperimentalCoroutinesApi
class GlossFragment : Fragment() {
    companion object {
        const val PARAM_QUERY = "query"
        private const val UI_BLANK = '◌'
        private const val API_BLANK = '▯'
    }

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

        binding.menuButton.setOnClickListener {
            (requireActivity() as MainActivity).openNavDrawer()
        }
        binding.clearButton.setOnClickListener {
            binding.glossInput.text.clear()
        }

        binding.glossInput.doOnTextChanged { text, _, _, _ ->
            model.query.value = text?.toString() ?: ""
            binding.clearButton.visibility = if (text?.isNotBlank() == true) View.VISIBLE else View.GONE
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
                model.errors.collect { (type, message) ->
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(type.string, message ?: getString(R.string.cant_connect)))
                        .show()
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

        arguments?.getString(PARAM_QUERY)?.let {
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
            holder.binding.glossDefinition.text = entry?.body?.replace(API_BLANK, UI_BLANK) ?: getString(R.string.missing_gloss)

            holder.binding.root.setOnClickListener {
                findNavController().apply {
                    popBackStack()
                    navigate(R.id.search_fragment, bundleOf(SearchFragment.PARAM_QUERY to term))
                }
            }
        }
    }
}