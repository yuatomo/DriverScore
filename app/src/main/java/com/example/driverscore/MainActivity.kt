package com.example.driverscore

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import io.github.takusan23.jdcardreadercore.JDCardReaderCore
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

        lifecycleScope.launch {
            // 暗証番号１のみ
//            val cardData = JDCardReaderCore.startGetCardData(this@MainActivity, "0000")
//            val cardNumber = cardData.jdCardDF1EF01Data.cardNumber
//            registerData(cardData)
            val cardNumber = "258703660970"
            viewViolationLog(cardNumber)
        }

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
        val parameter = "?sql=INSERT IGNORE INTO user VALUES ($cardNumber, '$name', '$kana', '$location', '$birthday', '$registeredAt', '$endTimeAt', '$color', 0)"
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
            "大正" -> japaneseCalendar.set(1912 + year, month - 1, day)
            "昭和" -> japaneseCalendar.set(1926 + year, month - 1, day)
            "平成" -> japaneseCalendar.set(1989 + year, month - 1, day)
            "令和" -> japaneseCalendar.set(2019 + year, month - 1, day)
            else -> throw IllegalArgumentException("Unsupported Japanese era: $japaneseEra")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        return sdf.format(japaneseCalendar.time)
    }

    fun viewViolationLog(cardNumber :String) {
        val builder = Uri.Builder()
        val parameter =
            "?sql=SELECT u.name, SUM(c.point) AS totalPoints FROM user u LEFT JOIN history h ON u.cardNumber = h.cardNumber LEFT JOIN charge c ON h.chargeId = c.chargeId WHERE u.cardNumber = '$cardNumber' GROUP BY u.name"
        val parameter2 =
            "?sql=SELECT violationtime, charge, point FROM charge c LEFT JOIN history h ON c.chargeId = h.chargeId WHERE h.cardNumber = '$cardNumber' ORDER BY violationtime DESC"
        // Asyncタスククラスのインスタンスを作成し、実行する
        val task = HttpAsyncLoader(this@MainActivity, parameter)
        task.execute(builder)
        val task2 = HttpAsyncLoader(this@MainActivity, parameter2)
        task2.execute(builder)
    }
}