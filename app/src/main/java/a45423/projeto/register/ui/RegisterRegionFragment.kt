package a45423.projeto.register.ui

import a45423.projeto.R
import a45423.projeto.register.NewUser
import a45423.projeto.register.helpers.Country
import a45423.projeto.register.helpers.JsonWorker
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.reflect.TypeToken
import java.io.IOException


class RegisterRegionFragment : Fragment(), View.OnClickListener {
    var navController : NavController? = null

    var username = "";
    var password = "";
    var age = "";
    var name = "";
    var email = "";

    lateinit var countryList : List<Country>;

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_region, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view);
        view.findViewById<Button>(R.id.registerRegion_btn).setOnClickListener(this);

        if(arguments != null){
            val args = RegisterRegionFragmentArgs.fromBundle(requireArguments());
            username = args.username;
            password = args.password;
            age = args.age;
            name = args.name;
            email = args.email
        }

        getDataCountry()
        populateSpinner()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerRegion_btn -> {
                val region = view?.findViewById<Spinner>(R.id.spinnerCountry);

                if (region != null) {

                    val action = RegisterRegionFragmentDirections.actionRegisterRegionToRegisterProfilePic(username, password, age, name, email, region.selectedItem.toString());
                    navController!!.navigate(action)
                }
            }
        }
    }

    fun populateSpinner(){
        val spinnerCountry = view?.findViewById<Spinner>(R.id.spinnerCountry);

        val array_adapter = activity?.let { ArrayAdapter(it, R.layout.custom_spinner, getAllCountrys()) }
        if (array_adapter != null)
            array_adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)

        spinnerCountry!!.setAdapter(array_adapter)
    }

    fun getAllCountrys(): ArrayList<String> {
        var arrayCities : ArrayList<String> = arrayListOf();
        for (country in countryList)
            arrayCities.add(country.city)

        arrayCities.sortBy { it }
        return arrayCities
    }

    fun getDataCountry(){
        val jsonFileString = activity?.let { getJsonDataFromAsset(it, "pt.json") }
        val gson =  JsonWorker().createGson()

        val listPersonType = object : TypeToken<List<Country>>() {}.type

        countryList = gson.fromJson(jsonFileString, listPersonType)
    }

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }


}