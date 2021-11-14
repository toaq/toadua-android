package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import town.robin.toadua.databinding.CommentBinding
import town.robin.toadua.databinding.EntryCardBinding
import town.robin.toadua.databinding.FragmentGlossDetailBinding

class GlossDetailFragment : Fragment() {
    private lateinit var binding: FragmentGlossDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlossDetailBinding.inflate(inflater, container, false)

        binding.results.apply {
            adapter = ResultsAdapter()
            layoutManager = LinearLayoutManager(context)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private inner class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: EntryCardBinding) : RecyclerView.ViewHolder(binding.root)

        val selected = MutableLiveData<Int?>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(EntryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

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
        inner class ViewHolder(binding: CommentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(CommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) { }
    }
}