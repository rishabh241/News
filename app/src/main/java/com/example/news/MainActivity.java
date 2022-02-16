package com.example.news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.icu.text.CaseMap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView list;
    ArrayList title = new ArrayList();
    ArrayList link = new ArrayList();
    ArrayAdapter adapter;
    SQLiteDatabase database;
    public void updatelist(){
        Cursor c= database.rawQuery("SELECT * FROM news",null);
        int titleIndex = c.getColumnIndex("title");
        int linkIndex = c.getColumnIndex("link");
        if(c.moveToFirst()){
            title.clear();
            link.clear();
            do {
                title.add(c.getString(titleIndex));
                link.add(c.getString(linkIndex));
            }while (c.moveToNext());
        }
    }
    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    result+=current;
                    data = reader.read();
                }
                JSONObject jsonObject = new JSONObject(result);
                String results =jsonObject.getString("results");
                JSONArray jsonArray = new JSONArray(results);
                int articles = 100;
                if(jsonArray.length()<100){
                    articles = jsonArray.length();
                }
                database.execSQL("DELETE FROM news");
                for(int i=0;i<articles;i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
//                    Log.i("title",jsonObject1.getString("title"));
                    String Title = jsonObject1.getString("title");
                    String Link = jsonObject1.getString("link");
                    String sql = "INSERT INTO news(title,link) VALUES(?,?)";
                    SQLiteStatement statement = database.compileStatement(sql);
                    statement.bindString(1,Title);
                    statement.bindString(2,Link);
                    statement.execute();
                    Log.i("Topics", Title);
                }




                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updatelist();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.list);
        database = this.openOrCreateDatabase("News",MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS news(id Integer PRIMARY KEY,title VARCHAR,link VARCHAR) ");
        DownloadTask task = new DownloadTask();
        String result = "";
        try{
//            task.execute("https://newsdata.io/api/1/news?apikey=pub_411086bd435c1364e23e308cee276195dff5&q=indian ");

//            result = task.execute("https://newsdata.io/api/1/news?apikey=pub_411086bd435c1364e23e308cee276195dff5&country=in&language=en,hi&category=politics,sports").get();
            result = task.execute("https://newsdata.io/api/1/news?apikey=pub_411086bd435c1364e23e308cee276195dff5&country=in&language=en&category=business,entertainment,politics,sports,top ").get();
        }catch (Exception e){
            e.printStackTrace();
        }
        adapter =new ArrayAdapter(this, android.R.layout.simple_list_item_1,title);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),newspadh.class);
                intent.putExtra("URL", String.valueOf(link.get(i)));
                startActivity(intent);
            }
        });
        updatelist();
    }
}