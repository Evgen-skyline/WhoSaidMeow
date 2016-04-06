package evgenskyline.whosaidmeow;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.rtp.AudioStream;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SoundPool mSoundPool;
    private int mCatSound, mChickenSound, mDogSound, mDonkeySpund, mDuckSound, mCowSound;
    private int mStreamID;
    private AssetManager mAssetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton mCatImButton = (ImageButton)findViewById(R.id.mCatButton);
        mCatImButton.setOnClickListener(mOnClickListener);
        ImageButton mChickenImButton = (ImageButton)findViewById(R.id.mChickenButton);
        mChickenImButton.setOnClickListener(mOnClickListener);
        ImageButton mDogImButton = (ImageButton)findViewById(R.id.mDogButton);
        mDogImButton.setOnClickListener(mOnClickListener);
        ImageButton mDonkeyImButton = (ImageButton)findViewById(R.id.mDonkeyButton);
        mDonkeyImButton.setOnClickListener(mOnClickListener);
        ImageButton mDuckImButton = (ImageButton)findViewById(R.id.mDuckButton);
        mDuckImButton.setOnClickListener(mOnClickListener);

        ImageButton mCowImButton = (ImageButton)findViewById(R.id.mCowButton);
        mCowImButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                if(eventAction == MotionEvent.ACTION_UP){
                    if(mStreamID > 0){
                        mSoundPool.stop(mStreamID);
                    }
                }
                if(eventAction == MotionEvent.ACTION_DOWN){
                    mStreamID = mPlaySound(mCowSound);
                }
                if(eventAction == MotionEvent.ACTION_CANCEL){
                    mSoundPool.stop(mStreamID);
                }
                return true;
            }
        });
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.mCatButton: mPlaySound(mCatSound); break;
                case R.id.mChickenButton: mPlaySound(mChickenSound); break;
                case R.id.mCowButton: mPlaySound(mCowSound); break;
                case R.id.mDogButton: mPlaySound(mDogSound); break;
                case R.id.mDonkeyButton: mPlaySound(mDonkeySpund); break;
                case R.id.mDuckButton: mPlaySound(mDuckSound); break;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool(){
        AudioAttributes mAudioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(mAudioAttrib)
                .build();
        //В Android 5.0 (API 21) вызов конструктора считается устаревшим.
        // На смену ему пришёл класс SoundPool.Builder
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool(){
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        //1 - максимальное количество потоков, который могут воспроизводится одновременно
        //2 - тип аудиопотока, это константа из класса AudioManager. Здесь чаще всего
        //    используется AudioManager.STREAM_MUSIC.
        //3 - качество кодирования. Сейчас этот параметр не используется
    }

    private int mPlaySound(int sound){
        if(sound > 0){
            mStreamID = mSoundPool.play(sound, 1, 1, 1, 0, 1);
            //soundID - идентификатор звука (который вернул load())
            //leftVolume - уровень громкости для левого канала (от 0.0 до 1.0)
            //rightVolume - уровень громкости для правого канала (от 0.0 до 1.0)
            //priority - приоритет потока (0 - самый низкий)
            //loop - количество повторов (0 - без повторов, (-1) - зациклен)
            //rate - скорость воспроизведения (от 0.5 до 2.0)
        }
        return mStreamID;
    }

    private int mLoadSound(String filename){
        AssetFileDescriptor afd;
        try{
            afd = mAssetManager.openFd(filename);
            //Загрузка из папки assets. Нужно знать путь к файлу
            //AssetManager - класс, предоставляющий доступ к файлам в директории assets.
            //AssetFileDescriptor - файловый дескриптор для файла из директории assets.
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "не могу загрузить файл " + filename,
                    Toast.LENGTH_LONG).show();
            return -1;
        }
        return mSoundPool.load(afd, 1);
        //Загрузка файла из указанного пути, второй параметр - приоритет
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Возобновить приостановленный поток (если поток был не на паузе, то метод не даст никакого результата)

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            //для до Android 5
            createOldSoundPool();
        }else {
            //Android 5+
            createNewSoundPool();
        }
        mAssetManager = getAssets();

        mCatSound = mLoadSound("cat.ogg");
        mChickenSound = mLoadSound("chicken.ogg");
        mCowSound = mLoadSound("cow.ogg");
        mDogSound = mLoadSound("dog.ogg");
        mDuckSound = mLoadSound("duck.ogg");
        mDonkeySpund = mLoadSound("sheep.ogg");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Приостановить воспроизведение

        mSoundPool.release();
        //даляет все загруженные звуки из SoundPool и освобождает память. После вызова этого
        // метода экземпляр класса SoundPool уже нельзя использовать. Метод используется при
        // выходе из программы, чтобы освободить ресурсы.

        mSoundPool = null;
    }

    //void setLoop(int streamID, int loop)
    //Установка количество повторов для потока

    //void setPriority(int streamID, int priority)
    //Установка приоритета для потока

    //void setRate(int streamID, float rate)
    //Установка скорости для потока

    //void setVolume(int streamID, float leftVolume, float rightVolume)
    //Установка громкости для потока

    //void autoPause()
    //Приостановить все активные потоки

    //void autoResume()
    //Возобновить все потоки

    //boolean unload(int soundID)
    //Удаляет звук из SoundPool. Метод возвращает true, если операция прошла успешно,
    // и false, если такого SoundID не существует


}
