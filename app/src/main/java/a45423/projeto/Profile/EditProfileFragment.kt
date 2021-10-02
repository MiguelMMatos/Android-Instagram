package a45423.projeto.Profile

import a45423.projeto.GlobalData.UserDataLoader
import a45423.projeto.R
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class EditProfileFragment : Fragment(), View.OnClickListener {

    lateinit var mAuth : FirebaseAuth
    lateinit var databaseRef : FirebaseDatabase
    lateinit var  storageRef : FirebaseStorage
    var navController : NavController? = null

    lateinit var usernameTextView : EditText
    lateinit var nameTextView : EditText
    lateinit var regionTextView : EditText

    lateinit var username : String
    lateinit var name : String
    lateinit var region : String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        databaseRef = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
        navController = Navigation.findNavController(view)

        usernameTextView = view.findViewById(R.id.usernameUpdate)
        nameTextView = view.findViewById(R.id.nameUpdate)
        regionTextView = view.findViewById(R.id.regionUpdate)

        view.findViewById<Button>(R.id.updateBtn).setOnClickListener(this)

        if(arguments != null){
            val args = EditProfileFragmentArgs.fromBundle(requireArguments());
            username = args.username
            name = args.name
            region = args.region
        }

        setTagOnEditText()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.updateBtn -> {
                if(usernameTextView.text.toString().length == 0 )
                    Toast.makeText(context, resources.getString(R.string.usernameWarning), Toast.LENGTH_SHORT).show()
                else if(nameTextView.text.toString().length == 0)
                    Toast.makeText(context, resources.getString(R.string.nameWarning), Toast.LENGTH_SHORT).show()
                else if(regionTextView.text.toString().length == 0)
                    Toast.makeText(context, resources.getString(R.string.regionWarning), Toast.LENGTH_SHORT).show()
                else{
                    updateUserData()
                }
            }
        }
    }

    fun setTagOnEditText(){
        usernameTextView.setText(username)
        nameTextView.setText(name)
        regionTextView.setText(region)
    }

    fun updateUserData(){
        databaseRef.reference.child("users").child(mAuth.uid.toString()).child("name").setValue(nameTextView.text.toString())
        databaseRef.reference.child("users").child(mAuth.uid.toString()).child("username").setValue(usernameTextView.text.toString())
        databaseRef.reference.child("users").child(mAuth.uid.toString()).child("region").setValue(regionTextView.text.toString())
        redirect()
    }

    fun redirect(){
        val action = EditProfileFragmentDirections.actionEditProfileFragmentToProfileFragment()
        navController!!.navigate(action)
    }

}