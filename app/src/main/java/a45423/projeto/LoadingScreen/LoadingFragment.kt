package a45423.projeto.LoadingScreen

import a45423.projeto.R
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoadingFragment : Fragment() {
    lateinit var mAuth : FirebaseAuth
    var navController : NavController? = null;

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view);
        checkIfUserIsLoggedIn()
    }

    fun checkIfUserIsLoggedIn(){
        mAuth = FirebaseAuth.getInstance()
        mAuth.signOut()
        val user = mAuth.currentUser

        Handler().postDelayed({
            if( user == null){
                navController!!.navigate(R.id.action_loadingFragment_to_loginFragment)
            }
            else{
                navController!!.navigate(R.id.action_loadingFragment_to_mainFragment)
            }

        }, 2000)
    }

}