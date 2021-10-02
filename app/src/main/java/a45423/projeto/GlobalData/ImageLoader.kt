package a45423.projeto.GlobalData

import a45423.projeto.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import android.widget.GridLayout
import android.widget.GridView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ImageLoader {

    fun loadProfileImage(view : View, databaseRef : FirebaseDatabase, uID : String, storageRef : FirebaseStorage, imageIdToLoad : Int){
        val referencePic = databaseRef.reference.child("users").child(uID).child("profilePic")

        referencePic.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //User exists
                if(snapshot.exists()){
                    loadImage(snapshot.value.toString(), view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(imageIdToLoad) ,storageRef)
                }
            }
        })
    }



    fun loadImage(url : String, imageView : ImageView, storageRef : FirebaseStorage) {
        val ref = storageRef.getReferenceFromUrl(url)
        val ONE_MEGABYTE: Long = 1024 * 1024

        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bmp)
        }.addOnFailureListener {
            // Handle any errors
        }
    }
}