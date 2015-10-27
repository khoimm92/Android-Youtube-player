package kh.youtubeplayer;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by trongkhoi on 01/10/2015.
 */
public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.PlaylistItems.List query;

    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.playlistItems().list("id,snippet");
            query.setKey(Config.DEVELOPER_KEY);
//            query.setType("video");
            query.setMaxResults((long) 2);
            if (MainActivity.PAGETOKEN != null)
                query.setPageToken(MainActivity.PAGETOKEN);
            query.setFields("items(id,snippet/title,snippet/description,snippet/thumbnails/default/url),nextPageToken");

        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords){
        query.setPlaylistId(keywords);
        try{
            PlaylistItemListResponse response = query.execute();
            List<PlaylistItem> results = response.getItems();
            MainActivity.PAGETOKEN = response.getNextPageToken();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(PlaylistItem result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId());
                items.add(item);
            }
            return items;
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
            return null;
        }
    }
}
