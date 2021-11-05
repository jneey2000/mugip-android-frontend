package com.giftmusic.mugip.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.adapter.SearchSongListViewAdapter
import com.giftmusic.mugip.models.response.SearchMusicItem
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class SearchMusicActivity : BaseActivity(), CoroutineScope {
    private lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private lateinit var searchEditText : EditText
    private lateinit var searchResultView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        job = Job()

        searchEditText = findViewById(R.id.song_search_bar)
        searchResultView = findViewById(R.id.search_song_result)
        searchResultView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        searchResultView.adapter = SearchSongListViewAdapter(this, arrayListOf())

        // 검색창 event listener
        searchEditText.addTextChangedListener(
            object : TextWatcher {
                var timer: CountDownTimer? = null

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    timer?.cancel()
                    timer = object : CountDownTimer(1000, 1500) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            getSearchResult("*${editable.toString()}*")
                        }
                    }.start()
                }
            }
        )
    }

    private fun getSearchResult(searchKeyword: String) {
        progressOn("음악 검색 결과 불러오는 중...")
        var searchUserFailed = true
        var errorMessage = ""
        val searchResult = ArrayList<SearchMusicItem>()
        launch {
            val url = URL(BuildConfig.server_url + "/search/music")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doInput = true
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val requestJson = HashMap<String, String>()
                requestJson["title"] = searchKeyword
                requestJson["artist"] = searchKeyword

                conn.outputStream.use { os ->
                    val input: ByteArray =
                        Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }

                when(conn.responseCode){
                    200 -> {
                        val inputStream = conn.inputStream
                        if(inputStream != null){
                            val returnBody = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                            val responseJson = JSONObject(returnBody.trim())
                            for (i in 0 until responseJson.getJSONArray("results").length()) {
                                val objects: JSONObject = responseJson.getJSONArray("results").getJSONObject(i)
                                val thumbnailURL = URL(objects.getJSONObject("thumbnail").getString("url"))
                                var thumbnailBitmap : Bitmap?

                                try {
                                    val connection = thumbnailURL.openConnection()
                                    connection.doInput = true
                                    connection.connect()

                                    val thumbnailInputStream = connection.getInputStream()
                                    thumbnailBitmap = BitmapFactory.decodeStream(thumbnailInputStream)
                                } catch (e : java.lang.Exception){
                                    thumbnailBitmap = null
                                }

                                searchResult.add(
                                    SearchMusicItem(
                                        objects.getString("title"), objects.getString("artist"), objects.getJSONObject("thumbnail").getString("url"), thumbnailBitmap
                                    )
                                )
                            }
                            searchUserFailed = false
                        }
                    }
                    else -> errorMessage = conn.responseCode.toString()
                }
            }
            catch (e : SocketTimeoutException){
                errorMessage = "연결 시간 초과 오류"
            }
            catch (e : Exception){
                Log.e("fetch search result error", e.toString())
                Log.e("fetch search result error", e.javaClass.kotlin.toString())
            }
            finally {
                conn.disconnect()
            }
            withContext(Dispatchers.Main){
                progressOFF()
                if(!searchUserFailed){
                    Log.d("result", searchResult.toString())
                    when {
                        searchEditText.text.toString().isEmpty() -> {
                            searchResultView.visibility = View.GONE
                        }
                        else -> {
                            searchResultView.adapter = SearchSongListViewAdapter(this@SearchMusicActivity, searchResult)
                            searchResultView.visibility = View.VISIBLE
                        }
                    }

                } else if(errorMessage.isNotEmpty()){
                    showFailDialog("음악 검색 실패", errorMessage)
                }
            }
        }
    }
}