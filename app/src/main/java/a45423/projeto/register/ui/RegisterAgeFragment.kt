package a45423.projeto.register.ui

import a45423.projeto.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation


class RegisterAgeFragment : Fragment(), View.OnClickListener {
    var username = ""
    var password = ""
    var name = ""
    var email = ""
    var navController : NavController? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_age, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view);

        if(arguments != null){
            val args = RegisterAgeFragmentArgs.fromBundle(requireArguments());
            username = args.username
            password = args.password
            name = args.name
            email = args.email
        }

        view.findViewById<Button>(R.id.registerAge_btn).setOnClickListener(this)
        fillSpinner(R.id.yearSpinner, 1950, 2021)
        fillSpinner(R.id.monthSpinner, 1, 12);
        fillSpinner(R.id.daySpinner, 1, 31);
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerAge_btn -> {
                val yearValue = view?.findViewById<Spinner>(R.id.yearSpinner)
                val monthValue = view?.findViewById<Spinner>(R.id.monthSpinner)
                val dayValue = view?.findViewById<Spinner>(R.id.daySpinner)
                if (yearValue != null && monthValue != null && dayValue != null) {
                    val ageString : String = dayValue?.selectedItem.toString() + "-" + monthValue?.selectedItem.toString() + "-" + yearValue?.selectedItem.toString()
                    val action = RegisterAgeFragmentDirections.actionRegisterAgeToRegisterRegion(username, password, ageString, name, email)
                    navController!!.navigate(action)
                }
            }
        }
    }

    fun fillSpinner(spinnerId : Int, min : Int, max : Int){
        var ages = ArrayList<String>();

        for(i in min..max)
            ages.add(i.toString());

        val spinnerAge = view?.findViewById<Spinner>(spinnerId);

        val array_adapter = activity?.let { ArrayAdapter(it, R.layout.custom_spinner, ages) }
        if (array_adapter != null)
            array_adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)

        spinnerAge!!.setAdapter(array_adapter)
    }
}