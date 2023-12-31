package com.example.driverscore

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.driverscore.AlarmReceiver.CHANNEL_ID

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

        // NotificationChannelの作成
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "DriverScore"
            val description = "アラーム通知用のチャンネル"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // NotificationManagerにチャンネルを登録
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = Uri.Builder()
        val parameter2 = "?sql=DELETE FROM notice WHERE startDate < ( NOW( ) - INTERVAL 1 WEEK )"
        val task2 = HttpAsyncLoader(this@MainActivity, parameter2)
        task2.execute(builder)

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
                            println(cardNumber)
                            insertAndSelectNotice(cardNumber);
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

    fun insertAndSelectNotice(cardNumber: String) {
        val builder = Uri.Builder()

        val parameter = "?sql=INSERT INTO notice (cardNumber, notificationTitle, notification, startDate, startTime, importance)" +
                " SELECT * FROM (SELECT '$cardNumber' AS cardNumber, '免許更新期限のお知らせ' AS notificationTitle, '免許更新期限が残り1ヶ月に迫っています。'" +
                " AS notification, DATE_SUB(endTimeAt, INTERVAL 1 MONTH) AS startDate , '12:00:00' AS startTime, 'red' AS importance" +
                " FROM user WHERE cardNumber = '$cardNumber') AS tmp" +
                " WHERE NOT EXISTS ( SELECT * FROM notice WHERE cardNumber = '$cardNumber'" +
                " AND notification = '免許更新期限が残り1ヶ月に迫っています。')"
        val task = HttpAsyncLoader(this@MainActivity, parameter)
        task.execute(builder)

        val parameter2 = "?sql=SELECT * FROM notice WHERE cardNumber IS NULL OR cardNumber = '$cardNumber'"
        val task2 = HttpAsyncLoader(this@MainActivity, parameter2)
        task2.execute(builder)
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
        val parameter = "?sql=INSERT IGNORE INTO user (cardNumber, name, kana, location, birthday, registeredAt, endTimeAt, color) VALUES ($cardNumber, '$name', '$kana', '$location', '$birthday', '$registeredAt', '$endTimeAt', '$color')"
        println(parameter)
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