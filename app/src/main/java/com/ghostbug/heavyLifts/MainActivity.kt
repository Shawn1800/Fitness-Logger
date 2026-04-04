package com.ghostbug.heavyLifts


import SignInViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ghostbug.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyLifts.data.repository.AuthRepository
import com.ghostbug.heavyLifts.data.repository.AuthRepositoryImpl
import com.ghostbug.heavyLifts.navigation.Route
import com.ghostbug.heavyLifts.ui.screen.exercise_selection.ExerciseSelectionScreen
import com.ghostbug.heavyLifts.ui.screen.exercise_selection.ExerciseViewModel
import com.ghostbug.heavyLifts.ui.screen.exercise_selection.ExerciseViewModelFactory
import com.ghostbug.heavyLifts.ui.screen.home.HomeScreen
import com.ghostbug.heavyLifts.ui.screen.home.HomeViewModel
import com.ghostbug.heavyLifts.ui.screen.home.HomeViewModelFactory
import com.ghostbug.heavyLifts.ui.screen.log_workout.LogWorkoutScreen
import com.ghostbug.heavyLifts.ui.screen.log_workout.LogWorkoutViewModel
import com.ghostbug.heavyLifts.ui.screen.log_workout.LogWorkoutViewModelFactory
import com.ghostbug.heavyLifts.ui.screen.signIn.SignInScreen
import com.ghostbug.heavyLifts.ui.screen.signIn.SignInViewModelFactory
import com.ghostbug.heavyLifts.ui.screen.theme.Demo103Theme
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpScreen
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpViewModel
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpViewModelFactory
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import com.ghostbug.heavyLifts.data.repository.ExerciseRepository
import com.ghostbug.heavyLifts.data.repository.ExerciseRepositoryImpl
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepository
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepositoryImpl
import com.ghostbug.heavyLifts.data.repository.WorkoutRepository
import com.ghostbug.heavyLifts.data.repository.WorkoutRepositoryImpl


val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
) {
    install(Auth)
    install(Postgrest)

}

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Demo103Theme {
                MainNavigation()
            }
        }
    }
}



@Composable
fun MainNavigation() {
    val context = LocalContext.current

    val workoutRepository = remember { WorkoutRepositoryImpl(supabase.postgrest, supabase.auth) }
    val exerciseRepository = remember { ExerciseRepositoryImpl(supabase.postgrest, supabase.auth) }
    val oneRepMaxRepository = remember { OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth) }

    val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository,workoutRepository)

    val authRepository : AuthRepository =remember {
        AuthRepositoryImpl(
            auth = supabase.auth
        )
    }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(workoutRepository,oneRepMaxRepository, oneRepMaxUseCase )
    )

    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(exerciseRepository, workoutRepository)
    )

    val logWorkoutViewModel: LogWorkoutViewModel= viewModel(
        factory = LogWorkoutViewModelFactory(workoutRepository, oneRepMaxRepository,oneRepMaxUseCase )
    )

    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(authRepository)
    )
    val signUpViewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(authRepository)
    )


    // Check if user is logged in to decide initial screen
    val initialRoute = remember {
        if (supabase.auth.currentUserOrNull() != null) Route.HomeScreen
        else Route.SignInScreen
    }
    val backStack = remember { mutableStateListOf<Route>(initialRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {
            entry<Route.HomeScreen>{
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onNavigateToLogWorkout = { exercise ->
                            val date = homeViewModel.state.value.selectedDateMillis ?: System.currentTimeMillis()
                            backStack.add(Route.LogWorkoutScreen(exercise,date))
                        },
                        signUpViewModel = signUpViewModel,
                        onNavigateToExerciseSelection = {
                            val selectedDate = homeViewModel.state.value.selectedDateMillis
                            exerciseViewModel.setSelectedDate(selectedDate ?:System.currentTimeMillis())
                            backStack.add(Route.ExerciseScreen)
                        },
                        onNavigateToLogIn ={
                            backStack.clear()
                            backStack.add(Route.SignInScreen)
                        }
                    )
                }

            entry<Route.ExerciseScreen> {
                ExerciseSelectionScreen(
                    viewModel = exerciseViewModel,
                    onNavigateToLogWorkout = { exercise ->
                        val date = homeViewModel.state.value.selectedDateMillis ?: System.currentTimeMillis()
                        backStack.add(Route.LogWorkoutScreen(exercise,date))
                    } ,
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<Route.LogWorkoutScreen> { entry ->
                LogWorkoutScreen(
                    exercise = entry.exercise,
                    dateMillis = entry.dateMillis,
                    logWorkoutViewModel = logWorkoutViewModel,
                    onBack = {
                        backStack.removeAll { it !is Route.HomeScreen }
                    }
                )
            }

            entry<Route.SignInScreen>{
                val state by signInViewModel.state.collectAsState()
                SignInScreen(
                    state = state,
                    onEvent = signInViewModel::onEvent,
                    uiEvent = signInViewModel.uiEvent,
                    onNavigateToHome={
                        backStack.clear()
                        backStack.add(Route.HomeScreen)
                    },
                    onNavigateToSignUp ={
                        backStack.add(Route.SignUpScreen)
                    }
                )
            }

            entry<Route.SignUpScreen>{
                val state by signUpViewModel.state.collectAsState()
                SignUpScreen(
                    state = state,
                    onEvent = signUpViewModel::onEvent,
                    uiEvent = signUpViewModel.uiEvent,
                    onNavigateToSignIn = {
                        backStack.clear()
                        backStack.add(Route.SignInScreen)
                    }
                )
            }
        }
    )
}
