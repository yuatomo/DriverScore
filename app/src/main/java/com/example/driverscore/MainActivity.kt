package com.example.driverscore

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import io.github.takusan23.jdcardreadercore.JDCardReaderCore
import io.github.takusan23.jdcardreadercore.JDCardReaderException
import io.github.takusan23.jdcardreadercore.data.JDCardData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var password = ""
        val errorText = findViewById<TextView>(R.id.error_text)
        val editText = findViewById<EditText>(R.id.password)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // 変更前のテキストがあればここで処理
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // テキストが変更された瞬間にここで処理
                password = charSequence.toString()
                // ここで取得したテキストを使って必要な処理を行います
            }

            override fun afterTextChanged(editable: Editable?) {
                // 変更後のテキストが確定した瞬間にここで処理
                if (password.length == 4) {
                    errorText.text = ""
                    lifecycleScope.launch {
                        // 暗証番号１のみ
                        try {
                            val cardData =
                                JDCardReaderCore.startGetCardData(this@MainActivity, password)
                            val cardNumber = cardData.jdCardDF1EF01Data.cardNumber
                            registerData(cardData)
                            val intent = Intent(this@MainActivity, InfoActivity::class.java)
                            intent.putExtra("cardNumber", cardNumber)
                            startActivity(intent)
                        } catch (e: JDCardReaderException) {
                            errorText.text = "暗証番号が違います\n3回間違えるとロックされます"
                        }
                    }
                } else {
                    errorText.text = "暗証番号は4桁で入力してください"
                }
            }
        })
    }

    fun registerData(cardData :JDCardData) {
        val cardNumber = cardData.jdCardDF1EF01Data.cardNumber
        val name = cardData.jdCardDF1EF01Data.name
        val kana = cardData.jdCardDF1EF01Data.yomi
        val location = cardData.jdCardDF1EF01Data.address
        val birthday = convertJapaneseEraToWesternDate(cardData.jdCardDF1EF01Data.birthday)
        val registeredAt = convertJapaneseEraToWesternDate(cardData.jdCardDF1EF01Data.publishDate)
        val endTimeAt = convertJapaneseEraToWesternDate(cardData.jdCardDF1EF01Data.endDate)
        val color = cardData.jdCardDF1EF01Data.cardColor
        val builder = Uri.Builder()
        val parameter = "?sql=INSERT IGNORE INTO user VALUES ($cardNumber, '$name', '$kana', '$location', '$birthday', '$registeredAt', '$endTimeAt', '$color')"
        val task = HttpAsyncLoader(this@MainActivity, parameter)
        task.execute(builder)
    }

    fun convertJapaneseEraToWesternDate(date: String): String {
        val japaneseEra = date.substring(0, 2)
        val year = date.substring(3, 5).toInt()
        val month = date.substring(7, 9).toInt()
        val day = date.substring(11, 13).toInt()
        val japaneseCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)

        when (japaneseEra) {
            "大正" -> japaneseCalendar.set(1911 + year, month - 1, day)
            "昭和" -> japaneseCalendar.set(1925 + year, month - 1, day)
            "平成" -> japaneseCalendar.set(1988 + year, month - 1, day)
            "令和" -> japaneseCalendar.set(2018 + year, month - 1, day)
            else -> throw IllegalArgumentException("Unsupported Japanese era: $japaneseEra")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        return sdf.format(japaneseCalendar.time)
    }
}