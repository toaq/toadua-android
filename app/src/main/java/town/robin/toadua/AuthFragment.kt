package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var flagCreateAccount:Boolean = false

/**
 * A simple [Fragment] subclass.
 * Use the [AuthFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuthFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var myinflate: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        myinflate = inflater.inflate(R.layout.fragment_auth, container, false)

        var skipbutton = myinflate.findViewById<Button>(R.id.skip_button)
        skipbutton.setOnClickListener{
            Navigation.findNavController(myinflate).navigate(R.id.auth_to_search)
        }
        /*
        gotoFragment2 = view.findViewById(R.id.fragment_fragment1_gotofragment2);
        gotoFragment2.setOnClickListener(this);
         */

        var createaccountbutton = myinflate.findViewById<Button>(R.id.create_account_button)
        var signincreateaccount = myinflate.findViewById<TextView>(R.id.auth_title)

        createaccountbutton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // do something
                if(flagCreateAccount == false) {
                    flagCreateAccount = true
                    signincreateaccount.text = getString(R.string.create_account)
                    createaccountbutton.text = getString(R.string.use_existing_account)
                }
                else if(flagCreateAccount == true) {
                    flagCreateAccount = false
                    signincreateaccount.text = getString(R.string.sign_in)
                    createaccountbutton.text = getString(R.string.create_account)
                }
            }
        })


        return myinflate

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AuthFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AuthFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}