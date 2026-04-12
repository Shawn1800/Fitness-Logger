package com.ghostbug.heavyliftsapp.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.AuthRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepositoryImpl
import com.ghostbug.heavyliftsapp.supabase
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseSelectionScreen
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseViewModel
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseViewModelFactory
import com.ghostbug.heavyliftsapp.screens.home.HomeScreen
import com.ghostbug.heavyliftsapp.screens.home.HomeViewModel
import com.ghostbug.heavyliftsapp.screens.home.HomeViewModelFactory
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutScreen
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutViewModel
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutViewModelFactory
import com.ghostbug.heavyliftsapp.screens.signIn.SignInScreen
import com.ghostbug.heavyliftsapp.screens.signIn.SignInViewModel
import com.ghostbug.heavyliftsapp.screens.signIn.SignInViewModelFactory
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModelFactory
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpScreen
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModel
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileScreen1
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileScreen2
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModel
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModelFactory
import com.ghostbug.heavyliftsapp.screens.user_onboarding.step3.UserProfileScreen3
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest


    @Composable
    fun MainNavigation() {
        val context = LocalContext.current

        val workoutRepository =
            remember { WorkoutRepositoryImpl(supabase.postgrest, supabase.auth) }
        val exerciseRepository =
            remember { ExerciseRepositoryImpl(supabase.postgrest, supabase.auth) }
        val oneRepMaxRepository =
            remember { OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth) }
        val userProfileRepository =
            remember { UserProfileRepositoryImpl(supabase.postgrest, supabase.auth) }

        val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)

        val authRepository: AuthRepository = remember {
            AuthRepositoryImpl(
                auth = supabase.auth
            )
        }
        val homeViewModel: HomeViewModel = viewModel(
            factory = HomeViewModelFactory(workoutRepository, oneRepMaxRepository, oneRepMaxUseCase)
        )

        val exerciseViewModel: ExerciseViewModel = viewModel(
            factory = ExerciseViewModelFactory(exerciseRepository, workoutRepository)
        )

        val logWorkoutViewModel: LogWorkoutViewModel = viewModel(
            factory = LogWorkoutViewModelFactory(
                workoutRepository,
                oneRepMaxRepository,
                oneRepMaxUseCase,
                exerciseRepository
            )
        )

        val signInViewModel: SignInViewModel = viewModel(
            factory = SignInViewModelFactory(authRepository, userProfileRepository)
        )
        val signUpViewModel: SignUpViewModel = viewModel(
            factory = SignUpViewModelFactory(authRepository, userProfileRepository)
        )

        val userProfileViewModel: UserProfileViewModel = viewModel(
            factory = UserProfileViewModelFactory(userProfileRepository)
        )


        // Check if user is logged in to decide initial screen
        val initialRoute = remember {
            if (supabase.auth.currentUserOrNull() != null) Route.HomeScreen
            else Route.SignInScreen
        }
        val backStack = remember { mutableStateListOf<Route>(initialRoute) }

        NavDisplay(
            modifier = Modifier.background(Color.Black),
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
            },
            entryProvider = entryProvider {
                entry<Route.HomeScreen> {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        onNavigateToLogWorkout = { exerciseId ->
                            val date = homeViewModel.state.value.selectedDateMillis
                                ?: System.currentTimeMillis()
                            backStack.add(Route.LogWorkoutScreen(exerciseId, date))
                        },
                        signUpViewModel = signUpViewModel,
                        onNavigateToExerciseSelection = {
                            val selectedDate = homeViewModel.state.value.selectedDateMillis
                            exerciseViewModel.setSelectedDate(
                                selectedDate ?: System.currentTimeMillis()
                            )
                            backStack.add(Route.ExerciseScreen)
                        },
                        onNavigateToLogIn = {
                            backStack.clear()
                            backStack.add(Route.SignInScreen)
                        }
                    )
                }

                entry<Route.ExerciseScreen> {
                    ExerciseSelectionScreen(
                        viewModel = exerciseViewModel,
                        onNavigateToLogWorkout = { exerciseId ->
                            val date = homeViewModel.state.value.selectedDateMillis
                                ?: System.currentTimeMillis()
                            backStack.add(Route.LogWorkoutScreen(exerciseId, date))
                        },
                        onBack = { backStack.removeLastOrNull() },
                    )
                }

                entry<Route.LogWorkoutScreen> { entry ->
                    LogWorkoutScreen(
                        exerciseId = entry.exerciseId,
                        dateMillis = entry.dateMillis,
                        logWorkoutViewModel = logWorkoutViewModel,
                        onBack = {
                            backStack.removeAll { it !is Route.HomeScreen }
                        }
                    )
                }

                entry<Route.SignInScreen> {
                    val state by signInViewModel.state.collectAsState()
                    SignInScreen(
                        state = state,
                        onEvent = signInViewModel::onEvent,
                        uiEvent = signInViewModel.uiEvent,
                        onNavigateToHome = {
                            backStack.clear()
                            backStack.add(Route.HomeScreen)
                        },
                        onNavigateToOnboarding = {
                            backStack.clear()
                            backStack.add(Route.UserProfileScreen1)
                        },
                        onNavigateToSignUp = {
                            backStack.add(Route.SignUpScreen)
                        }
                    )
                }

                entry<Route.SignUpScreen> {
                    val state by signUpViewModel.state.collectAsState()
                    SignUpScreen(
                        state = state,
                        onEvent = signUpViewModel::onEvent,
                        uiEvent = signUpViewModel.uiEvent,
                        onNavigateToUserProfileScreen1 = {
                            backStack.clear()
                            backStack.add(Route.UserProfileScreen1)
                        },
                        onNavigateToSignIn = {
                            backStack.clear()
                            backStack.add(Route.SignInScreen)
                        }
                    )
                }
                entry<Route.UserProfileScreen1> {
                    UserProfileScreen1(
                        viewModel = userProfileViewModel,
                        onNext = {
                            backStack.add(Route.UserProfileScreen2)
                        },
                        onBack = {
                            backStack.removeLastOrNull()
                        }

                    )

                }

                entry<Route.UserProfileScreen2> {
                    UserProfileScreen2(
                        viewModel = userProfileViewModel,
                        onNext = {
                            backStack.add(Route.UserProfileScreen3)
                        },
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )

                }
                entry<Route.UserProfileScreen3> {
                    UserProfileScreen3(
                        viewModel = userProfileViewModel,
                        onBack = {
                            backStack.removeLastOrNull()
                        },
                        onNext = {
                            backStack.add(Route.HomeScreen)
                        }

                    )
                }
            }


        )
    }