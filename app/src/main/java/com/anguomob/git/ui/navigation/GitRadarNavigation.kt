package com.anguomob.git.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anguomob.git.ui.components.BottomNavigationBar
import com.anguomob.git.ui.screen.dashboard.DashboardScreen
import com.anguomob.git.ui.screen.personal.PersonalRadarScreen
import com.anguomob.git.ui.screen.repository.RepositoryDetailScreen
import com.anguomob.git.ui.screen.search.SearchScreen
import com.anguomob.git.ui.screen.topic.TopicScreen

@Composable
fun GitRadarNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GitRadarNavHost(navController = navController)
        }
    }
}

@Composable
fun GitRadarNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(
            route = Screen.Topics.route + "?topicName={topicName}",
            arguments = listOf(navArgument("topicName") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            TopicScreen(navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(Screen.PersonalRadar.route) {
            PersonalRadarScreen(navController)
        }
        composable(
            route = Screen.RepositoryDetail.route + "/{owner}/{repo}",
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("repo") { type = NavType.StringType }
            )
        ) {
            RepositoryDetailScreen(navController)
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "仓库看板")
    object Topics : Screen("topics", "话题追踪")
    object Search : Screen("search", "搜索")
    object PersonalRadar : Screen("personal_radar", "我的雷达")
    object RepositoryDetail : Screen("repository_detail", "仓库详情")
}
