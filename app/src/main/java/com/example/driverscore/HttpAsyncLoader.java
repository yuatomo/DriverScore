package com.example.driverscore;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAsyncLoader extends AsyncTask<Uri.Builder, Void, String> {
    private MainActivity mainActivity;
    String getUrl = "http://192.168.11.11/webapi.php/index.php";// "http://54.146.118.1/index.php"; // WebAPIのURL

    public HttpAsyncLoader(MainActivity activity, String parameter) {
        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
        this.getUrl += parameter;
    }

    @Override
    protected String doInBackground(Uri.Builder... builder) {

        String text = null;
        try {
            URL url = new URL(getUrl);
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
                TextView textView2 = (TextView) mainActivity.findViewById(R.id.textView2);
                textView2.setText(jsonNode.get(0).get("name").asText());
                TextView point_display2 = (TextView) mainActivity.findViewById(R.id.point_display2);
                if(jsonNode.get(0).get("totalPoints").asText().equals("null")) {
                    point_display2.setText("0 POINT");
                } else {
                    point_display2.setText(jsonNode.get(0).get("totalPoints").asText() + " POINT");
                }
            }
            if (jsonNode != null && getUrl.contains("SELECT violationtime")) {
                TextView[] day_row = new TextView[5];
                day_row[0] = (TextView) mainActivity.findViewById(R.id.day_row_1);
                day_row[1] = (TextView) mainActivity.findViewById(R.id.day_row_2);
                day_row[2] = (TextView) mainActivity.findViewById(R.id.day_row_3);
                day_row[3] = (TextView) mainActivity.findViewById(R.id.day_row_4);
                day_row[4] = (TextView) mainActivity.findViewById(R.id.day_row_5);
                TextView[] violation_row = new TextView[5];
                violation_row[0] = (TextView) mainActivity.findViewById(R.id.violation_row_1);
                violation_row[1] = (TextView) mainActivity.findViewById(R.id.violation_row_2);
                violation_row[2] = (TextView) mainActivity.findViewById(R.id.violation_row_3);
                violation_row[3] = (TextView) mainActivity.findViewById(R.id.violation_row_4);
                violation_row[4] = (TextView) mainActivity.findViewById(R.id.violation_row_5);
                TextView[] point_row = new TextView[5];
                point_row[0] = (TextView) mainActivity.findViewById(R.id.point_row_1);
                point_row[1] = (TextView) mainActivity.findViewById(R.id.point_row_2);
                point_row[2] = (TextView) mainActivity.findViewById(R.id.point_row_3);
                point_row[3] = (TextView) mainActivity.findViewById(R.id.point_row_4);
                point_row[4] = (TextView) mainActivity.findViewById(R.id.point_row_5);
                for(int i = 0; i < jsonNode.size(); i++) {
                    day_row[i].setText(jsonNode.get(i).get("violationtime").asText());
                    violation_row[i].setText(jsonNode.get(i).get("charge").asText());
                    point_row[i].setText(jsonNode.get(i).get("point").asText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}