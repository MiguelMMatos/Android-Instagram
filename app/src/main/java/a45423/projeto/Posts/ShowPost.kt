package a45423.projeto.Posts

import a45423.projeto.GlobalData.PostLoader
import a45423.projeto.Message.ViewAllMessagesFragmentDirections
import a45423.projeto.R
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ShowPost : Fragment(), View.OnClickListener {
    var navController : NavController? = null
    lateinit var databaseRef : FirebaseDatabase
    lateinit var  storageRef : FirebaseStorage
    lateinit var firebaseUser : FirebaseUser

    lateinit var userUID : String
    lateinit var postID : String

    lateinit var currentView : View

    lateinit var imageView : ImageView
    lateinit var descriptionView : TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        storageRef = FirebaseStorage.getInstance()
        databaseRef = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        imageView = view.findViewById(R.id.postImage)

        this.currentView = view

        view.findViewById<ImageView>(R.id.likeImage_ShowPost).setOnClickListener(this)
        view.findViewById<TextView>(R.id.commentText_Post).setOnClickListener(this)
        descriptionView = view.findViewById(R.id.description_text)

        if(arguments != null){
            val args = ShowPostArgs.fromBundle(requireArguments());
            userUID = args.userUID
            postID = args.postID;
        }

        loadPost(view)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.likeImage_ShowPost -> {
                updateLike()
            }

            R.id.commentText_Post -> {
                val action = ShowPostDirections.actionShowPostToComentFragment(userUID,  postID);
                navController!!.navigate(action)
            }
        }
    }

    fun updateLike(){
        val referencePic = databaseRef.reference.child("posts").child(postID).child("likes")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    databaseRef.reference.child("posts").child(postID).child("likes")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).removeValue()
                }
                else{
                    databaseRef.reference.child("posts").child(postID).child("likes")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(true)
                }
                PostLoader().loadLikes(currentView, databaseRef, postID, R.id.postLikes)
            }
        })
    }

    fun loadPost(view: View){
        val referencePic = databaseRef.reference.child("posts").child(postID)

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val description = snapshot.child("description").value.toString()
                    descriptionView.setText(description)
                    val url = snapshot.child("uri").value.toString()
                    PostLoader().loadPost(view, databaseRef, userUID, postID,  url, storageRef, R.id.postImage, R.id.postLikes, R.id.postUsername, R.id.postProfileImage, R.id.commentText_Post)
                }
            }
        })
    }



}