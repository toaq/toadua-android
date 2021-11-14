package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import town.robin.toadua.databinding.FragmentGlossBinding
import town.robin.toadua.databinding.GlossCardBinding

class GlossFragment : Fragment() {
    private lateinit var binding: FragmentGlossBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlossBinding.inflate(inflater, container, false)

        binding.results.apply {
            adapter = ResultsAdapter()
            layoutManager = LinearLayoutManager(context)
        }

        binding.menuButton.setOnClickListener {
            findNavController().apply {
                popBackStack()
                navigate(R.id.search_fragment)
            }
        }

        return binding.root
    }

    private inner class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: GlossCardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(GlossCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = 8

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.root.setOnClickListener {
                findNavController().navigate(R.id.gloss_to_gloss_detail)
            }
        }
    }
}