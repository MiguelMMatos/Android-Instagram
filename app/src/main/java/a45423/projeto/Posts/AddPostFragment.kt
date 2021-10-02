package a45423.projeto.Posts

import a45423.projeto.GlobalData.UtilFuctions
import a45423.projeto.R
import a45423.projeto.register.NewUser
import a45423.projeto.register.ui.RegisterProfilePicDirections
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.concurrent.schedule


class AddPostFragment : Fragment(), View.OnClickListener {
    var navController : NavController? = null;
    lateinit var mStorageRefence : StorageReference

    lateinit var buttonCreated : Button
    lateinit var progressBar : ProgressBar

    var newPostPic : Uri? = null
    lateinit var postPicImageView : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mStorageRefence = FirebaseStorage.getInstance().getReference("uploads/posts")
        navController = Navigation.findNavController(view);

        buttonCreated = view.findViewById(R.id.newPostNext_Button)
        buttonCreated.setOnClickListener(this)

        postPicImageView = view.findViewById(R.id.newPostPic_Selector)
        postPicImageView.setOnClickListener(this)

        progressBar = view.findViewById(R.id.progressBar_profilePic)

        UtilFuctions().disableButton(buttonCreated, this.requireContext())
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.newPostPic_Selector -> {
                openFileChooser()
            }
            R.id.newPostNext_Button -> {
                loadImage()
            }
        }
    }

    fun openFileChooser(){
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if( requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.data != null ) {
            newPostPic = data.data
            postPicImageView.setImageURI(newPostPic)
            UtilFuctions().enableButton(buttonCreated, this.requireContext())
        }
    }

    fun loadImage(){
        if(newPostPic != null){
            val fileReference = mStorageRefence.child(System.currentTimeMillis().toString() + "." + UtilFuctions().getFileExtension(newPostPic!!, this.requireContext()))

            val uploadTask = fileReference.putFile(newPostPic!!)

            //ONSUCESS
            uploadTask.addOnSuccessListener { taskSnapshot ->
                Timer("ProgressBarDelay", false).schedule(500){
                    progressBar.setProgress(0)
                }
                Toast.makeText(context, "Upload sucessuful", Toast.LENGTH_LONG).show()
            }

            //ONFAILURE
            uploadTask.addOnFailureListener{
                Toast.makeText(context, "Failed to load Image", Toast.LENGTH_SHORT).show()
            }

            //ON PROGRESS
            uploadTask.addOnProgressListener { snapshot ->
                var progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressBar.setProgress(progress.toInt())
            }

            //ONCOMPLETE
            uploadTask.addOnCompleteListener{ taskUpload ->
                if ( taskUpload.isSuccessful){
                    fileReference.downloadUrl.addOnSuccessListener (object :
                        OnSuccessListener<Uri> {
                        override fun onSuccess(uri: Uri?) {
                            val action = AddPostFragmentDirections.actionAddPostFragmentToAddPostDescriptionFragment(uri.toString());
                            navController!!.navigate(action)
                        }
                    })
                }
            }
        }
        else
            Toast.makeText(context, "No file seleted", Toast.LENGTH_SHORT).show()
    }
}