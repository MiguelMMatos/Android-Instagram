package a45423.projeto.register.ui

import a45423.projeto.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation

class NameFragment : Fragment(), View.OnClickListener {
    var navController : NavController? = null;
    var username = "";
    var password = "";
    var email = "";

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.registerName_btn).setOnClickListener(this);

        navController = Navigation.findNavController(view);

        if(arguments != null){
            val args = NameFragmentArgs.fromBundle(requireArguments());
            username = args.username;
            password = args.password;
            email = args.email
        }

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerName_btn -> {
                val name = view?.findViewById<EditText>(R.id.name_value);

                if (name != null) {
                    if( nameValidator(name.text.toString())){
                        val action = NameFragmentDirections.actionNameFragmentToRegisterAge(username, password,name?.text.toString(), email );
                        navController!!.navigate(action)
                    } else{
                        name.setError("Insert Valid Name!!")
                        name.requestFocus()
                    }
                }
            }
        }
    }

    fun nameValidator(fullName : String): Boolean {
        return true

        if(fullName.contains(" ")){
            var nameList : List<String> = fullName.split(" ");

            for(name in nameList){
                if(name.length <= 3)
                    return false
            }

            return true
        }

        return false;
    }

}