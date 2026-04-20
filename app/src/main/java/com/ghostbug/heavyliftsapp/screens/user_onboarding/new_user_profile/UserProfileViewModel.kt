package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import HeightUnit
import UserProfileState
import WeightUnit
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.permission.HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_HISTORY
import androidx.health.connect.client.permission.HealthPermission.Companion.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity
import com.ghostbug.heavyliftsapp.data.health.HealthConnectManager
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlinx.coroutines.*
import kotlin.time.Clock

class UserProfileViewModel(
    val repository: UserProfileRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),

        )

    val backgroundReadPermissions = setOf(PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)
    val historyReadPermissions = setOf(PERMISSION_READ_HEALTH_DATA_HISTORY)


    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UserProfileUiEvent>()
    val uiEvent: SharedFlow<UserProfileUiEvent> = _uiEvent.asSharedFlow()

    private var checkNameJob: Job? = null

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            val googlePic = repository.getGoogleProfilePic()
            val existingProfile = repository.getOwnProfile()

            _state.update {
                UserProfileState(
                    profilePic = googlePic,
                    userName = existingProfile?.userName ?: "",
                    age = existingProfile?.age,
                    height = existingProfile?.height,
                    userWeight = existingProfile?.userWeight,
                    gender = existingProfile?.gender,
                    city = existingProfile?.city,
                    country = existingProfile?.country,
                    heightUnit = it.heightUnit,
                    weightUnit = it.weightUnit,
                    weightInputText = existingProfile?.userWeight?.let { w ->
                        if (it.weightUnit == WeightUnit.LBS) "%.1f".format(w * 2.20462f)
                        else "%.1f".format(w)
                    } ?: ""
                )
            }

            val initialHeight = _state.value.height
            if (initialHeight != null) {
                val formattedHeight = if (_state.value.heightUnit == HeightUnit.FEET) {
                    cmToFeetInches(initialHeight)
                } else {
                    "%.1f".format(initialHeight)
                }
                _state.update { it.copy(heightInputText = formattedHeight) }
            }
        }
    }

    fun onEvent(event: UserProfileEvent) {
        when (event) {
            is UserProfileEvent.OnAgeChanged -> handleAgeChanged(event.age)
            is UserProfileEvent.OnHeightChanged -> handleHeightChanged(event.height)
            is UserProfileEvent.OnUserWeightChanged -> handleWeightChanged(event.userWeight)
            is UserProfileEvent.OnHeightUnitChanged -> handleHeightUnitChanged(event.unit)
            is UserProfileEvent.OnWeightUnitChanged -> handleWeightUnitChanged(event.unit)
            is UserProfileEvent.OnGenderChanged -> handleGenderChanged(event.gender)
            is UserProfileEvent.OnCityChanged -> _state.update { it.copy(city = event.city) }
            is UserProfileEvent.OnCountryChanged -> _state.update { it.copy(country = event.country) }
            is UserProfileEvent.OnDobChanged -> _state.update { it.copy(dob = event.dob) }
            is UserProfileEvent.OnProfilePicChanged -> _state.update { it.copy(profilePic = event.profilePic) }
            is UserProfileEvent.OnUserNameChanged -> handleUserNameChanged(event.userName)
            UserProfileEvent.OnSubmitProfile -> handleSubmitProfile()
            UserProfileEvent.NavtoScreen2 -> handleNavToScreen2()
            UserProfileEvent.NavtoScreen3 -> handleNavToScreen3()
            is UserProfileEvent.message -> sendSnackbar(event.message)
        }
    }

    private fun handleAgeChanged(ageStr: String) {
        val age = ageStr.toIntOrNull()
        val error = when {
            ageStr.isBlank() -> null
            age == null -> "Please enter a valid age"
            age < 12 -> "You must be at least 12 years old"
            age > 120 -> "Please enter a valid age"
            else -> null
        }
        _state.update { it.copy(age = age, ageError = error) }
    }

 fun handleHeightChanged(input: String) {
        val unit = _state.value.heightUnit

        if (unit == HeightUnit.CM) {
            val heightInCm = input.toFloatOrNull()
            val error = when {
                input.isBlank() -> null
                heightInCm == null -> "Please enter a valid height"
                heightInCm < 91.44f -> "Height must be more than 3 feet"
                heightInCm > 272f -> "Please enter a valid height"
                else -> null
            }
            _state.update { it.copy(heightInputText = input, height = heightInCm, heightError = error) }
            return
        }

        // FEET mode — auto format
        // strip everything except digits
        val digitsOnly = input.filter { it.isDigit() }

        val formatted = when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length == 1 -> digitsOnly // just "5", still typing feet
            else -> {
                // first digit = feet, rest = inches (max 2 digits)
                val feet = digitsOnly.take(1)
                val inches = digitsOnly.drop(1).take(2)
                "$feet'$inches"
            }
        }

        val heightInCm = if (digitsOnly.length >= 2) {
            val feet = digitsOnly.take(1).toFloatOrNull() ?: 0f
            val inches = digitsOnly.drop(1).take(2).toFloatOrNull() ?: 0f
            if (inches >= 12f) null // invalid inches
            else ((feet * 12f) + inches) * 2.54f
        } else null

        val error = when {
            formatted.isBlank() -> null
            digitsOnly.length == 1 -> null // still typing, no error yet
            heightInCm == null -> "Inches must be 0–11"
            heightInCm < 91.44f -> "Height must be more than 3 feet"
            heightInCm > 272f -> "Please enter a valid height"
            else -> null
        }

        _state.update { it.copy(heightInputText = formatted, height = heightInCm, heightError = error) }
    }


    private fun handleWeightChanged(input: String) {
        val value = input.toFloatOrNull()
        val unit = _state.value.weightUnit

        val error = when {
            input.isBlank() -> null
            value == null -> "Please enter a valid weight"
            unit == WeightUnit.KG && value < 30f -> "Weight must be at least 30 kg"
            unit == WeightUnit.KG && value > 300f -> "Weight must be less than 300 kg"
            unit == WeightUnit.LBS && value < 66f -> "Weight must be at least 66 lbs (30 kg)"
            unit == WeightUnit.LBS && value > 661f -> "Weight must be less than 661 lbs (300 kg)"
            else -> null
        }

        // always store internally in KG
        val weightInKg = if (value != null) {
            if (unit == WeightUnit.LBS) value * 0.453592f else value
        } else null

        _state.update { it.copy(userWeight = weightInKg, weightInputText = input, weightError = error) }
    }

    private fun handleHeightUnitChanged(unit: HeightUnit) {
        val currentCm = _state.value.height

        val convertedInput = when (unit) {
            HeightUnit.FEET -> {
                if (currentCm != null && currentCm > 0f) {
                    // convert cm to feet'inches formatted string
                    val totalInches = (currentCm / 2.54f).toInt()
                    val feet = totalInches / 12
                    val inches = totalInches % 12
                    // store as raw digits so handleHeightChanged formats it
                    "$feet$inches"  // e.g "56" which handleHeightChanged turns into "5'6"
                } else ""
            }
            HeightUnit.CM -> {
                if (currentCm != null && currentCm > 0f) {
                    "%.1f".format(currentCm)
                } else ""
            }
        }

        // update unit first, then run through handler to format properly
        _state.update { it.copy(heightUnit = unit) }
        handleHeightChanged(convertedInput)
    }

    private fun handleWeightUnitChanged(unit: WeightUnit) {
        val currentInput = _state.value.weightInputText
        val currentValue = currentInput.toFloatOrNull()

        val convertedInput = if (currentValue != null) {
            when (unit) {
                WeightUnit.LBS -> if (_state.value.weightUnit == WeightUnit.KG)
                    "%.1f".format(currentValue * 2.20462f) else currentInput
                WeightUnit.KG -> if (_state.value.weightUnit == WeightUnit.LBS)
                    "%.1f".format(currentValue * 0.453592f) else currentInput
            }
        } else currentInput

        _state.update { it.copy(weightUnit = unit, weightInputText = convertedInput) }
        handleWeightChanged(convertedInput)
    }

    private fun handleGenderChanged(gender: Gender) {
        _state.update { it.copy(gender = gender) }
    }

    private fun handleUserNameChanged(userName: String) {
        _state.update {
            it.copy(
                userName = userName,
                userNameError = when {
                    userName.isBlank() -> null
                    userName.length < 3 -> "Username must be at least 3 characters"
                    userName.length > 20 -> "Username must be less than 20 characters"
                    else -> null
                }
            )
        }
        if (userName.length in 3..20) {
            checkNameJob?.cancel()
            checkNameJob = viewModelScope.launch {
                delay(350)
                val isTaken = repository.isUserNameTaken(userName)
                _state.update {
                    it.copy(userNameError = if (isTaken) "Username is already taken" else null)
                }
            }
        }
    }

    fun handleNavToScreen2() {
        viewModelScope.launch {
            val userName = _state.value.userName
            when {
                userName.isBlank() -> {
                    _state.update { it.copy(userNameError = "Please enter a username") }
                    sendSnackbar("Please enter a username")
                }
                userName.length < 3 -> {
                    _state.update { it.copy(userNameError = "Username must be at least 3 characters") }
                }
                userName.length > 20 -> {
                    _state.update { it.copy(userNameError = "Username must be 20 characters or less") }
                }
                _state.value.userNameError == "Username is already taken" -> {
                    sendSnackbar("Please choose a different username")
                }
                else -> {
                    _state.update { it.copy(isLoading = true) }
                    val isTaken = repository.isUserNameTaken(userName)
                    _state.update { it.copy(isLoading = false) }
                    if (isTaken) {
                        _state.update { it.copy(userNameError = "Username is already taken") }
                        sendSnackbar("Username is already taken")
                    } else {
                        _uiEvent.emit(UserProfileUiEvent.NavToScreen2)
                    }
                }
            }
        }
    }

    private fun handleNavToScreen3() {


            viewModelScope.launch { _uiEvent.emit(UserProfileUiEvent.NavToScreen3) }
        }


    private fun handleSubmitProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val s = _state.value
                if (s.userName.isBlank() || s.userNameError != null) {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }
                val existingProfile = repository.getOwnProfile()
                val now = Clock.System.now()
                val profile = UserProfileEntity(
                    userId = "",
                    userName = s.userName,
                    age = s.age,
                    height = s.height,
                    userWeight = s.userWeight,
                    gender = s.gender,
                    city = s.city,
                    country = s.country,
                    profilePic = s.profilePic,
                    dob = s.dob,
                    accountCreated = existingProfile?.accountCreated ?: now,
                    accountLastUpdated = now,
                )
                repository.upsertProfile(profile)

//                // Sync to Health Connect if possible
//                if (healthConnectManager.checkAvailability() && s.userWeight != null) {
//                    if (healthConnectManager.hasAllPermissions(healthConnectManager.permissions)) {
//                        healthConnectManager.writeWeightInput(s.userWeight.toDouble())
//                    }
//                }

                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(UserProfileUiEvent.NavToHome)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                 sendSnackbar("Failed to save profile. Please try again.")
            }
        }
    }

    fun cmToFeetInches(cm: Float): String {
        val totalInches = (cm / 2.54f).toInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        return "$feet'$inches\""
    }



    private fun sendSnackbar(message: String) {
        viewModelScope.launch { _uiEvent.emit(UserProfileUiEvent.SendSnackbar(message)) }
    }
}