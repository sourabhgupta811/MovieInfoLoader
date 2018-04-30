package com.example.android.movieinfoloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    EditText edit_text=null;
    Disposable disposable;
    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    TextView emptyResult;
    ProgressBar progressBar;
    ArrayList<String[]> list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit_text=findViewById(R.id.edit_text);
        progressBar=findViewById(R.id.main_progressbar);
        emptyResult=findViewById(R.id.empty_result);
        recyclerView=findViewById(R.id.recycler_view);
        adapter=new MyRecyclerViewAdapter(this);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);




        findViewById(R.id.button).setOnClickListener(v ->   {
        if(!edit_text.getText().toString().equals("")) {
            emptyResult.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            disposable = getMovieSource("https://www.imdb.com/find?s=all&q=" + edit_text.getText().toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .flatMap(s -> findParticulars(s))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(strings -> {
                        Log.wtf("next element", Thread.currentThread().toString());
                        adapter.setMyDataList(list);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }, throwable -> {
                        Log.e("error", throwable.getLocalizedMessage());
                        progressBar.setVisibility(View.GONE);
                        emptyResult.setVisibility(View.VISIBLE);
                    }, () -> {
                        progressBar.setVisibility(View.GONE);
                        emptyResult.setVisibility(View.VISIBLE);
                    });
        }
        });
    }

    private Observable<String> getMovieSource(String movieName){
        return Observable.create(emitter -> {
            String line="";
            String data="";
            try {
                URL url = new URL(movieName);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    data += line;
                }
                reader.close();
                emitter.onNext(data);
            }
            catch(Exception e){
                emitter.onError(new Throwable());
            }
        });
    }
    
    private Observable<String[]> findParticulars(String s){
        if(!s.equals("") && !s.contains("404 Error - IMDb")) {
            ArrayList<String[]> dataList = new ArrayList<>();
            String[] list;
            Document doc = Jsoup.parse(s);
            Elements elements = doc.select("tr.findResult");
            for (Element movieList : elements) {
                String href = movieList.getElementsByTag("a").attr("href");
                String imageSrc = movieList.getElementsByTag("img").attr("src");
                String movieName = movieList.getElementsByTag("td").last().text();
                if (href.contains("title")) {
                    list = new String[3];
                    href = href.substring(7, 16);
                    list[0] = href;
                    list[1] = imageSrc;
                    list[2] = movieName;
                    Log.wtf("movieList", list.toString());
                    dataList.add(list);
                }
                this.list = dataList;
            }
            return Observable.fromIterable(dataList);
        }
        return Observable.empty();
    }

    @Override
    protected void onStop() {
        if(disposable!=null)
        disposable.dispose();
        super.onStop();
    }
}
