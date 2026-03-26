package com.example.HeavyLifts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.HeavyLifts.data.UseCase.OneRepMaxUseCase
import com.example.HeavyLifts.data.db.ExerciseDatabase
import com.example.HeavyLifts.data.repository.AuthRepository
import com.example.HeavyLifts.data.repository.ExerciseRepository
import com.example.HeavyLifts.data.repository.OneRepMaxRepository
import com.example.HeavyLifts.data.repository.WorkoutRepository
import com.example.HeavyLifts.navigation.Route
import com.example.HeavyLifts.ui.screen.exercise_selection.ExerciseSelectionScreen
import com.example.HeavyLifts.ui.screen.exercise_selection.ExerciseViewModel
import com.example.HeavyLifts.ui.screen.exercise_selection.ExerciseViewModelFactory
import com.example.HeavyLifts.ui.screen.home.HomeScreen
import com.example.HeavyLifts.ui.screen.home.HomeViewModel
import com.example.HeavyLifts.ui.screen.home.HomeViewModelFactory
import com.example.HeavyLifts.ui.screen.log_workout.LogWorkoutScreen
import com.example.HeavyLifts.ui.screen.log_workout.LogWorkoutViewModel
import com.example.HeavyLifts.ui.screen.log_workout.LogWorkoutViewModelFactory
import com.example.HeavyLifts.theme.Demo103Theme
import com.example.HeavyLifts.ui.screen.signIn.AuthViewModel
import com.example.HeavyLifts.ui.screen.signIn.AuthViewModelFactory
import com.example.HeavyLifts.ui.screen.signIn.LogInScreen
import com.example.HeavyLifts.ui.screen.signIn.SignUpScreen

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
    val db = ExerciseDatabase.getInstance(context)

    val workoutRepository = WorkoutRepository(db.workoutEntryEntityDao())
    val exerciseRepository = ExerciseRepository(
        db.exerciseDao(),
        workoutEntryEntityDao = db.workoutEntryEntityDao()
    )
    val oneRepMaxRepository = OneRepMaxRepository(db.oneRepMaxEntityDao(), workoutEntryEntityDao = db.workoutEntryEntityDao())
    val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)

    val authRepository = AuthRepository()

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(workoutRepository,oneRepMaxRepository, oneRepMaxUseCase )
    )

    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(exerciseRepository, workoutRepository)
    )

    val logWorkoutViewModel: LogWorkoutViewModel= viewModel(
        factory = LogWorkoutViewModelFactory(workoutRepository, oneRepMaxRepository,oneRepMaxUseCase )
    )

    val authViewModel: AuthViewModel=viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    // Check if user is logged in to decide initial screen
    val initialRoute = remember {
        if (authRepository.isUserLoggedIn()) Route.HomeScreen else Route.LogInScreen
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
                        authViewModel = authViewModel,
                        onNavigateToLogWorkout = { exercise ->
                            val date = homeViewModel.state.value.selectedDateMillis ?: System.currentTimeMillis()
                            backStack.add(Route.LogWorkoutScreen(exercise,date))
                        },
                        onNavigateToExerciseSelection = {
                            val selectedDate = homeViewModel.state.value.selectedDateMillis
                            exerciseViewModel.setSelectedDate(selectedDate ?:System.currentTimeMillis())
                            backStack.add(Route.ExerciseScreen)
                        },
                        onNavigateToLogIn ={
                            backStack.clear()
                            backStack.add(Route.LogInScreen)
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

            entry<Route.LogInScreen>{
                LogInScreen(
                    authViewModel=authViewModel,
                    NavToHome={
                        backStack.clear()
                        backStack.add(Route.HomeScreen)
                    },
                    NavToSignUp={
                        backStack.add(Route.SignUpScreen)
                    }
                )
            }

            entry<Route.SignUpScreen>{
                SignUpScreen(
                    authViewModel=authViewModel,
                    NavToHome={
                        backStack.clear()
                        backStack.add(Route.HomeScreen)
                    },
                    NavToLogIn={
                        backStack.clear()
                        backStack.add(Route.LogInScreen)
                    }
                )
            }
        }
    )
}
