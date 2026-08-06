import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 建立 DataStore 單例實例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "note_prefs")

class NoteDataStore(private val context: Context) {

    companion object {
        // 定義 key 值，用於儲存與讀取
        val TITLE_KEY = stringPreferencesKey("note_title")
        val CONTENT_KEY = stringPreferencesKey("note_content")
    }

    // 讀取標題 (Flow 會持續監聽資料變化)
    val titleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TITLE_KEY] ?: ""
    }

    // 讀取內容
    val contentFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CONTENT_KEY] ?: ""
    }

    // 儲存資料
    suspend fun saveNote(title: String, content: String) {
        context.dataStore.edit { preferences ->
            preferences[TITLE_KEY] = title
            preferences[CONTENT_KEY] = content
        }
    }

    // 清空資料
    suspend fun clearNote() {
        context.dataStore.edit { preferences ->
            preferences.remove(TITLE_KEY)
            preferences.remove(CONTENT_KEY)
        }
    }
}