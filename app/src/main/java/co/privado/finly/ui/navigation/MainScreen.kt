package co.privado.finly.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.privado.finly.ui.screens.accounts.AccountsScreen
import co.privado.finly.ui.screens.allowedapps.AllowedAppsScreen
import co.privado.finly.ui.screens.categories.CategoriesScreen
import co.privado.finly.ui.screens.home.HomeScreen
import co.privado.finly.ui.screens.more.MoreScreen
import co.privado.finly.ui.screens.review.ReviewScreen
import co.privado.finly.ui.screens.transactions.TransactionsScreen
import co.privado.finly.ui.theme.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import co.privado.finly.ui.screens.transaction_detail.TransactionDetailScreen

import co.privado.finly.ui.screens.history.HistoryScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.Home, "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.History, "Movim.", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    BottomNavItem(Routes.AllowedApps, "Apps", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem(Routes.More, "Más", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
)

@Composable
fun FinlyBottomNav(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorInk)
            .drawBehind {
                drawLine(
                    color = ColorHair,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(top = 10.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onNavigate(item.route) }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) ColorBrass.copy(alpha = 0.18f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (selected) ColorBrass else ColorSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.label,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.5.sp,
                        color = if (selected) ColorBone else ColorSlate
                    )
                )
            }
        }
    }
}

@Composable
fun FinlyFab(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = ColorBrass,
        contentColor = ColorOnBrass,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Movimiento", modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Movimiento",
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun MainScreen() {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isMainRoute = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = ColorInk,
        bottomBar = {
            if (isMainRoute) {
                FinlyBottomNav(
                    items = bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onNavigate = { route ->
                        nav.navigate(route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isMainRoute) {
                FinlyFab(onClick = { nav.navigate(Routes.AddTransaction) })
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.Home,
            modifier = Modifier.fillMaxSize().padding(padding).background(ColorInk),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Routes.Home) { HomeScreen(onNavigateToAccounts = { nav.navigate(Routes.Accounts) }, onTransactionClick = { id -> nav.navigate("${Routes.TransactionDetail}/$id") }) }
            composable(Routes.History) { HistoryScreen(onTransactionClick = { id -> nav.navigate("${Routes.TransactionDetail}/$id") }) }
            composable(
                route = "${Routes.TransactionDetail}/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) {
                TransactionDetailScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.AddTransaction) { TransactionsScreen(onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) }
            composable(Routes.Transactions) { TransactionsScreen(onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) } // fallback just in case
            composable(Routes.Accounts) { AccountsScreen() }
            composable(Routes.AllowedApps) { AllowedAppsScreen() }
            composable(Routes.More) {
                MoreScreen(
                    onNavigateToAccounts = { nav.navigate(Routes.Accounts) },
                    onNavigateToCategories = { nav.navigate(Routes.Categories) },
                    onNavigateToReview = { nav.navigate(Routes.ReviewQueue) }
                )
            }
            composable(Routes.Categories) { CategoriesScreen() }
            composable(Routes.ReviewQueue) { ReviewScreen() }
        }
    }
}
