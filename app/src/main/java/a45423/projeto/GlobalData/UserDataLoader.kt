package a45423.projeto.GlobalData

import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class UserDataLoader {

    lateinit var age : String
    lateinit var email : String
    lateinit var name : String
    lateinit var password : String
    lateinit var profilePic : String
    lateinit var region : String
    lateinit var username : String

    fun insertData(age : String, email : String, name : String, password : String, profilePic : String, region : String, username : String){
        this.age = StringToAge(age)
        this.email = email
        this.name = name
        this.password = password
        this.profilePic = profilePic
        this.region = region
        this.username = username
    }


    fun StringToAge(ageText : String): String {
        val ageSplit = ageText.split("-")
        val year = ageSplit[2].toInt()
        val month = ageSplit[1].toInt()
        val day = ageSplit[0].toInt()

        println("y -> " + year + " m -> " + month + " d-> " + day)

        var dob : Calendar = Calendar.getInstance()
        var today : Calendar = Calendar.getInstance()

        dob.set(year, month, day)

        var age : Int = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if(today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
            age--

        return age.toString()
    }
}