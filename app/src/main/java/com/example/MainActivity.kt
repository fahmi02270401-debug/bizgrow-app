package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.BusinessRepository
import com.example.ui.BusinessViewModel
import com.example.ui.ViewModelFactory
import com.example.ui.screens.ERPAppContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & repository locally using standard Constructor pattern
        val database = AppDatabase.getDatabase(this)
        val repository = BusinessRepository(database)

        // Instantiate business view model with customized Factory
        val viewModel: BusinessViewModel by viewModels {
            ViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ERPAppContent(viewModel = viewModel)
                }
            }
        }
    }
}
