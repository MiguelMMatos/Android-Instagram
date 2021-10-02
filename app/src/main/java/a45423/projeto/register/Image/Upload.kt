package a45423.projeto.register.Image

class Upload(var name : String, var uri : String) {

    init {
        if( name.trim().equals("") )
            name = "Noname"

        this.name = name
        this.uri = uri
    }
}