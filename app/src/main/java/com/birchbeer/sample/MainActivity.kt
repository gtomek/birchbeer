package com.birchbeer.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.birchbeer.sample.R.*
import com.birchbeer.sample.extensions.hide
import com.birchbeer.sample.extensions.show
import com.birchbeer.sample.ui.RootItemAdapter
import com.birchbeer.sample.ui.RootedResultTextView
import com.birchbeer.sample.ui.ScopedActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.barbuzz.beerprogressview.BeerProgressView

class MainActivity : ScopedActivity() {

    private var infoDialog: AlertDialog? = null
    private val rootItemAdapter = RootItemAdapter()
    private val checkForRoot = CheckForRootWorker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        initView()
        resetView()
    }

    private fun initView() {
        setSupportActionBar(findViewById(id.toolbar))
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { checkForRoot() }
        val rootResultsRecycler: RecyclerView = findViewById(R.id.rootResultsRecycler)
        rootResultsRecycler.layoutManager = LinearLayoutManager(this)
        rootResultsRecycler.adapter = rootItemAdapter
    }

    private fun resetView() {
        val progressView: BeerProgressView = findViewById(R.id.progressView)
        progressView.max = 100
        progressView.beerProgress = 0
        progressView.show()

        val isRootedTextView: View = findViewById(R.id.isRootedTextView)
        isRootedTextView.hide()
        rootItemAdapter.clear()
    }

    private fun checkForRoot() {
        resetView()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.hide()

        launch {
            val results = checkForRoot.invoke()
            animateResults(results)
        }
    }

    /**
     * There's probably a much easier way of doing this using View Property animators? :S
     */
    private fun animateResults(results: List<RootItemResult>) {
        val isRooted = results.any { it.result }
        // this allows us to increment the progress bar for x number of times for each of the results
        // all in the effort to smooth the animation
        val multiplier = 10
        val progressView: BeerProgressView = findViewById(R.id.progressView)
        progressView.max = results.size * multiplier

        launch {
            withContext(Dispatchers.IO) {
                results.forEachIndexed { index, rootItemResult ->
                    for (i in 1..multiplier) {
                        // arbitrary delay, 50 millis seems to look ok when testing with 12 results
                        delay(50)
                        // post the UI updates in the UI thread
                        withContext(Dispatchers.Main) {
                            progressView.beerProgress = progressView.beerProgress + 1

                            // only add to the once we hit the multiplier
                            if (i == multiplier) {
                                rootItemAdapter.add(rootItemResult)
                            }
                            //is it the end of the results
                            if (index == results.size - 1) {
                                onRootCheckFinished(isRooted = isRooted)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            id.action_github -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(GITHUB_LINK)
                startActivity(i)
                true
            }
            id.action_info -> {
                showInfoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInfoDialog() {
        //do nothing if already showing
        if (infoDialog?.isShowing != true) {
            infoDialog = AlertDialog.Builder(this)
                .setTitle(string.app_name)
                .setMessage(string.info_details)
                .setCancelable(true)
                .setPositiveButton("ok") { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("More info") { dialog, _ ->
                    dialog.dismiss()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(GITHUB_LINK)
                        )
                    )
                }
                .create()
            infoDialog?.show()
        }
    }

    private fun onRootCheckFinished(isRooted: Boolean) {
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.show()
        val isRootedTextView: RootedResultTextView = findViewById(R.id.isRootedTextView)
        isRootedTextView.update(isRooted = isRooted)
        isRootedTextView.show()
    }

    companion object {
        private const val GITHUB_LINK = "https://github.com/gtomek/birchbeer"
    }
}

