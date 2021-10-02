package a45423.projeto.register.ui

import a45423.projeto.GlobalData.UtilFuctions
import a45423.projeto.R
import a45423.projeto.register.NewUser
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.concurrent.schedule


class RegisterProfilePic : Fragment(), View.OnClickListener  {
    var navController : NavController? = null;
    lateinit var auth : FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    lateinit var mStorageRefence : StorageReference
    lateinit var mDatabaseReference: DatabaseReference

    lateinit var progressBar : ProgressBar

    var username = "";
    var password = "";
    var age = "";
    var name = "";
    var email = "";
    var region = "";
    var profilePicUri : Uri? = null;

    lateinit var buttonCreateUser : Button

    lateinit var profilePicImageView : ImageView;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_profile_pic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().getReference()
        navController = Navigation.findNavController(view)

        //Storage Firebase
        mStorageRefence = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseReference = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("uploads")

        progressBar = view.findViewById(R.id.progressBar_profilePic)

        //Button submeter
        buttonCreateUser = view.findViewById(R.id.registerProfilePic_Btn)
        buttonCreateUser.setOnClickListener(this)
        UtilFuctions().disableButton(buttonCreateUser, this.requireContext())

        //Image view
        profilePicImageView = view.findViewById(R.id.newPostPic_Selector)
        profilePicImageView.setOnClickListener(this)

        if(arguments != null){
            val args = RegisterProfilePicArgs.fromBundle(requireArguments());
            username = args.username;
            password = args.password;
            age = args.age;
            name = args.name;
            email = args.email
            region = args.region;
        }
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.newPostPic_Selector -> {
                openFileChooser()
            }
            R.id.registerProfilePic_Btn -> {
                uploadImage()
            }
        }
    }

    fun uploadImage(){
        if(profilePicUri != null){
            val fileReference = mStorageRefence.child(System.currentTimeMillis().toString() + "." + UtilFuctions().getFileExtension(profilePicUri!!, this.requireContext()))

            val uploadTask = fileReference.putFile(profilePicUri!!)

            //ONSUCESS
            uploadTask.addOnSuccessListener { taskSnapshot ->
                Timer("ProgressBarDelay", false).schedule(500){
                    progressBar.setProgress(0)
                }
                Toast.makeText(context, "Upload sucessuful", Toast.LENGTH_LONG).show()
            }

            //ONFAILURE
            uploadTask.addOnFailureListener{
                Toast.makeText(context, "Failed to load Image", Toast.LENGTH_SHORT).show()
            }

            //ON PROGRESS
            uploadTask.addOnProgressListener { snapshot ->
                var progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressBar.setProgress(progress.toInt())
            }

            //ONCOMPLETE
            uploadTask.addOnCompleteListener{ taskUpload ->
                if ( taskUpload.isSuccessful){
                    //authWithEmailAndPassword
                    if(auth.currentUser == null){
                        auth.createUserWithEmailAndPassword(email, password ).addOnCompleteListener{ task: Task<AuthResult> ->
                                    if (task.isSuccessful) {
                                        fileReference.downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
                                            override fun onSuccess(uri: Uri?) {
                                                createUser(uri.toString())
                                            }
                                        })
                                    }
                                    else{
                                        println("createUserWithEmail:failure   " +  task.exception)
                                    }
                                }
                    }else{
                        fileReference.downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
                            override fun onSuccess(uri: Uri?) {
                                createUser(uri.toString())
                            }
                        })
                    }
                }
            }
        }
        else
            Toast.makeText(context, "No file seleted", Toast.LENGTH_SHORT).show()
    }

    fun createUser(uri : String){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users")

        auth.currentUser?.uid?.let {
            val user = NewUser(name, email, password, username.toLowerCase() , region , age, uri, auth.uid.toString())

            ref.child(it).setValue(user).addOnCompleteListener {
                val action = RegisterProfilePicDirections.actionRegisterProfilePicToMainFragment();
                navController!!.navigate(action)
             }
        }
    }


    fun openFileChooser(){
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if( requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null ) {
            profilePicUri = data.data
            profilePicImageView.setImageURI(profilePicUri)
            UtilFuctions().enableButton(buttonCreateUser, this.requireContext())
        }
    }

}