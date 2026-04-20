package com.ghostbug.heavyliftsapp.navigation

import androidx.navigation3.runtime.NavKey


class Navigator(val state: NavigationState) {

        fun navigate(route: NavKey) {
            if(route in state.backStacks.keys) {
                state.topLevelRoute = route
            } else {
                state.backStacks[state.topLevelRoute]?.add(route)
            }
        }

        fun goBack() {
            val currentStack = state.backStacks[state.topLevelRoute]
                ?: error("Back stack for ${state.topLevelRoute} doesn't exist")
            val currentRoute = currentStack.last()

            if(currentRoute == state.topLevelRoute) {
                state.topLevelRoute = state.startRoute
            } else {
                currentStack.removeLastOrNull()
            }
        }

    fun clearAll() {
        state.backStacks.forEach { (key, stack) ->
            stack.clear()
            stack.add(key) // Retain the top-level route as the base of its own stack
        }
        state.topLevelRoute = state.startRoute
    }

    /**
     * Clears only the backstack of the currently selected top-level route.
     * Use this if you want to pop a user back to the root of the current tab.
     */
    fun clearCurrentStack() {
        val currentStack = state.backStacks[state.topLevelRoute]
        currentStack?.clear()
        currentStack?.add(state.topLevelRoute) // Retain the top-level route
    }
    }

