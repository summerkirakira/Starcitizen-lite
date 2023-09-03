package vip.kirakira.starcitizenlite.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.gyf.immersionbar.ImmersionBar
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.adapters.model.SuggestionItem
import com.paulrybitskyi.persistentsearchview.listeners.OnSuggestionChangeListener
import com.paulrybitskyi.persistentsearchview.utils.SuggestionCreationUtil
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.GameTranslation
import vip.kirakira.starcitizenlite.database.User
import vip.kirakira.starcitizenlite.database.getDatabase

class LocalizationSearchActivity : AppCompatActivity() {

    lateinit var searchView: PersistentSearchView
    private lateinit var filteredGameTranslations: LiveData<List<GameTranslation>>
    private lateinit var gameTranslationList: LiveData<List<GameTranslation>>
    private lateinit var chineseContentTextview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localization_search)
        val database = getDatabase(application)
        val immersionBar = ImmersionBar.with(this)
        immersionBar.transparentBar()
            .fullScreen(true)
            .statusBarDarkFont(true)
            .init()

        gameTranslationList = database.gameTranslationDao.getAll()
        applyFilter("")

        searchView = findViewById(R.id.localizationSearchView)
        chineseContentTextview = findViewById(R.id.localizationSearchResult)
        with(searchView) {
            setOnLeftBtnClickListener {
                // Handle the left button click
                if (searchView.isExpanded) {
                    searchView.collapse()
                } else {
                    finish()
                }
            }
            setOnClearInputBtnClickListener {
                // Handle the clear input button click
            }

            setOnSearchConfirmedListener { searchView, query ->
                // Handle a search confirmation. This is the place where you'd
                // want to save a new query and perform a search against your
                // data provider.
            }

            setOnSearchQueryChangeListener { searchView, oldQuery, newQuery ->
                // Handle a search query change. This is the place where you'd
                // want load new suggestions based on the newQuery parameter.
                applyFilter(newQuery)
            }

            setOnSuggestionChangeListener(object : OnSuggestionChangeListener {

                override fun onSuggestionPicked(suggestion: SuggestionItem) {
                    // Handle a suggestion pick event. This is the place where you'd
                    // want to perform a search against your data provider.
                    Log.d("LocalizationSearchActivity", "onSuggestionPicked: ${suggestion.itemModel.text}")
                    chineseContentTextview.text = getChineseByEnglish(suggestion.itemModel.text)

                }

                override fun onSuggestionRemoved(suggestion: SuggestionItem) {
                    // Handle a suggestion remove event. This is the place where
                    // you'd want to remove the suggestion from your data provider.
                }

            })

        }
    }

    override fun onBackPressed() {
        if (searchView.isExpanded) {
            searchView.collapse()
        } else {
            finish()
        }
    }

    private fun setSuggestions(queries: List<String>, expandIfNecessary: Boolean) {

        val suggestions: List<SuggestionItem> = SuggestionCreationUtil.asRegularSearchSuggestions(queries)

        searchView.setSuggestions(suggestions, expandIfNecessary)
    }

    private fun applyFilter(query: String) {
        filterGameTranslations(gameTranslationList, query)
    }

    private fun filterGameTranslations(gameTranslationLIst: LiveData<List<GameTranslation>>, query: String) {
        filteredGameTranslations = Transformations.map(gameTranslationLIst) { gameTranslations ->
            gameTranslations.filter {
                it.english.contains(query, true) && query.isNotEmpty()
            }
        }
        filteredGameTranslations.observe(this) { data ->
            if (data.isNotEmpty()) {
                searchView.hideProgressBar()
                var suggestions = data.map {
                    it.english
                }
//                if(suggestions.size > 10) {
//                    suggestions = suggestions.subList(0, 10)
//                }
                setSuggestions(suggestions, true)
            }
        }
    }

    private fun getChineseByEnglish(english: String): String {
        filteredGameTranslations.value?.forEach {
            if (it.english == english) {
                return modifyChineseContent(it.chinese)
            }
        }
        return "未找到汉化,以下为原文:\n$english"
    }

    private fun modifyChineseContent(text: String): String {
        var modified_text = text.replace("\\n", "\n")
        Log.d("LocalizationSearchActivity", "modifyChineseContent: $modified_text")
        return modified_text
    }
}