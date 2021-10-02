package a45423.projeto.Comments

import a45423.projeto.MainFragmentDirections
import a45423.projeto.R
import a45423.projeto.register.NewUser
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class ComentFragment : Fragment(), View.OnClickListener {

    lateinit var addComent : EditText
    lateinit var image_profile : ImageView
    lateinit var post : TextView

    lateinit var postID : String
    lateinit var publisherID : String

    lateinit var firebaseUser : FirebaseUser
    lateinit var  storageRef : FirebaseStorage
    lateinit var currentView : View
    var navController : NavController? = null

    lateinit var currentUser : NewUser

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = ComentFragmentArgs.fromBundle(requireArguments());
            postID = args.postID
            publisherID = args.userID
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance()

        addComent = view.findViewById(R.id.add_coment)
        image_profile =  view.findViewById(R.id.image_profile)
        post = view.findViewById(R.id.post)
        post.setOnClickListener(this)

        navController = Navigation.findNavController(view)
        currentView = view

        LoadUserData()
        loadCommets()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.post -> {
                if(addComent.text.toString().equals("")){
                    Toast.makeText(context, R.string.cantSendPost.toString(), Toast.LENGTH_SHORT).show()
                }
                else
                    addComent();
            }
        }

        if(v != null && v.getTag() != null){
            val action = ComentFragmentDirections.actionComentFragmentToUsersProfileFragment(v.getTag().toString());
            navController!!.navigate(action)
        }
    }

    fun loadCommets(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Comments").child(postID)
        view?.findViewById<LinearLayout>(R.id.recycler_view_Comments)?.removeAllViewsInLayout()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                for(i in 0..snapshot.children.count() - 1){
                    val referenceSnap = snapshot.children.elementAt(i).key.toString()

                    loadSingleComment(
                            snapshot.child(referenceSnap).child("username").value.toString(),
                            snapshot.child(referenceSnap).child("profilePic").value.toString(),
                            snapshot.child(referenceSnap).child("UID").value.toString(),
                            postID,
                            snapshot.child(referenceSnap).child("comment").value.toString())
                }
            }
        })
    }

    fun addComent(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Comments").child(postID)

        val hashMap : HashMap<String, String> = HashMap();
        hashMap.put("comment", addComent.text.toString())
        hashMap.put("publisher", firebaseUser.uid)
        hashMap.put("profilePic", currentUser.profilePic.toString())
        hashMap.put("username", currentUser.username.toString())
        hashMap.put("UID", currentUser.UID.toString())

        ref.push().setValue(hashMap)
        addComent.setText("")

        loadCommets()
    }

    fun LoadUserData(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(firebaseUser.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val singleUser: HashMap<String, Any> = snapshot.value as HashMap<String, Any>
                currentUser = NewUser(singleUser["name"] as String,singleUser["email"] as String, singleUser["password"] as String, singleUser["username"] as String,singleUser["region"] as String
                        ,singleUser["age"] as String, singleUser["profilePic"] as String, snapshot.key)

                Glide.with(currentView).load(currentUser.profilePic).into(image_profile)

            }
        })
    }

    fun loadSingleComment(username : String, profilePic : String, userUID : String, postID : String, comment : String){
        println("Profile -> " + profilePic)
        //Create Main container
        var layoutParams_Vertical = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Vertical.setMargins(0, 0, 0, 0)

        var linearLayot_Vertical = LinearLayout(currentView.context)
        linearLayot_Vertical.layoutParams = layoutParams_Vertical
        linearLayot_Vertical.orientation = LinearLayout.VERTICAL

        //Create Top Container
        var layoutParams_Horizontal = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Horizontal.setMargins(0, 0, 0, 170)

        var linearLayot_Horizontal = LinearLayout(currentView.context)
        linearLayot_Horizontal.layoutParams = layoutParams_Horizontal
        linearLayot_Horizontal.orientation = LinearLayout.HORIZONTAL

        //Create Profile Pic
        var layoutParams_ProfileImage = LinearLayout.LayoutParams(150, 150)
        layoutParams_ProfileImage.setMargins(20, 0, 25, 0)
        var profileImage = CircleImageView(currentView.context)
        profileImage.layoutParams = layoutParams_ProfileImage

        var ref = storageRef.getReferenceFromUrl(profilePic)

        val ONE_MEGABYTE: Long = 1024 * 1024
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profileImage.setImageBitmap(bmp)
        }

        profileImage.setTag(userUID)
        profileImage.setOnClickListener(this)

        linearLayot_Horizontal.addView(profileImage)

        //Create name TextView
        var usernameText = TextView(currentView.context)
        usernameText.setText(username)
        usernameText.setTextColor(Color.WHITE)
        usernameText.textSize = 20F

        //Commets
        var commentsText = TextView(currentView.context)

        var layoutParams_HorizontalLike = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_HorizontalLike.setMargins(0, 20, 0, 30)

        var layoutParams_CommentsText = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_HorizontalLike.setMargins(50, 0, 0, 0)

        commentsText.setText(comment)
        commentsText.setTextColor(Color.WHITE)
        commentsText.textSize = 16F
        commentsText.layoutParams = layoutParams_CommentsText;

        commentsText.setOnClickListener{
            val action = MainFragmentDirections.actionMainFragmentToComentFragment(userUID, postID);
            navController!!.navigate(action)
        }


        linearLayot_Vertical.addView(usernameText)
        linearLayot_Vertical.addView(commentsText)

        linearLayot_Horizontal.addView(linearLayot_Vertical)

        view?.findViewById<LinearLayout>(R.id.recycler_view_Comments)?.addView(linearLayot_Horizontal)
    }
}