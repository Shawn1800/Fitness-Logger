package com.example.demo103

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.setSelectedDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.demo103.data.db.ExerciseDatabase
import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.repository.ExerciseRepository
import com.example.demo103.data.repository.WorkoutRepository
import com.example.demo103.di.Demo103App
import com.example.demo103.navigation.Route
import com.example.demo103.ui.theme.exercise_selection.ExerciseSelectionScreen
import com.example.demo103.ui.theme.exercise_selection.ExerciseViewModel
import com.example.demo103.ui.theme.exercise_selection.ExerciseViewModelFactory
import com.example.demo103.ui.theme.home.HomeScreen
import com.example.demo103.ui.theme.home.HomeViewModel
import com.example.demo103.ui.theme.home.HomeViewModelFactory
import com.example.demo103.ui.theme.log_workout.LogWorkoutScreen
import com.example.demo103.ui.theme.log_workout.LogWorkoutViewModel
import com.example.demo103.ui.theme.log_workout.LogWorkoutViewModelFactory
import com.example.demo103.ui.theme.theme.Demo103Theme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as Demo103App


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

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(workoutRepository)
    )

    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(exerciseRepository, workoutRepository)
    )

    val logWorkoutViewModel: LogWorkoutViewModel= viewModel(
        factory = LogWorkoutViewModelFactory(workoutRepository)
    )


//val backStack = rememberNavBackStack <Route> (Route.HomeScreen)
    val backStack = remember { mutableStateListOf<Route>(Route.HomeScreen) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
                 },
        entryProvider = entryProvider {
            entry<Route.HomeScreen>{
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onNavigateToLogWorkout = { exercise ->
                            val date = homeViewModel.state.value.selectedDateMillis ?: System.currentTimeMillis()
                            backStack.add(Route.LogWorkoutScreen(exercise,date))  // pass exercise
                        },
                        onNavigateToExerciseSelection = {
                            val selectedDate = homeViewModel.state.value.selectedDateMillis
                            exerciseViewModel.setSelectedDate(selectedDate ?:System.currentTimeMillis())
                            backStack.add(Route.ExerciseScreen)
                        }
                    )
                }

            entry<Route.ExerciseScreen> {
                ExerciseSelectionScreen(
                    viewModel = exerciseViewModel,
                    onNavigateToLogWorkout = { exercise ->
                        val date = homeViewModel.state.value.selectedDateMillis ?: System.currentTimeMillis()
                        backStack.add(Route.LogWorkoutScreen(exercise,date))  // pass exercise
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
        }
    )
}



