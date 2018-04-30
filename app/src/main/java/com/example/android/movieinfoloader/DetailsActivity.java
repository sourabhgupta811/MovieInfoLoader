package com.example.android.movieinfoloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DetailsActivity extends AppCompatActivity {
    TextView movie_details;
    String title;
    int i=0;
    Disposable disposable;
    ProgressBar progressbar;
    String movie_name;
    ImageView poster_imageview;
    TextView movie_name_textview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        movie_details=findViewById(R.id.detail_textview);
        movie_details.setClickable(true);
        progressbar=findViewById(R.id.details_progressbar);
        poster_imageview=findViewById(R.id.movie_poster);
        movie_name_textview=findViewById(R.id.movie_name);
        movie_details.setMovementMethod(LinkMovementMethod.getInstance());
        int position=getIntent().getIntExtra("position",0);
        movie_name=MyRecyclerViewAdapter.myDataList.get(position)[2];
        title=MyRecyclerViewAdapter.myDataList.get(position)[0];
        loadData(title);
    }
    private void loadData(String title){
        progressbar.setVisibility(View.VISIBLE);
                disposable=getMovieSource("https://www.imdb.com/title/"+title+"/")
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .flatMap(source -> getMovieInfo(source))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(strings -> {
                            runOnUiThread(() -> {
                                progressbar.setVisibility(View.GONE);
                                movie_details.setText(Html.fromHtml(strings.get(1)));
                                movie_name_textview.setText(movie_name);
                                Glide.with(DetailsActivity.this).load(strings.get(0)).into(poster_imageview);
                            });
                        }, throwable -> {
                            Log.e("error",throwable.getLocalizedMessage());
                        });

    }
    private Observable<String> getMovieSource(String movieName){
        return Observable.create(emitter -> {
            String line="";
            String data="";
            URL url=new URL(movieName);
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            InputStream stream=connection.getInputStream();
            BufferedReader reader=new BufferedReader(new InputStreamReader(stream));
            while((line=reader.readLine())!=null){
                data+=line;
            }
            emitter.onNext(data);
        });
    }
    private Observable<List<String>> getMovieInfo(String source){
        List<String> data=new ArrayList<>();
        Document doc= Jsoup.parse(source);
        String posterSource="";
        String movieSummary="";
        Elements poster=doc.select("div.poster");
        Elements summary=doc.getElementsByClass("summary_text");
        Elements credit_summmary=doc.getElementsByClass("credit_summary_item");
        movieSummary=summary.get(0).text()+"\r\n\r\n";
        for(Element element:credit_summmary){
            movieSummary+=element.html().replace("<a href=\"","<a href=\"https://www.imdb.com");
        }
        for(Element element:poster){
            posterSource=element.getElementsByTag("img").attr("src");
        }
        data.add(posterSource);
        data.add(movieSummary);
        return Observable.just(data);
    }

    @Override
    protected void onStop() {
        if(disposable!=null)
        disposable.dispose();
        super.onStop();
    }
}

