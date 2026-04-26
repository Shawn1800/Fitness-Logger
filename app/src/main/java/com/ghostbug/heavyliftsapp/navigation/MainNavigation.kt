package com.ghostbug.heavyliftsapp.navigation


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ghostbug.heavyliftsapp.data.health.HealthConnectAvailability
import com.ghostbug.heavyliftsapp.di.HeavyLifts
import com.ghostbug.heavyliftsapp.screens.Profile.ProfileViewModelFactory
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseSelectionScreen
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseViewModel
import com.ghostbug.heavyliftsapp.screens.exercise_selection.ExerciseViewModelFactory
import com.ghostbug.heavyliftsapp.screens.home.HomeScreen
import com.ghostbug.heavyliftsapp.screens.home.HomeViewModel
import com.ghostbug.heavyliftsapp.screens.home.HomeViewModelFactory
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutScreen
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutViewModel
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutViewModelFactory
import com.ghostbug.heavyliftsapp.screens.profile.ProfileScreen
import com.ghostbug.heavyliftsapp.screens.profile.ProfileViewModel
import com.ghostbug.heavyliftsapp.screens.signIn.SignInScreen
import com.ghostbug.heavyliftsapp.screens.signIn.SignInViewModel
import com.ghostbug.heavyliftsapp.screens.signIn.SignInViewModelFactory
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModelFactory
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpScreen
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModel
import com.ghostbug.heavyliftsapp.screens.step_tracker.StepTrackerEvent
import com.ghostbug.heavyliftsapp.screens.step_tracker.StepTrackerScreen
import com.ghostbug.heavyliftsapp.screens.step_tracker.StepTrackerViewModel
import com.ghostbug.heavyliftsapp.screens.step_tracker.StepTrackerViewModelFactory
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileScreen1
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileScreen2
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModel
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModelFactory
import com.ghostbug.heavyliftsapp.screens.user_onboarding.step3.UserProfileScreen3
@Composable
fun MainNavigation(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as HeavyLifts
    val container = app.appContainer




    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            container.workoutRepository,
            container.oneRepMaxRepository,
            container.dailyActivityRepository
        )
    )

    val exerciseViewModel: ExerciseViewModel = viewModel(
        factory = ExerciseViewModelFactory(
            container.exerciseRepository,
            container.workoutRepository
        )
    )

    val logWorkoutViewModel: LogWorkoutViewModel = viewModel(
        factory = LogWorkoutViewModelFactory(
            container.workoutRepository,
            container.oneRepMaxRepository,
            container.oneRepMaxUseCase,
            container.exerciseRepository
        )
    )

    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(container.authRepository, container.userProfileRepository)
    )
    val signUpViewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(container.authRepository, container.userProfileRepository)
    )

    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(
            container.userProfileRepository,
            container.healthConnectManager
        )
    )

    val stepTrackerViewModel: StepTrackerViewModel = viewModel(
        factory = StepTrackerViewModelFactory(container.dailyActivityRepository)
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(container.userProfileRepository, container.authRepository)
    )


    val permissionsLauncher = rememberLauncherForActivityResult(
        container.healthConnectManager.requestPermissionsActivityContract()
    ) { grantedPermissions ->
        if (grantedPermissions.isNotEmpty()) {
            stepTrackerViewModel.onEvent(StepTrackerEvent.RefreshToday)
        }
    }

