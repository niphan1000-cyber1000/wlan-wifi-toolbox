package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.FieldRefScreen
import com.example.ui.screens.LearningScreen
import com.example.ui.screens.UtilitiesScreen
import com.example.ui.theme.BottomNavBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var currentTab by remember { mutableStateOf(Tab.DIAGNOSTICS) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            NavigationBar(
              containerColor = BottomNavBg,
              modifier = Modifier.testTag("bottom_nav_bar")
            ) {
              NavigationBarItem(
                selected = currentTab == Tab.DIAGNOSTICS,
                onClick = { currentTab = Tab.DIAGNOSTICS },
                icon = { Icon(Icons.Default.Wifi, contentDescription = "Diagnostics") },
                label = { Text("Diagnostics", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = CyberCyan,
                  selectedTextColor = CyberCyan,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = BottomNavBg
                ),
                modifier = Modifier.testTag("nav_diagnostics")
              )
              NavigationBarItem(
                selected = currentTab == Tab.CALCULATORS,
                onClick = { currentTab = Tab.CALCULATORS },
                icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculators") },
                label = { Text("Calculators", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = CyberCyan,
                  selectedTextColor = CyberCyan,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = BottomNavBg
                ),
                modifier = Modifier.testTag("nav_calculators")
              )
              NavigationBarItem(
                selected = currentTab == Tab.FIELD_REF,
                onClick = { currentTab = Tab.FIELD_REF },
                icon = { Icon(Icons.Default.ElectricalServices, contentDescription = "Field Ref") },
                label = { Text("Field Ref", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = CyberCyan,
                  selectedTextColor = CyberCyan,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = BottomNavBg
                ),
                modifier = Modifier.testTag("nav_field_ref")
              )
              NavigationBarItem(
                selected = currentTab == Tab.UTILITIES,
                onClick = { currentTab = Tab.UTILITIES },
                icon = { Icon(Icons.Default.Build, contentDescription = "Utilities") },
                label = { Text("Utilities", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = CyberCyan,
                  selectedTextColor = CyberCyan,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = BottomNavBg
                ),
                modifier = Modifier.testTag("nav_utilities")
              )
              NavigationBarItem(
                selected = currentTab == Tab.LEARNING,
                onClick = { currentTab = Tab.LEARNING },
                icon = { Icon(Icons.Default.Book, contentDescription = "Learning") },
                label = { Text("Learning", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = CyberCyan,
                  selectedTextColor = CyberCyan,
                  unselectedIconColor = TextMuted,
                  unselectedTextColor = TextMuted,
                  indicatorColor = BottomNavBg
                ),
                modifier = Modifier.testTag("nav_learning")
              )
            }
          }
        ) { innerPadding ->
          Modifier.padding(innerPadding).let { _ ->
            // Scaffold innerPadding handling
            val paddedModifier = Modifier.padding(innerPadding)
            BoxWithPadding(modifier = paddedModifier, currentTab = currentTab)
          }
        }
      }
    }
  }
}

@Composable
fun BoxWithPadding(modifier: Modifier, currentTab: Tab) {
  androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
    when (currentTab) {
      Tab.DIAGNOSTICS -> DiagnosticsScreen()
      Tab.CALCULATORS -> CalculatorsScreen()
      Tab.FIELD_REF -> FieldRefScreen()
      Tab.UTILITIES -> UtilitiesScreen()
      Tab.LEARNING -> LearningScreen()
    }
  }
}

enum class Tab {
  DIAGNOSTICS,
  CALCULATORS,
  FIELD_REF,
  UTILITIES,
  LEARNING
}
