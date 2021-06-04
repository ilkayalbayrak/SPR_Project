package com.example.text2speechproject;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

//    private HashMap<String,String> TtsInfo;
    private TextToSpeech mTts;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Button mButtonSpeak;

    private final int ttsInstalled = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonSpeak = findViewById(R.id.button_speak);
        mEditText = findViewById(R.id.input_text);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        mTts = new TextToSpeech(this,this);
//        TtsInfo = new HashMap<>();
//        Locale[] avLanguages = Locale.getAvailableLocales();
//        Log.d("");
//        System.out.println("\n\n\n");
//        System.out.println("**************************************************** "+mTts.getLanguage());
//        if(avLanguages != null) {
//            for (Locale lang : avLanguages) {
//                System.out.println("**************************************************** " + lang.toString());
//
//            }
//        } else {
//            System.out.println("****************************************************############# " + "NULL");
//
//        }
//        System.out.println("\n\n\n");
        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int isLanguageAvailable = mTts.isLanguageAvailable(Locale.US);
            if (isLanguageAvailable != TextToSpeech.LANG_MISSING_DATA && isLanguageAvailable != TextToSpeech.LANG_NOT_SUPPORTED){
                mTts.setLanguage(Locale.US);
                mTts.setVoice(new Voice("en-us-x-sfg#female_2-local", Locale.US, Voice.QUALITY_VERY_HIGH,
                        Voice.LATENCY_NORMAL, false, new HashSet<>()));
                mButtonSpeak.setEnabled(true);
            } else {
                isLanguageAvailable = mTts.isLanguageAvailable(Locale.UK);
                if (isLanguageAvailable != TextToSpeech.LANG_MISSING_DATA && isLanguageAvailable != TextToSpeech.LANG_NOT_SUPPORTED){
                    mTts.setLanguage(Locale.UK);
                    mTts.setVoice(new Voice("en-gb-x-sfg#female_2-local", Locale.UK, Voice.QUALITY_VERY_HIGH,
                            Voice.LATENCY_NORMAL, false, new HashSet<>()));
                    mButtonSpeak.setEnabled(true);
                } else {
                    Toast.makeText(this, "Failed to set the main language for the TTS engine!"
                            + " Using default locale instead...", Toast.LENGTH_SHORT).show();

                    mTts.setLanguage(Locale.getDefault());
                    mTts.setVoice(new Voice("Default voice", Locale.getDefault(), Voice.QUALITY_VERY_HIGH,
                            Voice.LATENCY_NORMAL, false, new HashSet<>()));
                    mButtonSpeak.setEnabled(true);
                }
            }

        } else if (status == TextToSpeech.ERROR){
            Log.e("TTS", "Initialization failed");
            Toast.makeText(this,"Unfortunately, TTS engine initialization failed...", Toast.LENGTH_SHORT).show();
        }
    }
    private void selectLanguage(){
//        mTts.get
    }
    private void selectVoice(){}

    private void selectFont(){}

    private void checkTTSEngine(){
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, ttsInstalled);
    }

    private void speak() {
        // get the text input to be synthesised from the editText
        String text = mEditText.getText().toString();

        // add a seekbar to manipulate the pitch of the synthesis
        float pitch = (float) mSeekBarPitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;

        // add a seekbar to manipulate the speed of the synthesis
        float speed = (float) mSeekBarSpeed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;
        mTts.setPitch(pitch);
        mTts.setSpeechRate(speed);

        // QUEUE_FLUSH option  replaces an already playing synthesis with the new entry
        // if the user calls the speak
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // todo: do the checks such as isLanguageAvailable... and many others in onStart method
        // this method will be called after the onCreate method

        // todo; get available languages
//        Set<Locale> availableLanguages = TextToSpeech.;

//        assert availableLanguages != null;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }
}