//         check HC availability and request permissions on launch
    LaunchedEffect(Unit) {
        // checkAvailability() returns enum not Boolean — fixed
        if (container.healthConnectManager.checkAvailability() == HealthConnectAvailability.AVAILABLE) {

            // hasAllPermissions() takes no param anymore — fixed
            if (!container.healthConnectManager.hasAllPermissions()) {
                permissionsLauncher.launch(container.healthConnectManager.permissions)
            }
        }
    }


    val navigationState = rememberNavigationState(
        startRoute = Route.HomeScreen,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember {
        Navigator(navigationState)
    }

    val showBottomNav = navigationState.currentRoute.let { current ->
        current !is Route.SignInScreen &&
                current !is Route.SignUpScreen &&
                current !is Route.UserProfileScreen1 &&
                current !is Route.UserProfileScreen2 &&
                current !is Route.UserProfileScreen3
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    selectedKey = navigationState.topLevelRoute,
                    onSelectKey = {
                        navigator.navigate(it)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),

            onBack = navigator::goBack,

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

            entries = navigationState.toEntries(
                entryProvider {
                    entry<Route.HomeScreen> {
                        HomeScreen(
                            homeViewModel = homeViewModel,
                            onNavigateToLogWorkout = { exerciseId ->
                                val date = homeViewModel.state.value.selectedDateMillis
                                    ?: System.currentTimeMillis()
                                navigator.navigate(Route.LogWorkoutScreen(exerciseId, date))
                            },
                            signUpViewModel = signUpViewModel,
                            onNavigateToExerciseSelection = {
                                val selectedDate = homeViewModel.state.value.selectedDateMillis
                                exerciseViewModel.setSelectedDate(
                                    selectedDate ?: System.currentTimeMillis()
                                )
                                navigator.navigate(Route.ExerciseScreen)
                            },
                            onNavigateToLogIn = {
                                navigator.navigate(Route.SignInScreen)
                            },
                            onNavigateToStepTracker = {
                                navigator.navigate(Route.StepTrackerScreen)
                            }
                        )
                    }


                    entry<Route.ExerciseScreen> {
                        ExerciseSelectionScreen(
                            viewModel = exerciseViewModel,
                            onNavigateToLogWorkout = { exerciseId ->
                                val date = homeViewModel.state.value.selectedDateMillis
                                    ?: System.currentTimeMillis()
                                navigator.navigate(Route.LogWorkoutScreen(exerciseId, date))
                            },
                            onBack = { navigator.clearCurrentStack() },
                        )
                    }


                    entry<Route.LogWorkoutScreen> { entry ->
                        LogWorkoutScreen(
                            exerciseId = entry.exerciseId,
                            dateMillis = entry.dateMillis,
                            logWorkoutViewModel = logWorkoutViewModel,
                            onBack = { navigator.clearCurrentStack() }
                        )
                    }



                    entry<Route.SignInScreen> {
                        val state by signInViewModel.state.collectAsStateWithLifecycle()
                        SignInScreen(
                            state = state,
                            onEvent = signInViewModel::onEvent,
                            uiEvent = signInViewModel.uiEvent,
                            onNavigateToHome = {
                                navigator.clearAll()
                                navigator.navigate(Route.HomeScreen)
                            },
                            onNavigateToOnboarding = {
                                userProfileViewModel.reload()
                                navigator.clearAll()
                                navigator.navigate(Route.UserProfileScreen1)
                            },
                            onNavigateToSignUp = {
                                navigator.navigate(Route.SignUpScreen)
                            }
                        )
                    }


                    entry<Route.SignUpScreen> {
                        val state by signUpViewModel.state.collectAsStateWithLifecycle()
                        SignUpScreen(
                            state = state,
                            onEvent = signUpViewModel::onEvent,
                            uiEvent = signUpViewModel.uiEvent,
                            onNavigateToUserProfileScreen1 = {
                                navigator.clearAll()
                                navigator.navigate(Route.UserProfileScreen1)
                            },
                            onNavigateToSignIn = {
                                navigator.clearAll()
                                navigator.navigate(Route.SignInScreen)
                            }
                        )
                    }


                    entry<Route.UserProfileScreen1> {
                        UserProfileScreen1(
                            viewModel = userProfileViewModel,
                            onNext = {
                                navigator.navigate(Route.UserProfileScreen2)
                            },
                            onBack = {
                                navigator.clearCurrentStack()
                            }

                        )

                    }


                    entry<Route.UserProfileScreen2> {
                        UserProfileScreen2(
                            viewModel = userProfileViewModel,
                            onNext = {
                                navigator.navigate(Route.UserProfileScreen3)
                            },
                            onBack = {
                                navigator.navigate(Route.UserProfileScreen1)
                            }
                        )

                    }


                    entry<Route.UserProfileScreen3> {
                        UserProfileScreen3(
                            viewModel = userProfileViewModel,
                            onBack = {
                                navigator.navigate(Route.UserProfileScreen2)
                            },
                            onNext = {
                                navigator.clearAll()
                                navigator.navigate(Route.HomeScreen)
                            }

                        )
                    }



                    entry<Route.StepTrackerScreen> {
                        StepTrackerScreen(
                            viewModel = stepTrackerViewModel,
                            onBack = {
                                navigator.clearCurrentStack()
                            }
                        )
                    }


                    entry<Route.ProfileScreen> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onBackClick = {
                                navigator.navigate(Route.HomeScreen)
                            },
                            onLogoutClick = {
                                navigator.clearAll()
                                navigator.navigate(Route.SignInScreen)
                            }
                        )
                    }


                    entry<Route.LeaderBoards> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Coming Soon..")
                        }
                    }
                }
            )
        )
    }
}
