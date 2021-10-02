package a45423.projeto.Message

import a45423.projeto.Comments.ComentFragmentDirections
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


class MessagesFragment : Fragment(), View.OnClickListener {
    lateinit var addMessage : EditText
    lateinit var image_profile : ImageView
    lateinit var send : TextView

    lateinit var receiverID : String

    lateinit var firebaseUser : FirebaseUser
    lateinit var  storageRef : FirebaseStorage
    lateinit var currentView : View
    var navController : NavController? = null

    var user: NewUser? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            val args = MessagesFragmentArgs.fromBundle(requireArguments());
            receiverID = args.receiverID
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance()

        addMessage = view.findViewById(R.id.add_message)
        image_profile =  view.findViewById(R.id.image_profile)
        send = view.findViewById(R.id.send)
        send.setOnClickListener(this)

        navController = Navigation.findNavController(view)
        currentView = view

        loadData()
        loadMessages()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.send -> {
                if(addMessage.text.toString().equals("")){
                    Toast.makeText(context, R.string.cantSendPost.toString(), Toast.LENGTH_SHORT).show()
                }
                else
                    addMessage()
            }
        }

        if(v != null && v.getTag() != null){
            val action = ComentFragmentDirections.actionComentFragmentToUsersProfileFragment(v.getTag().toString());
            navController!!.navigate(action)
        }
    }

    fun loadMessages(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Messages")
        view?.findViewById<LinearLayout>(R.id.recycler_view_Comments)?.removeAllViewsInLayout()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                for(i in 0..snapshot.children.count() - 1){
                    if( snapshot.children.elementAt(i).child("receiver").value == receiverID || snapshot.children.elementAt(i).child("sender").value == receiverID){
                        val ref_messages = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Messages").child(snapshot.children.elementAt(i).key.toString()).child("chat").orderByKey()

                        ref_messages.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                println("Erro Login singleValueEvent")
                            }

                            override fun onDataChange(snapshotCommet: DataSnapshot) {

                                view?.findViewById<LinearLayout>(R.id.recycler_view_Messages)?.removeAllViewsInLayout()
                                snapshotCommet.children.forEach {
                                    loadSingleMessage(
                                            it.child("comment").value.toString(),
                                            it.child("profilePic").value.toString(),
                                            it.child("publisher").value.toString())
                                }

                            }
                        })
                    }
                }
            }
        })
    }

    fun addMessage(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Messages")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var find = false;
                for(i in 0..snapshot.children.count() - 1){
                    if( (snapshot.children.elementAt(i).child("receiver").value == receiverID && snapshot.children.elementAt(i).child("sender").value == firebaseUser.uid)
                            || snapshot.children.elementAt(i).child("sender").value == receiverID && snapshot.children.elementAt(i).child("receiver").value == firebaseUser.uid){
                        find = true;
                        val ref_messages = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Messages").child(snapshot.children.elementAt(i).key.toString()).child("chat")

                        val hashMap : HashMap<String, String> = HashMap();
                        hashMap.put("comment", addMessage.text.toString())
                        hashMap.put("publisher", firebaseUser.uid)
                        hashMap.put("profilePic", user?.profilePic.toString())

                        ref_messages.child(System.currentTimeMillis().toString()).setValue(hashMap)
                        addMessage.setText("")
                    }
                }

                if(!find){
                    val hashMap : HashMap<String, String> = HashMap();
                    hashMap.put("receiver", receiverID)
                    hashMap.put("sender", firebaseUser.uid)
                    ref.push().setValue(hashMap)
                    addMessage()
                }
            }
        })
    }


    fun loadData(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(firebaseUser.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val singleUser: HashMap<String, Any> = snapshot.value as HashMap<String, Any>
                user = NewUser(singleUser["name"] as String,singleUser["email"] as String, singleUser["password"] as String, singleUser["username"] as String,singleUser["region"] as String
                    ,singleUser["age"] as String, singleUser["profilePic"] as String, snapshot.key)

                Glide.with(currentView).load(user?.profilePic).into(image_profile)
            }
        })
    }

    fun loadSingleMessage(comment : String, profilePic : String, pusblisher : String){

        //Create Top Container
        var layoutParams_Horizontal = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Horizontal.setMargins(0, 0, 0, 170)

        var linearLayot_Horizontal = LinearLayout(context)
        linearLayot_Horizontal.layoutParams = layoutParams_Horizontal
        linearLayot_Horizontal.orientation = LinearLayout.HORIZONTAL
        linearLayot_Horizontal.gravity = Gravity.CENTER_VERTICAL

        if(pusblisher == firebaseUser.uid)
            linearLayot_Horizontal.gravity = Gravity.END
        else
            linearLayot_Horizontal.gravity = Gravity.START

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

        //Message
        var commentsText = TextView(context)

        var layoutParams_CommentsText = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_CommentsText.gravity = Gravity.CENTER_VERTICAL

        commentsText.setText(comment)
        commentsText.setTextColor(Color.WHITE)
        commentsText.textSize = 16F
        commentsText.layoutParams = layoutParams_CommentsText;

        if(pusblisher == firebaseUser.uid){
            linearLayot_Horizontal.addView(commentsText)
            linearLayot_Horizontal.addView(profileImage)
        }
        else{
            linearLayot_Horizontal.addView(profileImage)
            linearLayot_Horizontal.addView(commentsText)
        }

        view?.findViewById<LinearLayout>(R.id.recycler_view_Messages)?.addView(linearLayot_Horizontal)
    }
}