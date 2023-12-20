package com.example.driverscore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HttpAsyncLoader extends AsyncTask<Uri.Builder, Void, String> {
    private MainActivity mainActivity;
    private InfoActivity infoActivity;
    private AlarmReceiver alarmReceiver;
    String getUrl = "http://54.146.118.1/index.php";// "http://10.30.4.98/webapi.php/index.php"; // WebAPIのURL

    public HttpAsyncLoader(MainActivity activity, String parameter) {
        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
        this.getUrl += parameter;
    }
    public HttpAsyncLoader(InfoActivity activity, String parameter) {
        // 呼び出し元のアクティビティ
        this.infoActivity = activity;
        this.getUrl += parameter;
    }
    public HttpAsyncLoader(AlarmReceiver activity, String parameter) {
        // 呼び出し元のアクティビティ
        this.alarmReceiver = activity;
        this.getUrl += parameter;
    }

    @Override
    protected String doInBackground(Uri.Builder... builder) {
        String text = null;
        try {
            URL url = new URL(getUrl);
            System.out.println(url);
            //connectionのインスタンス
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //リクエストのメソッドを指定
            connection.setRequestMethod("GET");
            //通信開始
            connection.connect();
            //　レスポンスコードを戻る
            int responseCode = connection.getResponseCode();
            // レスポンスコードを判断する、OKであれば、進める
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                InputStream in = connection.getInputStream();
                String encoding = connection.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                StringBuilder result = new StringBuilder();
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                // 　クローズ
                bufReader.close();
                inReader.close();
                in.close();
                // アウトプット
                System.out.println("result:\n" + result);
                text = String.valueOf(result);
            }
        } catch (Exception e) {
            System.out.println("エラー:");
            e.printStackTrace();
        }
        return text;
    }

    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {
        // Jackson ObjectMapperを使用してJSONオブジェクトに変換
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            if (jsonNode != null && getUrl.contains("SELECT u.name")) {
                TextView user_name = infoActivity.findViewById(R.id.user_name);
                user_name.setText(jsonNode.get(0).get("name").asText());
                TextView point_display = infoActivity.findViewById(R.id.point_display);
                if(jsonNode.get(0).get("totalPoints").asText().equals("null")) {
                    point_display.setText("0");
                } else {
                    point_display.setText(jsonNode.get(0).get("totalPoints").asText());
                }
            }
            if (jsonNode != null && getUrl.contains("SELECT violationDate")) {
                for(int i = 0; i < jsonNode.size(); i++) {
                    LinearLayout parentLayout = infoActivity.findViewById(R.id.LinearLayout);
                    TextView day_row = infoActivity.findViewById(R.id.day_row);
                    if (i != 0) {
                        TextView textView = new TextView(this.infoActivity);
                        textView.setId(View.generateViewId()); // ユニークなIDを生成
                        textView.setText(jsonNode.get(i).get("violationDate").asText());
                        textView.setLayoutParams(day_row.getLayoutParams());
                        textView.setPadding(day_row.getPaddingLeft(), day_row.getPaddingTop(),
                                day_row.getPaddingRight(), day_row.getPaddingBottom());
                        textView.setTypeface(day_row.getTypeface());
                        textView.setMaxWidth(825);
                        textView.setTextSize(20);
                        textView.setTextColor(day_row.getTextColors());
                        textView.setTextAlignment(day_row.getTextAlignment());
                        textView.setTypeface(day_row.getTypeface(), day_row.getTypeface().getStyle());

                        TextView textView2 = new TextView(this.infoActivity);
                        textView2.setId(View.generateViewId()); // ユニークなIDを生成
                        textView2.setText(jsonNode.get(i).get("charge").asText());
                        textView2.setLayoutParams(day_row.getLayoutParams());
                        textView2.setPadding(day_row.getPaddingLeft(), day_row.getPaddingTop(),
                                day_row.getPaddingRight(), day_row.getPaddingBottom());
                        textView2.setTypeface(day_row.getTypeface());
                        textView2.setMaxWidth(825);
                        textView2.setTextSize(20);
                        textView2.setTextColor(day_row.getTextColors());
                        textView2.setTextAlignment(day_row.getTextAlignment());
                        textView2.setTypeface(day_row.getTypeface(), day_row.getTypeface().getStyle());

                        TextView textView3 = new TextView(this.infoActivity);
                        textView3.setId(View.generateViewId()); // ユニークなIDを生成
                        textView3.setText(jsonNode.get(i).get("point").asText() + "点");
                        textView3.setLayoutParams(day_row.getLayoutParams());
                        textView3.setPadding(day_row.getPaddingLeft(), day_row.getPaddingTop(),
                                day_row.getPaddingRight(), day_row.getPaddingBottom());
                        textView3.setTypeface(day_row.getTypeface());
                        textView3.setMaxWidth(825);
                        textView3.setTextSize(20);
                        textView3.setTextColor(day_row.getTextColors());
                        textView3.setTextAlignment(day_row.getTextAlignment());
                        textView3.setTypeface(day_row.getTypeface(), day_row.getTypeface().getStyle());

                        // 生成したTextViewを追加
                        parentLayout.addView(textView);
                        parentLayout.addView(textView2);
                        parentLayout.addView(textView3);

                        View lineView = new View(this.infoActivity);
                        lineView.setId(View.generateViewId());
                        lineView.setLayoutParams(new ConstraintLayout.LayoutParams(825, 3));
                        lineView.setBackgroundColor(Color.parseColor("#51413E"));
                        lineView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                        parentLayout.addView(lineView);
                    } else {
                        TextView violation_row = infoActivity.findViewById(R.id.violation_row);
                        TextView point_row = infoActivity.findViewById(R.id.point_row);
                        day_row.setText(jsonNode.get(i).get("violationDate").asText());
                        violation_row.setText(jsonNode.get(i).get("charge").asText());
                        point_row.setText(jsonNode.get(i).get("point").asText() + "点");

                        View lineView = new View(this.infoActivity);
                        lineView.setId(View.generateViewId());
                        lineView.setLayoutParams(new ConstraintLayout.LayoutParams(825, 3));
                        lineView.setBackgroundColor(Color.parseColor("#51413E"));
                        lineView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                        parentLayout.addView(lineView);
                    }
                }
            }
            if (jsonNode != null && getUrl.contains("SELECT *")) {
                for(int i = 0; i < jsonNode.size(); i++) {
                    String dateTimeString = jsonNode.get(i).get("startDate").asText() + " " + jsonNode.get(i).get("startTime").asText();
                    int notificationId = jsonNode.get(i).get("notificationId").asInt();
                    String title = jsonNode.get(i).get("notificationTitle").asText();
                    String content = jsonNode.get(i).get("notification").asText();
                    String color = jsonNode.get(i).get("importance").asText();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    try {
                        // 文字列を Date オブジェクトに変換
                        Date date = sdf.parse(dateTimeString);

                        // Date オブジェクトを Calendar オブジェクトに変換
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        // Calendar オブジェクトからミリ秒を取得
                        long timeInMillis = calendar.getTimeInMillis();

                        // アラームを設定する
                        setAlarm(timeInMillis, notificationId, title, content, color);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAlarm(long timeInMillis, int notificationId, String title, String content, String color) {
        System.out.println(notificationId);
        System.out.println(timeInMillis);
        AlarmManager alarmManager = (AlarmManager) this.mainActivity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.mainActivity, AlarmReceiver.class);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("color", color);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mainActivity, notificationId, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent);

        // アラームをセット
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
//        }
    }
}

