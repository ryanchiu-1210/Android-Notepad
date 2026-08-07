package com.example.androidnotepad

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidnotepad.ui.theme.AndroidNotepadTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidNotepadTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Simple Note", fontSize = 40.sp) }
                        )
                    }
                ) { innerPadding ->
                    notepad(modifier = Modifier.padding(innerPadding))

                }
            }
        }
    }


    @Composable
    fun notepad(modifier: Modifier = Modifier) {

        val context = LocalContext.current

        // 2. 初始化 DataStore 工具類別
        val noteDataStore = remember { NoteDataStore(context) }

        // 3. 監聽 DataStore 中的數據流
        val savedTitle by noteDataStore.titleFlow.collectAsState(initial = "")
        val savedContent by noteDataStore.contentFlow.collectAsState(initial = "")

        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        // 4. 當從 DataStore 讀取到歷史紀錄時，自動填入輸入框
        LaunchedEffect(savedTitle, savedContent) {
            if (title.isEmpty() && savedTitle.isNotEmpty()) title = savedTitle
            if (content.isEmpty() && savedContent.isNotEmpty()) content = savedContent
        }

        // 建立用於執行協程儲存任務的 Scope
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // 避開狀態列
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Simple Note",
                style = MaterialTheme.typography.headlineMedium
            )

            // 標題輸入框
            OutlinedTextField(
                value = title,
                onValueChange = { newTitle ->
                    title = newTitle
                    // 輸入時自動儲存到 DataStore
                    scope.launch {
                        noteDataStore.saveNote(title, content)
                    }
                },
                label = { Text("title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 內容輸入框
            OutlinedTextField(
                value = content,
                onValueChange = { newContent ->
                    content = newContent
                    // 輸入時自動儲存到 DataStore
                    scope.launch {
                        noteDataStore.saveNote(title, content)
                    }
                },
                label = { Text("Enter...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick={
                    scope.launch {
                        noteDataStore.saveNote(title,content)
                    }
                },
                modifier=Modifier.fillMaxWidth()
            ){
                Text("Save")
            }

            // 清空按鈕
            Button(
                onClick = {
                    title = ""
                    content = ""
                    // 點擊清空時同步抹除 DataStore 中的紀錄
                    scope.launch {
                        noteDataStore.clearNote()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }

        }
    }
}
