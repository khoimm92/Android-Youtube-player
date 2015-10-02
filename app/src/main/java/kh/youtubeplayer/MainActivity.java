package kh.youtubeplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class MainActivity extends YouTubeBaseActivity {
    public static String PAGETOKEN;
    EditText input;
    ListView listvideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = (EditText)findViewById(R.id.txtSearch);
//        btnSearch = (Button)findViewById(R.id.btnSearch);
        listvideo = (ListView)findViewById(R.id.listView);

//
//        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_ACTION_DONE){
//                    searchOnYoutube(input.getText().toString());
//                    return false;
//                }
//                return true;
//            }
//        });
    }

    public void searchVideo(View view) {
        searchOnYoutube(input.getText().toString());
    }

    private List<VideoItem> searchResults;
    private String token = "";
    private void searchOnYoutube(final String keywords){

        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(MainActivity.this);
                searchResults = yc.search(keywords);

//                Gson gson = new Gson();
//                System.out.println(gson.toJson(searchResults));
//                Toast.makeText(this, gson.toJson(searchResults), Toast.LENGTH_SHORT).show();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(){
//        Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_LONG).show();
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.activity_player, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.activity_player, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
//                try {
//                    URL thumbUrl = new URL(searchResult.getThumbnailURL());
//                    Bitmap bmp = BitmapFactory.decodeStream(thumbUrl.openConnection().getInputStream());
//                    thumbnail.setImageBitmap(bmp);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                return convertView;
            }
        };


        listvideo.setAdapter(adapter);
        listvideo.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                Toast.makeText(MainActivity.this, PAGETOKEN, Toast.LENGTH_SHORT).show();
//                if (scrollState == SCROLL_STATE_IDLE) {
//                    if (PAGETOKEN != null)//determine next page exists
//                        searchOnYoutube(input.getText().toString());
//                }
            }

            private int preLast;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                final int lastItem = firstVisibleItem + visibleItemCount;
//                if(lastItem == totalItemCount - 1) {
//                    if (PAGETOKEN != null)//determine next page exists
//                        searchOnYoutube(input.getText().toString());
//                }

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        if (PAGETOKEN != null)//determine next page exists
                            searchOnYoutube(input.getText().toString());
                            adapter.add();

                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class QueryYoutube extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            updateVideosFound();
            return null;
        }

//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
//        }
    }

//    private String GetJson(String url) {
//        String json = null;
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//            HttpResponse httpResponse = httpClient.execute(httpPost);
//            HttpEntity httpEntity = httpResponse.getEntity();
//            json = EntityUtils.toString(httpEntity, HTTP.UTF_8);
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return json;
//    }

}
