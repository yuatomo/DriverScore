package com.example.driverscore

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info)

        val cardNumber = intent.getStringExtra("cardNumber").toString()
        viewViolationLog(cardNumber)
    }

    fun viewViolationLog(cardNumber :String) {
        val builder = Uri.Builder()
        val parameter =
            "?sql=SELECT u.name, SUM(c.point) AS totalPoints FROM user u LEFT JOIN history h ON u.cardNumber = h.cardNumber LEFT JOIN charge c ON h.chargeId = c.chargeId WHERE u.cardNumber = '$cardNumber' GROUP BY u.name"
        val parameter2 =
            "?sql=SELECT violationtime, charge, point FROM charge c LEFT JOIN history h ON c.chargeId = h.chargeId WHERE h.cardNumber = '$cardNumber' ORDER BY violationtime DESC"
        // Asyncタスククラスのインスタンスを作成し、実行する
        val task = HttpAsyncLoader(this@InfoActivity, parameter)
        task.execute(builder)
        val task2 = HttpAsyncLoader(this@InfoActivity, parameter2)
        task2.execute(builder)
    }

}