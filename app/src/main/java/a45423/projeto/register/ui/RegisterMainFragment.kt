package a45423.projeto.register.ui

import a45423.projeto.GlobalData.GlobalData
import a45423.projeto.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth


class RegisterMainFragment : Fragment(), View.OnClickListener {
    var navController : NavController? = null;
    lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view);

        mAuth = FirebaseAuth.getInstance()

        if(  mAuth.currentUser != null){
            view.findViewById<LinearLayout>(R.id.PasswordContainer).visibility = View.GONE
        }

        view.findViewById<Button>(R.id.registerMain_btn).setOnClickListener(this);

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerMain_btn -> {
                if(  mAuth.currentUser == null)
                    registerWithPassword()
                else
                    registerWithoutPassword()
            }
        }
    }

    fun registerWithPassword(){
        val usernameText = view?.findViewById<EditText>(R.id.name_value);
        val passwordText = view?.findViewById<EditText>(R.id.password_value);

        if (usernameText != null &&  passwordText != null ) {
            if(usernameText.text.length >= GlobalData().usernameMinLenght && passwordText.text.length >= GlobalData().passwordMinLength){
                val action = RegisterMainFragmentDirections.actionRegisterMainFragmentToRegisterEmailFragment2(usernameText.text.toString(), passwordText.text.toString());
                navController!!.navigate(action)
            }
            else{
                if(usernameText.text.length < GlobalData().usernameMinLenght){
                    usernameText.setError("Insert Valid Username!!")
                    usernameText.requestFocus()
                }


                if(passwordText.text.length < GlobalData().passwordMinLength){
                    passwordText.setError("Insert Valid Password!!")
                    passwordText.requestFocus()
                }
            }
        }
    }

    fun registerWithoutPassword(){
        val usernameText = view?.findViewById<EditText>(R.id.name_value);

        if (usernameText != null) {
            if(usernameText.text.length >= GlobalData().usernameMinLenght){
                val action = RegisterMainFragmentDirections.actionRegisterMainFragmentToNameFragment(usernameText.text.toString(), "", mAuth.currentUser?.email.toString());
                navController!!.navigate(action)
            }
            else{
                if(usernameText.text.length < GlobalData().usernameMinLenght){
                    usernameText.setError("Insert Valid Username!!")
                    usernameText.requestFocus()
                }
            }
        }
    }

    fun showAlert(view : View, text : String){
        val dlgAlert = AlertDialog.Builder(view.context);

        dlgAlert.setMessage(text);
        dlgAlert.setTitle("Error Message...");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

}