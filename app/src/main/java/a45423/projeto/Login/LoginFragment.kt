package a45423.projeto.Login

import a45423.projeto.R
import a45423.projeto.register.ui.NameFragmentDirections
import a45423.projeto.register.ui.RegisterRegionFragmentDirections
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseError
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class LoginFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth : FirebaseAuth
    lateinit var googleSignInClient : GoogleSignInClient
    var navController : NavController? = null

    lateinit var email : EditText
    lateinit var password : EditText

    companion object{
        private const val RC_SIGN_IN = 120
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }!!

        mAuth = FirebaseAuth.getInstance()

        view.findViewById<com.google.android.gms.common.SignInButton>(R.id.sign_in_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.loginFragment_RegisterBtn).setOnClickListener(this)
        view.findViewById<Button>(R.id.loginBtn).setOnClickListener(this)

        email = view.findViewById(R.id.email_login)
        password = view.findViewById(R.id.password_Login)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.sign_in_button -> signIn()

            R.id.loginFragment_RegisterBtn ->{
                val action = LoginFragmentDirections.actionLoginFragmentToRegister();
                navController!!.navigate(action)
            }

            R.id.loginBtn -> {
                loginWithEmailAndPassword(email.text.toString(), password.text.toString());
            }
        }
    }

    private fun loginWithEmailAndPassword(email : String, password : String){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        val action = LoginFragmentDirections.actionLoginFragmentToMainFragment();
                        navController!!.navigate(action)
                    }
                    else{
                        Toast.makeText(context, resources.getString(R.string.failLogin), Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if(task.isSuccessful){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("SignInActivity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Toast.makeText(context, resources.getString(R.string.failLogin), Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(context, resources.getString(R.string.failLogin), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users")

                        ref.child(mAuth.currentUser?.uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                println("Erro Login singleValueEvent")
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                //User exists
                                if(snapshot.exists()){
                                    val action = LoginFragmentDirections.actionLoginFragmentToMainFragment();
                                    navController!!.navigate(action)
                                }
                                //User doesnt exist
                                else{
                                    val action = LoginFragmentDirections.actionLoginFragmentToRegister();
                                    navController!!.navigate(action)
                                }
                            }
                        })

                        //se fizer login fazer algo
                    } else {
                        Toast.makeText(context, resources.getString(R.string.failLogin), Toast.LENGTH_SHORT).show()
                    }
                }
    }


}

