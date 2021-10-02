package a45423.projeto.Profile

import a45423.projeto.GlobalData.ImageLoader
import a45423.projeto.GlobalData.UserDataLoader
import a45423.projeto.R
import a45423.projeto.register.ui.RegisterEmailFragmentArgs
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class UsersProfileFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth : FirebaseAuth
    lateinit var databaseRef : FirebaseDatabase
    lateinit var  storageRef : FirebaseStorage
    val userDataLoader = UserDataLoader()
    var navController : NavController? = null
    lateinit var gridView : androidx.gridlayout.widget.GridLayout

    lateinit var postText : TextView
    lateinit var matchesText : TextView
    lateinit var followersText : TextView
    lateinit var followedText : TextView

    lateinit var userUID : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        databaseRef = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
        navController = Navigation.findNavController(view)

        gridView = view?.findViewById(R.id.ContainerPosts)

        postText = view.findViewById(R.id.postValue)
        followersText = view.findViewById(R.id.followersValue)
        followedText = view.findViewById(R.id.followedValue)

        view.findViewById<Button>(R.id.send_message).setOnClickListener(this)

        if(arguments != null){
            val args = UsersProfileFragmentArgs.fromBundle(requireArguments());
            userUID = args.userID
        }

        loadUserStats(userUID)
        loadUserData(view, userUID)
        loadUserPosts(userUID)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.send_message -> {
                val action = UsersProfileFragmentDirections.actionUsersProfileFragmentToMessagesFragment(userUID)
                navController!!.navigate(action)
            }
        }

        if(v != null && v.getTag() != null){
            val action = UsersProfileFragmentDirections.actionUsersProfileFragmentToShowPost(userUID, v.getTag().toString())
            navController!!.navigate(action)
        }
    }

    fun loadUserStats(userUID : String){
        var referencePic = databaseRef.reference.child("users").child(userUID).child("followers")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists())
                    followersText.setText(snapshot.children.count().toString())
            }
        })

        referencePic = databaseRef.reference.child("users").child(userUID).child("following")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists())
                    followedText.setText(snapshot.children.count().toString())
            }
        })
    }

    fun loadUserPosts(userUID : String){
        val referencePic = databaseRef.reference.child("users").child(userUID).child("user-posts")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(snapshot: DataSnapshot) {

                postText.setText(snapshot.children.count().toString())

                snapshot.children.forEach {

                    databaseRef.reference.child("posts").child(it.key.toString()).get().addOnSuccessListener {
                        val keyId = it.key.toString()
                        val url = it.child("uri").value.toString()

                        createNewPosts((keyId + ";" + url))
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun createNewPosts(uriList : String){
        val trim = uriList.split(";")
        val keyId = trim[0]
        val url = trim[1]

        val ref = storageRef.getReferenceFromUrl(url)

        val ONE_MEGABYTE: Long = 1024 * 1024
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val resize = Bitmap.createScaledBitmap(bmp, gridView.measuredWidth.div(3), gridView.measuredWidth.div(3), true)
            loadSingleImage(resize, keyId)

        }.addOnFailureListener {
            // Handle any errors
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun loadSingleImage(bmp : Bitmap, keyId : String){
        val width = gridView?.measuredWidth?.div(3)

        var newImageView = ImageView(context)
        var layoutParams = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f))

        if (width != null)
            layoutParams.width = width

        newImageView.layoutParams = layoutParams

        newImageView.setTag(keyId)
        newImageView.setImageBitmap(bmp)
        newImageView.setOnClickListener(this)

        if (gridView != null)
            gridView.addView(newImageView)
    }


    fun loadUserData(view : View, userUID : String){
        val referencePic = databaseRef.reference.child("users").child(userUID)

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists()){
                    userDataLoader.insertData(
                            snapshot.child("age").value.toString(),
                            snapshot.child("email").value.toString(),
                            snapshot.child("name").value.toString(),
                            snapshot.child("password").value.toString(),
                            snapshot.child("profilePic").value.toString(),
                            snapshot.child("region").value.toString(),
                            snapshot.child("username").value.toString()

                    )
                    initProfile(view)
                }
            }
        })
    }

    fun initProfile(view : View){
        val referencePic = databaseRef.reference.child("users").child(userUID).child("profilePic")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists()){

                    ImageLoader().loadImage(snapshot.value.toString(), view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.postProfileImage), storageRef)
                    view.findViewById<TextView>(R.id.NameAndAge_Info).setText(userDataLoader.name + ", " + userDataLoader.age)
                    view.findViewById<TextView>(R.id.Region_Info).setText(userDataLoader.region)
                }
            }
        })
    }

}