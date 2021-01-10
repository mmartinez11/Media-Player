package com.example.funcenter;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import com.example.ClipCommon.funPlaza;

public class MainActivity extends Service {

    //Notification Variables
    private Notification mNotification;
    private static final int NOTIFICATION_ID = 1;

    //Music Player Variables
    private MediaPlayer mediaPlayer;
    private static String CHANNEL_ID = "Fun Center Music";

    //Image Clip Variables
    public ArrayList<Bitmap> mImages;

    //Music Clip Variables
    public String[] musicName;
    public ArrayList<Integer> musicID;


    public void onCreate()
    {
        super.onCreate();
        createChannel();

        //Set the arrays that hold the values of the music clips and the images
        mImages = setImages();
        musicID = getMusicIDs();
        musicName = getResources().getStringArray(R.array.music_names);

        mNotification = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID).setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music is Playing").setTicker("Music is Playing!").build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    //Store the images in a bitmap array
    public ArrayList<Bitmap> setImages()
    {
        ArrayList<Bitmap> m = new ArrayList<>();

        Bitmap a = BitmapFactory.decodeResource(getResources(),R.drawable.civic);
        Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.focus);
        Bitmap c = BitmapFactory.decodeResource(getResources(),R.drawable.sienna);

        m.add(a);
        m.add(b);
        m.add(c);

        return m;
    }

    //Create a notification channel
    private void createChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String mDescription = "Music Player Channel";
            CharSequence mName = "Music Player";

            int mImportance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, mName, mImportance);

            mChannel.setDescription(mDescription);

            NotificationManager mManager = getSystemService(NotificationManager.class);
            mManager.createNotificationChannel(mChannel);
        }
    }

    //Get the Ids for the music clips
    public ArrayList<Integer> getMusicIDs()
    {
        ArrayList<Integer> temp = new ArrayList<>();

        temp.add(R.raw.happy);
        temp.add(R.raw.heroes);
        temp.add(R.raw.jazz);

        return temp;
    }


    //Set the methods for the service that will be bounded
    private final funPlaza.Stub mBinder = new funPlaza.Stub() {

        @Override
        public int test() throws RemoteException { return -99; }

        //Plays the song that the client requested to play
        public void playSongAtId(int i)
        {

            if(mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.stop();
            }

            mediaPlayer = MediaPlayer.create(MainActivity.this, musicID.get(i));

            if(mediaPlayer != null)
            {
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            }

            startForeground(NOTIFICATION_ID, mNotification);
        }

        //Pauses current song
        public void pause()
        {
            if(mediaPlayer != null)
            {
                mediaPlayer.pause();
            }
        }

        //Resumes playing current song
        public void resume()
        {
            if(mediaPlayer != null)
            {
                mediaPlayer.start();
            }
        }

        //Stops playing song
        public void stop()
        {
            if(mediaPlayer != null)
            {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer= null;
            }

            stopForeground(true);
            mediaPlayer = null;
        }

        //Closes the bounded service
        public void closeService()
        {
            if(mediaPlayer != null)
            {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer= null;
            }

            stopForeground(true);
            mediaPlayer = null;

            stopSelf();
        }

        //Send the list of songs to the client
        public String[] getSongList()
        {
            return musicName;
        }

        //Sends the image requested by the client
        public Bitmap getImages(int i)
        {
            Bitmap tmp = mImages.get(i);
            return tmp;
        }

    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        stopSelf();
        stopForeground(true);
    }
}