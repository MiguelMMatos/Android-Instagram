package a45423.projeto.Adapter

import a45423.projeto.Login.LoginFragmentDirections
import a45423.projeto.Profile.FollowFragmentArgs
import a45423.projeto.Profile.FollowFragmentDirections
import a45423.projeto.R
import a45423.projeto.Search.SearchFragmentDirections
import a45423.projeto.register.NewUser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (private var mContext: Context,
                   private var mUser: List<NewUser>,
                   private var isSearchFragment : Boolean,
                   private var view : View) : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = mUser[position]
        viewHolder.userNameTextView.text =user.username
        viewHolder.fullnameTextView.text =user.name
        viewHolder.followButton.visibility = View.VISIBLE
        Picasso.get().load(user.profilePic).placeholder(R.drawable.coment).into(viewHolder.image_profileTextView)
        isFollowing(user.UID.toString(), viewHolder.followButton)

        if (user.UID == FirebaseAuth.getInstance().currentUser?.uid ) {
            viewHolder.followButton.visibility = View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            var action_Follow = FollowFragmentDirections.actionFollowFragmentToUsersProfileFragment(user.UID.toString())
            var action_Search = SearchFragmentDirections.actionSearchFragmentToUsersProfileFragment(user.UID.toString());

            if( isSearchFragment)
                Navigation.findNavController(view).navigate(action_Search)
            else
                Navigation.findNavController(view).navigate(action_Follow)
        }
        viewHolder.followButton.setOnClickListener {
            if (viewHolder.followButton.text.toString() == "Follow") {
                FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                    FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference.child(
                        "Follow"
                    ).child(it1)
                        .child("following").child(user.UID.toString()).setValue(true)
                }
                FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                    FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference.child(
                        "Follow"
                    ).child(user.UID.toString())
                        .child("followers").child(it1).setValue(true)
                }
            } else {
                FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                    FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference.child(
                        "Follow"
                    ).child(it1)
                        .child("following").child(user.UID.toString()).removeValue()
                }
                FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                    FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference.child(
                        "Follow"
                    ).child(user.UID.toString())
                        .child("followers").child(it1).removeValue()
                }
            }

        }
    }


    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var userNameTextView: TextView = itemView.findViewById(R.id.username)
        var fullnameTextView: TextView = itemView.findViewById(R.id.fullname)
        var image_profileTextView: CircleImageView = itemView.findViewById(R.id.image_profile)
        var followButton: Button = itemView.findViewById(R.id.btn_follow)
    }

    private fun isFollowing(userid: String, button: Button) {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val reference = firebaseUser?.let {
            FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app").reference
                .child("Follow").child(it.uid).child("following")
        }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(userid).exists()) {
                    button.text = "Following"
                } else {
                    button.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}