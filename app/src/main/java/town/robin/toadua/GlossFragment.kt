package town.robin.toadua

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import town.robin.toadua.databinding.FragmentGlossBinding
import town.robin.toadua.databinding.GlossCardBinding

class GlossFragment : Fragment() {
    private lateinit var binding: FragmentGlossBinding
    private val activityModel: ToaduaViewModel by activityViewModels {
        ToaduaViewModel.Factory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlossBinding.inflate(inflater, container, false)

        binding.results.apply {
            adapter = ResultsAdapter()
            layoutManager = LinearLayoutManager(context)
        }

        //new start
        val navigation = binding.mynavigationview
        navigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home ->    {             // Handle settings click
                    findNavController().apply {
                        //getActivity()?.setTitle("Search")
                        popBackStack()
                        navigate(R.id.auth_fragment)
                    }
                    true}
                R.id.nav_account->                 // Handle menu click
                    true
                R.id.nav_settings ->    {             // Handle settings click
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
                R.id.nav_changeserver ->                 // Handle logout click
                    true
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