package town.robin.toadua

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import town.robin.toadua.ui.Toadua
import town.robin.toadua.ui.ToaduaViewModel

private val idQueryRegex = "#([^ ]+)".toRegex()

class MainActivity : AppCompatActivity() {
    private val viewModel: ToaduaViewModel by viewModels {
        ToaduaViewModel.Factory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleIntent(intent)
        setContent { Toadua(viewModel) }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onActivityResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // TODO: bring back support for text selection intents
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                uri.fragment?.let { query ->
                    idQueryRegex.matchEntire(query)?.groupValues?.getOrNull(1)?.let { id ->
                        viewModel.setIdFilter(id)
                    }
                }
            }
        }
    }
}