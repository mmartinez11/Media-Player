package com.example.funclient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.ClipCommon.funPlaza;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //XML Variables
    Button bPlay;
    Button bPause;
    Button bResume;

    Button bStopPlayback;

    Button bStartService;
    Button bStopService;

    ListView musicList;

    ImageView mImageView;
    Spinner spinner;

    ResolveInfo mInfo;
    ComponentName mComponent;

    Intent startIntent;

    private funPlaza mFunPlaza;
    int mSong;
    private boolean mIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Drop Down Menu
        spinner = (Spinner) findViewById(R.id.planets_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setEnabled(false);

        //Initialize List View
        musicList = (ListView) findViewById(R.id.clip_list_view);
        musicList.setOnItemClickListener(listItem);

        //Initialize Image View
        mImageView = (ImageView) findViewById(R.id.imageView) ;
        bStartService = (Button) findViewById(R.id.btn_startService);

        //Initialize Play Button
        bPlay = (Button) findViewById(R.id.btn_play);
        bPlay.setEnabled(false);

        //Initialize Pause Button
        bPause = (Button) findViewById(R.id.btn_pause);
        bPause.setEnabled(false);

        //Initialize Resume Button
        bResume = (Button) findViewById(R.id.btn_resume);
        bResume.setEnabled(false);

        //Initialize Stop Playback Button
        bStopPlayback = (Button) findViewById(R.id.btn_stopPlayBack);
        bStopPlayback.setEnabled(false);

        //Initialize Stop Service Button
        bStopService = (Button) findViewById(R.id.btn_stopService);
        bStopService.setEnabled(false);

    }

        //Starts Bound Service
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void startService(View view)
        {
            startIntent = new Intent(funPlaza.class.getName());
            mInfo = getPackageManager().resolveService(startIntent,0);

            mComponent = new ComponentName(mInfo.serviceInfo.packageName,mInfo.serviceInfo.name);
            startIntent.setComponent(mComponent);

            getApplicationContext().startForegroundService(startIntent);

            if (getApplicationContext().bindService(startIntent, MainActivity.this.mConnection , Context.BIND_AUTO_CREATE))
            {
                spinner.setEnabled(true);
                bStopService.setEnabled(true);

                bStartService.setEnabled(false);

                musicList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.music_names)));
            }
        }

        //Play the music selected from the list view
        public void playMusic(View view)
        {
            if(!mIsBound) { getApplicationContext().bindService(startIntent, MainActivity.this.mConnection, Context.BIND_AUTO_CREATE); }
            try {

                mFunPlaza.playSongAtId(mSong);

                bPlay.setEnabled(false);

                bPause.setEnabled(true);
                bStopPlayback.setEnabled(true);

            } catch (RemoteException e) { e.printStackTrace(); }

        }

        //Stop the music playback
        public void stopPlayback(View view)
        {
            try { mFunPlaza.stop();} catch (RemoteException e) { e.printStackTrace(); }
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;

            bResume.setEnabled(false);
            bPause.setEnabled(false);
            bStopPlayback.setEnabled(false);
        }

        //Resume The Music
        public void resumeMusic(View view) {
            try {

                mFunPlaza.resume();

                bPause.setEnabled(true);
                bResume.setEnabled(false);

            } catch (RemoteException e) { e.printStackTrace(); }
        }

        //Pause The Music
        public void pauseMusic(View view) {
            try {
                mFunPlaza.pause();

                bPause.setEnabled(false);
                bResume.setEnabled(true);

            } catch (RemoteException e) { e.printStackTrace();}
        }

        //Stop the Bound Service
        public void stopService(View view)
        {
            if(mIsBound){ getApplicationContext().unbindService(mConnection); }

            stopService(startIntent);
            mIsBound = false;

            bResume.setEnabled(false);
            bPause.setEnabled(false);
            bPlay.setEnabled(false);
            bStopService.setEnabled(false);
            bStopPlayback.setEnabled(false);

            spinner.setEnabled(false);
            bStartService.setEnabled(true);

            musicList.setAdapter(null);
        }

        //Get the Index of the selected Item on the List View
        public AdapterView.OnItemClickListener listItem = new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                mSong = i;
                bPlay.setEnabled(true);
            }
        };

        //Initializes Variables when the service is bounded and unbounded
        private final ServiceConnection mConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service)
            {
                mFunPlaza = funPlaza.Stub.asInterface(service);
                mIsBound = true;
            }

            public void onServiceDisconnected(ComponentName className) {

                mFunPlaza = null;
                mIsBound = false;
            }
        };

        //Get the index of the item picked in the Spinner (Dropped Down)
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            if(i != 0) {
                int t = i - 1;
                try {

                    Bitmap img = mFunPlaza.getImages(t);
                    mImageView.setImageBitmap(img);

                } catch (Exception e) {
                    System.out.println("Could not get Image!");
                }
            }
        }

        //Called when nothing is selected in spinner
        @Override
        public void onNothingSelected(AdapterView<?> adapterView)
        {
            Log.i("Spinner", "Nothing Selected");
        }

        //When Called unbound the service
        @Override
        public void onDestroy(){

            try {
                mFunPlaza.closeService();
            } catch (RemoteException e) { e.printStackTrace(); }

            if(mIsBound){ getApplicationContext().unbindService(mConnection); }

            stopService(startIntent);
            super.onDestroy();
        }
}