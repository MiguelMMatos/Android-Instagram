package a45423.projeto.Profile

import a45423.projeto.Adapter.UserAdapter
import a45423.projeto.Posts.ShowPostArgs
import a45423.projeto.R
import a45423.projeto.register.NewUser
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FollowFragment : Fragment() {
    lateinit var recyclerView : RecyclerView
    lateinit var userAdapter: UserAdapter

    lateinit var mUsers : ArrayList<NewUser>

    lateinit var follow : String
    lateinit var userUID : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view : View = inflater.inflate(R.layout.fragment_follow, container, false)

        recyclerView  = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.visibility = View.VISIBLE

        mUsers = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUsers, false, view) }!!
        recyclerView.adapter = userAdapter

        if(arguments != null){
            val args = FollowFragmentArgs.fromBundle(requireArguments());
            follow = args.follow
            userUID = args.userUID
        }
        
        view.findViewById<TextView>(R.id.followText).setText(follow)

        readUsers()

        return view;
    }


    fun readUsers() {
        val reference =
            FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference().child("Follow").child(userUID).child(follow)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers.clear()

                snapshot.children.forEach {
                    loadSingleUser( it.key.toString())
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun loadSingleUser(loadUID : String){
        val reference =
            FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference()
                .child("users")
                .child(loadUID)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val singleUser: HashMap<String, Any> = snapshot.value as HashMap<String, Any>

                val user = NewUser(singleUser["name"] as String,singleUser["email"] as String, singleUser["password"] as String, singleUser["username"] as String,singleUser["region"] as String
                    ,singleUser["age"] as String, singleUser["profilePic"] as String,  loadUID)


                if (user != null && (user.UID  != FirebaseAuth.getInstance().currentUser?.uid)) {
                    mUsers.add(user)
                    println("mUsers Size " + mUsers.size)
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}