package com.aar.app.wsp.features.gamethemeselector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aar.app.wsp.R
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.commons.gone
import com.aar.app.wsp.commons.visible
import com.aar.app.wsp.custom.easyadapter.MultiTypeAdapter
import com.aar.app.wsp.custom.easyadapter.SimpleAdapterDelegate
import com.aar.app.wsp.databinding.ActivityGamePlayBinding
import com.aar.app.wsp.databinding.ActivityThemeSelectorBinding
import com.aar.app.wsp.features.FullscreenActivity
import com.aar.app.wsp.features.gamethemeselector.ThemeSelectorViewModel.ResponseType
import com.aar.app.wsp.model.GameTheme
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ThemeSelectorActivity : FullscreenActivity() {
    @JvmField
    @Inject
    var mViewModelFactory: ViewModelProvider.Factory? = null
    lateinit var viewModel: ThemeSelectorViewModel


    private val adapter = MultiTypeAdapter()

    private var mUpdateDisposable: Disposable? = null

    val binding by lazy {   ActivityThemeSelectorBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        (application as WordSearchApp).appComponent.inject(this)

        initViews()
        initRecyclerView()
        initViewModel()

        updateRevisionNumber()
        loadData()
    }

    private fun initViews() {
        binding.btnAllTheme.setOnClickListener {
            onItemClick(GameTheme.NONE.id)
        }
        binding.btnUpdate.setOnClickListener {
            onUpdateClick()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, mViewModelFactory).get(ThemeSelectorViewModel::class.java)
        viewModel.onGameThemeLoaded.observe(this) { gameThemes: List<GameThemeItem>? ->
            adapter.setItems(gameThemes)
            binding.rvThemes.visible()
            binding.progressBar.gone()
        }
    }

    private fun initRecyclerView() {
        adapter.addDelegate(
            GameThemeItem::class.java,
            R.layout.item_theme_list,
            object : SimpleAdapterDelegate.Binder<GameThemeItem> {
                override fun bind(model: GameThemeItem, holder: SimpleAdapterDelegate.ViewHolder?) {
                    holder?.find<TextView>(R.id.textTheme)?.text = model.name
                    holder?.find<TextView>(R.id.textCount)?.text =
                        getString(R.string.text_words)
                            .replace(":count".toRegex(), java.lang.String.valueOf(model.wordsCount))
                }
            },
            object : SimpleAdapterDelegate.OnItemClickListener<GameThemeItem> {
                override fun onClick(model: GameThemeItem, view: View?) {
                    onItemClick(model.id)
                }
            }
        )

        binding.rvThemes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding. rvThemes.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        binding.rvThemes.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        mUpdateDisposable?.dispose()
    }

    private fun onUpdateClick() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.btnUpdate.isEnabled = false
        mUpdateDisposable = viewModel.updateData()
            .subscribe({ responseType: ResponseType ->
                updateRevisionNumber()
                binding.loadingLayout.visibility = View.GONE
                binding.btnUpdate.isEnabled = true
                val message = if (responseType == ResponseType.NoUpdate) {
                    getString(R.string.up_to_date)
                } else {
                    getString(R.string.update_success)
                }
                Toast.makeText(
                    this@ThemeSelectorActivity,
                    message,
                    Toast.LENGTH_LONG)
                    .show()
            }
            ) {
                binding.loadingLayout.visibility = View.GONE
                binding.btnUpdate.isEnabled = true
                Toast.makeText(
                    this@ThemeSelectorActivity,
                    R.string.err_no_connect,
                    Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun loadData() {
        binding.rvThemes.gone()
        binding.progressBar.visible()
        viewModel.loadThemes()
    }

    @SuppressLint("CheckResult")
    private fun onItemClick(themeId: Int) {
        viewModel
            .checkWordAvailability(
                themeId,
                gridRowCount,
                gridColCount)
            .subscribe { available: Boolean ->
                if (available) {
                    val intent = Intent()
                    intent.putExtra(EXTRA_THEME_ID, themeId)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@ThemeSelectorActivity,
                        "No words data to use, please select another theme or change grid size",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateRevisionNumber() {
        binding.textRev.text =
            if (viewModel.lastDataRevision > 0) viewModel.lastDataRevision.toString()
            else "-"
    }

    private val gridRowCount: Int
        get() {
            val extras = intent.extras
            return extras?.getInt(EXTRA_ROW_COUNT) ?: 0
        }

    private val gridColCount: Int
        get() {
            val extras = intent.extras
            return extras?.getInt(EXTRA_COL_COUNT) ?: 0
        }

    companion object {
        const val EXTRA_THEME_ID = "game_theme_id"
        const val EXTRA_ROW_COUNT = "row_count"
        const val EXTRA_COL_COUNT = "col_count"
    }
}