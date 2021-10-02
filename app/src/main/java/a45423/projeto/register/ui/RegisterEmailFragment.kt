package a45423.projeto.register.ui

import a45423.projeto.R
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import java.util.regex.Pattern

class RegisterEmailFragment : Fragment() , View.OnClickListener {
    var navController : NavController? = null;
    var username = "";
    var password = "";

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.registerBtn_Email).setOnClickListener(this);

        navController = Navigation.findNavController(view);

        if(arguments != null){
            val args = RegisterEmailFragmentArgs.fromBundle(requireArguments());
            username = args.username;
            password = args.password;
        }

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerBtn_Email -> {
                val email = view?.findViewById<EditText>(R.id.name_value)

                if (email != null) {
                    if(emailValidator(email.text.toString())){
                        val action = RegisterEmailFragmentDirections.actionRegisterEmailFragment2ToNameFragment(username, password, email?.text.toString())
                        navController!!.navigate(action)
                    }
                    else{

                        email.setError("Insert Valid Email!!")
                        email.requestFocus()
                    }
                }
            }
        }
    }

    fun emailValidator(email : String): Boolean {
        return true
        //return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.length > 0
    }

}