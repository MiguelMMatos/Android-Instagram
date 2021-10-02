package a45423.projeto.Posts

import a45423.projeto.R
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddPostDescription_Fragment : Fragment(), View.OnClickListener {
    lateinit var auth : FirebaseAuth
    var navController : NavController? = null

    lateinit var uri : String
    lateinit var descriptionView : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post_description_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)

        if(arguments != null){
            val args = AddPostDescription_FragmentArgs.fromBundle(requireArguments());
            uri = args.uri
        }

        descriptionView = view.findViewById(R.id.description_value)

        view.findViewById<Button>(R.id.newPostNext_Button).setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.newPostNext_Button -> {
                if(descriptionView.text.length > 0)
                    createPost()
                else{
                    descriptionView.setError("Insert a description!!")
                    descriptionView.requestFocus()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(){
        var ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("posts")
        val post = NewPost(uri, descriptionView.text.toString() , auth.currentUser?.uid!!)

        val postId = System.currentTimeMillis().toString();

        ref.child(postId).setValue(post).addOnCompleteListener {
            ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users")

            auth.currentUser?.uid?.let {
                ref.child(it).child("user-posts").child(postId).setValue(NewUserPost(true)).addOnCompleteListener {
                    val action = AddPostDescription_FragmentDirections.actionAddPostFragmentToProfileFragment();
                    navController!!.navigate(action)
                }
            }
        }
    }

}