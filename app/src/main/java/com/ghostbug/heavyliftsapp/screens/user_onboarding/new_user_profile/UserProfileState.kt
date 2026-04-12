import com.ghostbug.heavyliftsapp.data.domain.Gender
import kotlin.time.Instant

data class UserProfileState(
    val userName: String = "",
    val userNameError: String? = null,
    val age: Int? = null,
    val ageError: String? = null,
    val height: Float? = null,
    val heightError: String? = null,
    val userWeight: Float? = null,
    val weightError: String? = null,
    val gender: Gender? = null,
    val city: String? = null,
    val country: String? = null,
    val profilePic: String? = null,
    val dob: Instant? = null,
    val isLoading: Boolean = false,
    val heightUnit: HeightUnit = HeightUnit.CM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val heightInputText: String = "",
    val weightInputText: String = "",
)

enum class HeightUnit { CM, FEET }
enum class WeightUnit { KG, LBS }