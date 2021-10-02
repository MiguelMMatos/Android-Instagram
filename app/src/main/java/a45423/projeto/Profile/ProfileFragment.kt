package a45423.projeto.Profile

import a45423.projeto.GlobalData.ImageLoader
import a45423.projeto.GlobalData.UserDataLoader
import a45423.projeto.R
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
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
class ProfileFragment : Fragment(), View.OnClickListener {
    lateinit var mAuth : FirebaseAuth
    lateinit var databaseRef : FirebaseDatabase
    lateinit var  storageRef : FirebaseStorage
    val userDataLoader = UserDataLoader()
    var navController : NavController? = null
    lateinit var gridView : androidx.gridlayout.widget.GridLayout

    lateinit var postText : TextView
    lateinit var followersText : TextView
    lateinit var followedText : TextView

    lateinit var username : String
    lateinit var name : String
    lateinit var region : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
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


        view.findViewById<Button>(R.id.editProfileButton).setOnClickListener(this)

        view.findViewById<LinearLayout>(R.id.Followed).setOnClickListener(this)
        view.findViewById<LinearLayout>(R.id.Followers).setOnClickListener(this)

        inicializeProfile(view)
    }

    fun inicializeProfile(view : View){
        getFollowersOrFollowing("followers", followersText)
        getFollowersOrFollowing("following", followedText)
        loadUserStats()
        loadUserData(view)
        loadUserPosts()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.Followers -> {
                val action = ProfileFragmentDirections.actionProfileFragmentToFollowFragment("followers", mAuth.currentUser?.uid!!)
                navController!!.navigate(action)
            }

            R.id.Followed -> {
                val action = ProfileFragmentDirections.actionProfileFragmentToFollowFragment("following", mAuth.currentUser?.uid!!)
                navController!!.navigate(action)
            }

            R.id.editProfileButton -> {
                val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(username, name, region)
                navController!!.navigate(action)
            }
        }

        if(v != null && v.getTag() != null){
            val action = ProfileFragmentDirections.actionProfileFragmentToShowPost(mAuth.currentUser?.uid!!, v.getTag().toString())
            navController!!.navigate(action)
        }
    }

    fun getFollowersOrFollowing(child : String, text : TextView){
        val reference = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference
            .child("Follow")
            .child(mAuth.currentUser?.uid!!)
            .child(child)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                    text.setText( snapshot.children.count().toString() )
            }
        })
    }

    fun loadUserStats(){
        var referencePic = databaseRef.reference.child("users").child(mAuth.currentUser?.uid.toString()).child("followers")

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

        referencePic = databaseRef.reference.child("users").child(mAuth.currentUser?.uid.toString()).child("following")

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

    fun loadUserPosts(){
        val referencePic = databaseRef.reference.child("users").child(mAuth.currentUser?.uid.toString()).child("user-posts")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onDataChange(snapshot: DataSnapshot) {

                postText.setText(snapshot.children.count().toString())
                addNewPost()

                snapshot.children.forEach {

                    println(it.key.toString())
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

    @SuppressLint("ResourceType", "UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun addNewPost(){
        val width = gridView?.measuredWidth?.div(3)

        var newImageView = ImageView(context)
        var layoutParams = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f))

        if (width != null)
            layoutParams.width = width

        newImageView.layoutParams = layoutParams

        val image = BitmapFactory.decodeResource(resources, R.drawable.plus)
        val resize = Bitmap.createScaledBitmap(image, gridView.measuredWidth.div(3), gridView.measuredWidth.div(3), true)

        newImageView.setImageBitmap(resize)
        
        newImageView.setOnClickListener{
            val action = ProfileFragmentDirections.actionProfileFragmentToAddPostFragment()
            navController!!.navigate(action)
        }

        if (gridView != null)
            gridView.addView(newImageView)
    }


    fun loadUserData(view : View,){
        val referencePic = databaseRef.reference.child("users").child(mAuth.currentUser?.uid.toString())

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

                    username = snapshot.child("username").value.toString()
                    region = snapshot.child("region").value.toString()
                    name = snapshot.child("name").value.toString()
                    initProfile(view)
                }
            }
        })
    }

    fun initProfile(view : View){
        ImageLoader().loadProfileImage(view, databaseRef, mAuth.currentUser?.uid.toString(), storageRef, R.id.postProfileImage)

        view.findViewById<TextView>(R.id.NameAndAge_Info).setText(userDataLoader.name + ", " + userDataLoader.age)
        view.findViewById<TextView>(R.id.Region_Info).setText(userDataLoader.region)
    }

}