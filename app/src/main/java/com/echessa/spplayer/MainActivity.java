package com.echessa.spplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Track> mListItems;
    private SCTrackAdapter mAdapter;
    private TextView mSelectedTrackTitle;
    private ImageView mSelectedTrackImage;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayerControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerControl.setImageResource(R.drawable.ic_play);
            }
        });

        mListItems = new ArrayList<Track>();
        ListView listView = (ListView)findViewById(R.id.track_list_view);
        mAdapter = new SCTrackAdapter(this, mListItems);
        listView.setAdapter(mAdapter);

        mSelectedTrackTitle = (TextView)findViewById(R.id.selected_track_title);
        mSelectedTrackImage = (ImageView)findViewById(R.id.selected_track_image);
        mPlayerControl = (ImageView)findViewById(R.id.player_control);

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = mListItems.get(position);

                mSelectedTrackTitle.setText(track.getTitle());
                Picasso.with(MainActivity.this).load(track.getArtworkURL()).into(mSelectedTrackImage);

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                try {
                    mMediaPlayer.setDataSource(track.getStreamURL() + "?client_id=" + Config.CLIENT_ID);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        SCService scService = SoundCloud.getService();
        scService.getRecentTracks(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()), new Callback<List<Track>>() {
            @Override
            public void success(List<Track> tracks, Response response) {
                loadTracks(tracks);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Error: " + error);
            }
        });
    }

    private void loadTracks(List<Track> tracks) {
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }

    private void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.ic_play);
        } else {
            mMediaPlayer.start();
            mPlayerControl.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
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
}
