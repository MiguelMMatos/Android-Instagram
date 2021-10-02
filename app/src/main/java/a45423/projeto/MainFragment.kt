package a45423.projeto

import a45423.projeto.GlobalData.ImageLoader
import a45423.projeto.GlobalData.PostLoader
import a45423.projeto.Login.LoginFragmentDirections
import a45423.projeto.Profile.ProfileFragmentDirections
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.marginLeft
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text


class MainFragment : Fragment(), View.OnClickListener {
    var navController : NavController? = null;
    lateinit var  storageRef : FirebaseStorage
    lateinit var mAuth : FirebaseAuth
    lateinit var databaseRef : FirebaseDatabase

    lateinit var currentView : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        databaseRef = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
        navController = Navigation.findNavController(view);

        currentView = view

        view.findViewById<ImageView>(R.id.goToMessage).setOnClickListener(this)

        loadFollowersPhotos()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.goToMessage -> {
                val action = MainFragmentDirections.actionMainFragmentToViewAllMessagesFragment();
                navController!!.navigate(action)
            }
        }

        if(v != null && v.getTag() != null){
            val action = MainFragmentDirections.actionMainFragmentToUsersProfileFragment(v.getTag().toString());
            navController!!.navigate(action)
        }
    }


    fun loadFollowersPhotos(){
        var referencePic = databaseRef.reference.child("Follow").child(mAuth.currentUser?.uid!!).child("following")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(snapshot: DataSnapshot) {
                var usersUID = ArrayList<String>()

                snapshot.children.forEach {
                    usersUID.add(it.key.toString())
                }

                loadUsersPosts(usersUID)
            }
        })
    }

    fun loadUsersPosts(uID : ArrayList<String>){
        var referencePic = databaseRef.reference.child("posts").orderByKey()

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(postSnapshot : DataSnapshot) {
                for(x in 0 .. postSnapshot.children.count() - 1){

                    var currentUID = postSnapshot.children.elementAt(x).child("uid").value
                    if( uID.contains( currentUID ) ){
                        var likeImage = 0
                        val numberOfLikes = postSnapshot.children.elementAt(x).child("likes").children.count()
                        val photoUrl = postSnapshot.children.elementAt(x).child("uri").value.toString();

                        if(postSnapshot.children.elementAt(x).child("likes").child(FirebaseAuth.getInstance().currentUser?.uid!!).value == true)
                            likeImage = R.drawable.hearth
                        else
                            likeImage = R.drawable.emptyheart


                        databaseRef.reference.child("users").child(currentUID.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(usersSnapshot: DataSnapshot) {

                                val profilePic = usersSnapshot.child("profilePic").value.toString()
                                val username = usersSnapshot.child("name").value.toString()
                                var commentsCount = 0

                                databaseRef.reference.child("Comments").child(postSnapshot.children.elementAt(x).key.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {
                                        println("Erro")
                                    }

                                    override fun onDataChange(commentSnapshot : DataSnapshot) {
                                        if(commentSnapshot.exists()){
                                            commentsCount = commentSnapshot.children.count()
                                        }

                                        println("ver -> " + postSnapshot.key.toString())
                                        val description = postSnapshot.child(postSnapshot.children.elementAt(x).key.toString()).child("description").value.toString()
                                        createSinglePost(
                                                username,
                                                profilePic,
                                                photoUrl,
                                                usersSnapshot.key.toString(),
                                                likeImage,
                                                numberOfLikes,
                                                postSnapshot.children.elementAt(x).key.toString(),
                                                commentsCount.toString(),
                                                description)
                                    }
                                })
                            }
                        })
                    }
                }
            }
        })
    }


    fun createSinglePost(username : String, profilePic : String, photo : String, userUID : String, likeImage : Int, numberOfLikes : Int, postID : String, commentsCount : String, description : String){

        //Create Main container
        var layoutParams_Vertical = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Vertical.setMargins(0, 0, 0, 150)

        var linearLayot_Vertical = LinearLayout(context)
        linearLayot_Vertical.layoutParams = layoutParams_Vertical
        linearLayot_Vertical.orientation = LinearLayout.VERTICAL

        //Create Top Container
        var layoutParams_Horizontal = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Horizontal.setMargins(0, 0, 0, 30)

        var linearLayot_Horizontal = LinearLayout(context)
        linearLayot_Horizontal.layoutParams = layoutParams_Horizontal
        linearLayot_Horizontal.orientation = LinearLayout.HORIZONTAL
        linearLayot_Horizontal.gravity = Gravity.CENTER_VERTICAL

        //Create Profile Pic
        var layoutParams_ProfileImage = LinearLayout.LayoutParams(150, 150)
        layoutParams_ProfileImage.setMargins(20, 0, 25, 0)
        var profileImage = CircleImageView(context)
        profileImage.layoutParams = layoutParams_ProfileImage

        var ref = storageRef.getReferenceFromUrl(profilePic)

        val ONE_MEGABYTE: Long = 1024 * 1024
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profileImage.setImageBitmap(bmp)
        }

        profileImage.setTag(userUID)
        profileImage.setOnClickListener(this)

        //Create name TextView
        var usernameText = TextView(context)
        usernameText.setText(username)
        usernameText.setTextColor(Color.WHITE)
        usernameText.textSize = 18F

        linearLayot_Horizontal.addView(profileImage)
        linearLayot_Horizontal.addView(usernameText)

        linearLayot_Vertical.addView(linearLayot_Horizontal)

        //Add main photo
        ref = storageRef.getReferenceFromUrl(photo)

        var layoutParams_Photo = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1600)

        val imageView = ImageView(context)

        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bmp)
        }
        imageView.layoutParams = layoutParams_Photo
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        linearLayot_Vertical.addView(imageView)

        //Likes and Comments Section
        var layoutParams_HorizontalLike = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_HorizontalLike.setMargins(0, 20, 0, 30)

        var linearLayot_HorizontalLikes = LinearLayout(context)
        linearLayot_HorizontalLikes.layoutParams = layoutParams_Horizontal
        linearLayot_HorizontalLikes.orientation = LinearLayout.HORIZONTAL
        linearLayot_HorizontalLikes.gravity = Gravity.CENTER_VERTICAL

        var layoutParams_Likes = LinearLayout.LayoutParams(70, 70)
        layoutParams_Likes.setMargins(20, 0, 20, 0)

        val likesImage = ImageView(context)
        likesImage.setImageResource(likeImage)
        likesImage.layoutParams = layoutParams_Likes
        
        val likesText = TextView(context)
        likesText.textSize = 18F;
        likesText.setTextColor(Color.WHITE)
        likesText.setText(numberOfLikes.toString())

        likesImage.setOnClickListener {
            updateLike(postID, likesImage, likesText)
        }


        linearLayot_HorizontalLikes.addView(likesImage)
        linearLayot_HorizontalLikes.addView(likesText)
        linearLayot_Vertical.addView(linearLayot_HorizontalLikes)

        //Description

        var descriptionText = TextView(context)

        var layoutParams_Description = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_HorizontalLike.setMargins(50, 0, 0, 0)

        descriptionText.setText(description)
        descriptionText.setTextColor(Color.WHITE)
        descriptionText.textSize = 18F
        descriptionText.layoutParams = layoutParams_Description;


        linearLayot_Vertical.addView(descriptionText)


        //Commets
        var commentsText = TextView(context)

        var layoutParams_CommentsText = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_HorizontalLike.setMargins(50, 0, 0, 0)

        commentsText.setText(resources.getString(R.string.viewAll) + " " + commentsCount + " " + resources.getString(R.string.commentsWord))
        commentsText.setTextColor(Color.GRAY)
        commentsText.textSize = 18F
        commentsText.layoutParams = layoutParams_CommentsText;

        commentsText.setOnClickListener{
            val action = MainFragmentDirections.actionMainFragmentToComentFragment(userUID, postID);
            navController!!.navigate(action)
        }

        linearLayot_Vertical.addView(commentsText)

        view?.findViewById<LinearLayout>(R.id.MainFrag_Images)?.addView(linearLayot_Vertical)
    }

    fun updateLike(postID : String, postLike : ImageView, textLikes : TextView){
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
                PostLoader().loadLikes(currentView, databaseRef, postID, postLike, textLikes)
            }
        })
    }

}



