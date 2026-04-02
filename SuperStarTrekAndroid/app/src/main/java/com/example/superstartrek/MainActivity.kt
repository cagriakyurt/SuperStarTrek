package com.example.superstartrek

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvConsole: TextView
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private lateinit var scrollView: ScrollView
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var game: StarTrekGame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvConsole = findViewById(R.id.tvConsole)
        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)
        scrollView = findViewById(R.id.scrollView)

        tvConsole.text = ""

        val ioHandler = object : IOHandler {
            override fun print(text: String) {
                runOnUiThread {
                    tvConsole.append(text)
                    scrollView.post {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }

            override fun println(text: String) {
                print("$text\n")
            }

            override fun clear() {
                runOnUiThread {
                    tvConsole.text = ""
                }
            }
        }

        game = StarTrekGame(ioHandler)

        val sendInput = {
            val inputStr = etInput.text.toString()
            etInput.text.clear()
            ioHandler.println(inputStr) 
            game.provideInput(inputStr)
        }

        btnSend.setOnClickListener {
            sendInput()
        }

        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendInput()
                true
            } else {
                false
            }
        }

        scope.launch {
            game.start()
        }
    }
}
