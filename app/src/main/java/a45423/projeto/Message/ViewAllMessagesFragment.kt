package a45423.projeto.Message

import a45423.projeto.Comments.ComentFragmentDirections
import a45423.projeto.R
import a45423.projeto.register.NewUser
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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


class ViewAllMessagesFragment : Fragment(), View.OnClickListener {

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
        return inflater.inflate(R.layout.fragment_view_all_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance()

        navController = Navigation.findNavController(view)
        currentView = view

        loadChat()
    }

    override fun onClick(v: View?) {
        if(v != null && v.getTag() != null){
            val action = ComentFragmentDirections.actionComentFragmentToUsersProfileFragment(v.getTag().toString());
            navController!!.navigate(action)
        }
    }


    fun loadChat(){
        val ref = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("Messages")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                println("Erro Login singleValueEvent")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                println("Whta ? -> " + snapshot.value)

                for(i in 0..snapshot.children.count() - 1){
                    var referenceUser = ""

                    if( snapshot.children.elementAt(i).child("receiver").value == firebaseUser.uid ){
                        referenceUser = snapshot.children.elementAt(i).child("sender").value.toString()
                    }
                    else if(snapshot.children.elementAt(i).child("sender").value == firebaseUser.uid){
                        referenceUser = snapshot.children.elementAt(i).child("receiver").value.toString()
                    }

                    if(referenceUser != ""){
                        val getUser = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(referenceUser)
                        getUser.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(userSnap: DataSnapshot) {

                                if(userSnap.exists()){
                                    loadSingleChat(
                                            userSnap.child("profilePic").value.toString(),
                                            userSnap.child("username").value.toString(),
                                            userSnap.key.toString())
                                }
                            }

                        })
                    }
                }
            }
        })
    }


    fun loadSingleChat(profilePic : String, username : String, receiverID : String){

        //Create Top Container
        var layoutParams_Horizontal = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams_Horizontal.setMargins(0, 0, 0, 170)

        var linearLayot_Horizontal = LinearLayout(context)
        linearLayot_Horizontal.layoutParams = layoutParams_Horizontal
        linearLayot_Horizontal.orientation = LinearLayout.HORIZONTAL
        linearLayot_Horizontal.gravity = Gravity.CENTER_VERTICAL
        linearLayot_Horizontal.gravity = Gravity.CENTER_HORIZONTAL

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

        commentsText.setText(username)
        commentsText.setTextColor(Color.WHITE)
        commentsText.textSize = 16F
        commentsText.layoutParams = layoutParams_CommentsText;

        linearLayot_Horizontal.addView(profileImage)
        linearLayot_Horizontal.addView(commentsText)

        view?.findViewById<LinearLayout>(R.id.recycler_view_ViewMessages)?.addView(linearLayot_Horizontal)

        linearLayot_Horizontal.setOnClickListener {
            val action = ViewAllMessagesFragmentDirections.actionViewAllMessagesFragmentToMessagesFragment(receiverID);
            navController!!.navigate(action)
        }
    }

}