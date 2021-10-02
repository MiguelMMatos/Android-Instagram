package a45423.projeto.Search

import a45423.projeto.Adapter.UserAdapter
import a45423.projeto.R
import a45423.projeto.register.NewUser
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SearchFragment : Fragment() {
    lateinit var recyclerView : RecyclerView
    lateinit var userAdapter: UserAdapter

    lateinit var mUsers : ArrayList<NewUser>

    lateinit var search_bar : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view : View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView  = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.visibility = View.VISIBLE

        search_bar = view.findViewById(R.id.search_bar)

        mUsers = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUsers, true, view) }!!
        recyclerView.adapter = userAdapter

        readUsers()
        searchUsers("")

        search_bar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString().toLowerCase())
            }
        })

        return view;
    }

    fun searchUsers(s : String){
        var query : Query =  FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference
            .child("users").orderByChild("username")
            .startAt(s).endAt(s+"\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers.clear()

                snapshot.children.forEach {

                    val singleUser: HashMap<String, Any> = it.value as HashMap<String, Any>

                    val user = NewUser(singleUser["name"] as String,singleUser["email"] as String, singleUser["password"] as String, singleUser["username"] as String,singleUser["region"] as String
                        ,singleUser["age"] as String, singleUser["profilePic"] as String, it.key)


                    if (user != null && (user.UID  != FirebaseAuth.getInstance().currentUser?.uid)) {
                        mUsers.add(user)
                    }
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun readUsers() {
        val reference =
            FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference().child("users")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (search_bar.text.toString().equals("")) {
                    mUsers.clear()
                    snapshot.children.forEach {

                        val singleUser: HashMap<String, Any> = it.value as HashMap<String, Any>

                        val user = NewUser(singleUser["name"] as String,singleUser["email"] as String, singleUser["password"] as String, singleUser["username"] as String,singleUser["region"] as String
                            ,singleUser["age"] as String, singleUser["profilePic"] as String,  it.key)


                        if (user != null && (user.UID  != FirebaseAuth.getInstance().currentUser?.uid)) {
                            mUsers.add(user)
                        }
                    }

                    userAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}