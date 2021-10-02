package a45423.projeto.GlobalData

import a45423.projeto.R
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PostLoader {

    fun loadPost(view : View, databaseRef : FirebaseDatabase, uID : String, imageID : String, url : String, storageRef : FirebaseStorage, whereToLoadImage : Int, whereToLoadLikes : Int, whereToLoadName : Int, whereToLoadProfileImg : Int, commentText : Int){
        ImageLoader().loadProfileImage(view, databaseRef, uID, storageRef, whereToLoadProfileImg)
        loadUserName(view, databaseRef, uID, whereToLoadName)

        ImageLoader().loadImage(url, view.findViewById(whereToLoadImage), storageRef)
        loadLikes(view, databaseRef, imageID, whereToLoadLikes)
        showComment(view, commentText, imageID)
    }

    fun showComment(view : View, commentText : Int, postID : String){
        val databaseRef = FirebaseDatabase.getInstance("https://projetodam-df606-default-rtdb.europe-west1.firebasedatabase.app")

        databaseRef.reference.child("Comments").child(postID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshotData: DataSnapshot) {

                var numberComments = 0

                if(snapshotData.exists()){
                    numberComments = snapshotData.children.count();
                }

                view.findViewById<TextView>(commentText).setTextColor(Color.GRAY)
                view.findViewById<TextView>(commentText).setText(view.resources.getString(R.string.viewAll) + " " + numberComments.toString() + " " + view.resources.getString(R.string.commentsWord))
            }
        })
    }

    fun loadLikes(view : View, databaseRef : FirebaseDatabase, imageID : String, whereToWrite : Int){
        val referencePic = databaseRef.reference.child("posts").child(imageID).child("likes")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val imageView = view.findViewById<ImageView>(R.id.likeImage_ShowPost)

                if(snapshot.child(FirebaseAuth.getInstance().currentUser?.uid!!).getValue() == true)
                    imageView.setImageResource(R.drawable.hearth)
                else
                    imageView.setImageResource(R.drawable.emptyheart)

                view.findViewById<TextView>(whereToWrite).setText(snapshot.children.count().toString())
            }
        })
    }

    fun loadLikes(view : View, databaseRef : FirebaseDatabase, imageID : String, whereToSeeImageLike : ImageView, whereToWriteLikes : TextView){
        val referencePic = databaseRef.reference.child("posts").child(imageID).child("likes")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.child(FirebaseAuth.getInstance().currentUser?.uid!!).getValue() == true)
                    whereToSeeImageLike.setImageResource(R.drawable.hearth)
                else
                    whereToSeeImageLike.setImageResource(R.drawable.emptyheart)

                whereToWriteLikes.setText(snapshot.children.count().toString())
            }
        })
    }

    fun loadUserName(view : View, databaseRef : FirebaseDatabase, uID : String, whereToWrite : Int){
        val referencePic = databaseRef.reference.child("users").child(uID).child("name")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists()){
                    view.findViewById<TextView>(whereToWrite).setText(snapshot.value.toString())
                }
            }
        })
    }

}