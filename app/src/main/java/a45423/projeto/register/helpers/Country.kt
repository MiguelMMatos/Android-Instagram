package a45423.projeto.register.helpers

class Country(val city : String, val lat : String, val lng : String, val country : String, val iso2 : String, val admin_name : String, val capital : String, val population : String, val population_proper : String) {
    override fun toString(): String {
        return "Country(city='$city', lat='$lat', long='$lng', country='$country', iso='$iso2', admin_name='$admin_name', capital='$capital', population='$population', real_pop='$population_proper')"
    }
}