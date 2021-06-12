package com.example.text2speechproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

//    private HashMap<String,String> TtsInfo;
    private static final String TTS_SERIALIZATION_KEY = "tts_serialization_key";
    private TextToSpeech mTts;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Button mButtonSpeak;
    private Button mButtonRandom;
    private AutoCompleteTextView mAutoCompleteLanguages, mAutoCompleteVoices;
    private TextView mHighlightedTextDisplay;

    private boolean isAppReady = false;

    private final HashMap<Locale, List<String>> voiceOptionsList = new HashMap<>();
    private final List<String> usVoices = new ArrayList<>();
    private final List<String> ukVoices = new ArrayList<>();
    private final List<String> frenchVoices = new ArrayList<>();
    private final List<String> italianVoices = new ArrayList<>();
    private final List<String> germanVoices = new ArrayList<>();

    private final int ttsInstalled = 0;

//    private String currentLanguage = "English-US";

    private String currentLanguage;
    private String currentVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonSpeak = findViewById(R.id.button_speak);
        mButtonRandom = findViewById(R.id.button_random_test);
        mEditText = findViewById(R.id.input_text);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        mHighlightedTextDisplay = findViewById(R.id.textView_highlight);
//        mRadioGroupLanguages = findViewById(R.id.radioGroup_languages);
//        mRadioGroupVoices = findViewById(R.id.radioGroup_voices);
        mAutoCompleteLanguages = findViewById(R.id.autoComplete_languages);
        mAutoCompleteVoices = findViewById(R.id.autoComplete_voices);


        
            ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this,R.array.array_languages,R.layout.dropdown_item);
            mAutoCompleteLanguages.setAdapter(adapterLanguage);
            mAutoCompleteLanguages.setOnItemClickListener((parent, view, position, id) -> {
                currentLanguage = parent.getItemAtPosition(position).toString();
                Log.e("LANGUAGE_SELECTION", "LANGUAGE NAME: "+ currentLanguage);
                setTTSLanguage();

            });
        ArrayAdapter<CharSequence> adapterVoice = ArrayAdapter.createFromResource(this,R.array.array_voices,R.layout.dropdown_item);
        mAutoCompleteVoices.setAdapter(adapterVoice);
        mAutoCompleteVoices.setOnItemClickListener((parent, view, position, id) -> {
            currentVoice = parent.getItemAtPosition(position).toString();
            Log.e("VOICE_SELECTION", "VOICE NAME: "+ currentVoice);
            setTTSVoice(this.getUserSelectedLanguage(currentLanguage));

        });


        mHighlightedTextDisplay.setText(mEditText.getText().toString());
        mButtonSpeak.setOnClickListener(v -> speak());
        mButtonRandom.setOnClickListener(v -> {
            Log.e("TESTING_Fragment","fragment button clicked");
            readTextFromFile();
        });



        usVoices.add("en-us-x-sfg#female_1-local");
        usVoices.add("en-us-x-sfg#male_1-local");
        voiceOptionsList.put(Locale.US, usVoices);

        ukVoices.add("en-gb-x-rjs#female_1-local");
        ukVoices.add("en-gb-x-rjs#male_1-local");
        voiceOptionsList.put(Locale.UK, ukVoices);

        frenchVoices.add("fr-fr-x-vlf#female_1-local");
        frenchVoices.add("fr-fr-x-vlf#male_1-local");
        voiceOptionsList.put(Locale.FRENCH, frenchVoices);

        italianVoices.add("it-it-x-kda#female_1-local");
        italianVoices.add("it-it-x-kda#male_1-local");
        voiceOptionsList.put(Locale.ITALIAN, italianVoices);

        germanVoices.add("de-de-x-nfh#female_1-local");
        germanVoices.add("de-de-x-nfh#male_1-local");
        voiceOptionsList.put(Locale.GERMAN, germanVoices);

        checkTTSEngine();

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            printOutSupportedLanguages();
            setTTSLanguage();


            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    runOnUiThread(()-> mHighlightedTextDisplay.setVisibility(View.VISIBLE));

                }

                @Override
                public void onDone(String utteranceId) {
                    mHighlightedTextDisplay.setVisibility(View.INVISIBLE);
                    mHighlightedTextDisplay.setText("");

                }

                @Override
                public void onError(String utteranceId) {
                    Log.e("UTTERANCE_ERROR", "An Error occurred while synthesizing the given text...");
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context,
                            "An Error occurred while synthesizing the given text...",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    mHighlightedTextDisplay.setVisibility(View.INVISIBLE);

                }

                // this method requires min API 26, it could be used for functionalities
                // such as highlighting the part of the text that is being synthesized
                // However, this app is min API 23

                @RequiresApi(Build.VERSION_CODES.O)
                @Override
                public void onRangeStart(String utteranceId, int start, int end, int frame) {
                    super.onRangeStart(utteranceId, start, end, frame);

                    Log.i("Current_Synth_Progress", "onRangeStart > utteranceId: " + utteranceId + ", start: " + start
                            + ", end: " + end + ", frame: " + frame);

                    runOnUiThread(()->{
                        Spannable textWithHighlights = new SpannableString(mEditText.getText());

                        textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLACK),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        textWithHighlights.setSpan(new BackgroundColorSpan(Color.YELLOW),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                        mHighlightedTextDisplay.setText(textWithHighlights);

                    });


                }
            });
        } else if (status == TextToSpeech.ERROR){
            Log.e("TTS", "Initialization failed");
            Toast.makeText(this,"Unfortunately, TTS engine initialization failed...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ttsInstalled){
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                // initalize tts engine
                mTts = new TextToSpeech(this,this, "com.google.android.tts");

                // Some audio configs to configure our synthesized speech
                // but it requires level 26 api, and currently this app supports min 21 leve
//                AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
//
//
            }else {
                Toast.makeText(this, "There is NO TTS available! Please install the Google TTS engine"+
                        " from the Play Store.", Toast.LENGTH_LONG).show();

                Intent installTTSEngineIntent = new Intent()
                        .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSEngineIntent);
            }

        }

    }


    private LinkedHashMap<String,String> returnMapForVoice(Voice voice){
        LinkedHashMap<String,String> voiceData = new LinkedHashMap<>();
        voiceData.put("voiceName", voice.getName());
        voiceData.put("languageName", voice.getLocale().getDisplayLanguage());
        voiceData.put("languageCode", voice.getLocale().getISO3Language());
        voiceData.put("languageString", voice.getLocale().toString());
        voiceData.put("countryName", voice.getLocale().getDisplayCountry());
        voiceData.put("countryCode", voice.getLocale().getISO3Country());

        return voiceData;
    }

    private void printOutSupportedLanguages(){
        // Supported languages
        Set<Locale> supportedLanguages = mTts.getAvailableLanguages();
        if (supportedLanguages != null) {
            for (Locale lang : supportedLanguages){
                Log.e("TTS","Supported Language: "+ lang);
            }
        }
    }

    private void printOutSupportedVoices(){
        Set<Voice> availableVoices = mTts.getVoices();
        if (availableVoices != null){
            for (Voice voice : availableVoices){
                if (!voice.isNetworkConnectionRequired() &&  !voice.getFeatures().contains("notInstalled")){
//                if (!voice.isNetworkConnectionRequired() && mTts.getVoice().getLocale().toString().equals(voice.getLocale().toString())){
//                    Log.e("TTS","Available Voice: " + voice);
                    Log.e("TTS", "Current Locale: " + voice);
//                Locale.US.get
//                }else {
//                    Log.e("TTS", "Current Locale: NULLLLLLLLLLLLLLL");

                }
            }
        }
    }

    /*
    * Gets the locale selection from the radio buttons
    * then this selection is used for determining the synthesis language
    * */
    private Locale getUserSelectedLanguage(String language){
        if (language != null){
            switch (language) {
                case "English-US":
                    return Locale.US;
                case "English-UK":
                    return Locale.UK;
                case "French":
                    return Locale.FRENCH;
                case "Italian":
                    return Locale.ITALIAN;
                case "German":
                    return Locale.GERMAN;
                default:
                    Log.e("LANG_SELECTION", "Selected language do not match with any of the predefined languages: NULL");
                    return null;
            }
        }else {
            Log.e("LANG_SELECTION","No initial language selection!! TTS will start by using the default language: "+mTts.getVoice().getLocale());
            return null;
        }

    }



    private String getSelectedVoiceName(String voiceName){
        Locale selectedLanguage = this.getUserSelectedLanguage(currentLanguage);
//        int checkedRadioId = this.mRadioGroupVoices.getCheckedRadioButtonId();
        if (voiceName != null){
            switch (voiceName){
                case "Voice_1":
                    return  Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(0);
                case "Voice_2":
                    return Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(1);
            }

        }
        // if no voice is selected, return inde
        return Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(0);
    }

    private void setTTSVoice(Locale selectedLanguage){
        String voiceName = this.getSelectedVoiceName(currentVoice);
        Voice voice = new Voice(voiceName,selectedLanguage,Voice.QUALITY_VERY_HIGH,
                Voice.LATENCY_NORMAL, false, new HashSet<>());
        Log.e("VOICE_NAME", "Selected Voice name: " + voiceName);
        mTts.setVoice(voice);
    }



    private void setTTSLanguage(){
        Locale selectedLanguage = this.getUserSelectedLanguage(currentLanguage);
//        int isLanguageAvailable = mTts.isLanguageAvailable(selectedLanguage);

        // if the user did not selected any language, set the language and the voice to default
        if(selectedLanguage == null){
            Toast.makeText(this, "Until a language and voice pair is chosen, the default language and voice "
                    + " will be used for the TTS", Toast.LENGTH_SHORT).show();

            mTts.setLanguage(Locale.getDefault());
            mTts.setVoice(new Voice(mTts.getVoice().getName(), Locale.getDefault(), Voice.QUALITY_VERY_HIGH,
                    Voice.LATENCY_NORMAL, false, new HashSet<>()));
            mButtonSpeak.setEnabled(true);

        }else if (mTts.isLanguageAvailable(selectedLanguage) != TextToSpeech.LANG_MISSING_DATA && mTts.isLanguageAvailable(selectedLanguage) != TextToSpeech.LANG_NOT_SUPPORTED){
            String voiceName = this.getSelectedVoiceName(currentVoice);
            Log.e("VOICE_NAME", "Default voice of the selected language: "+ voiceName);

            mTts.setLanguage(selectedLanguage);
            this.setTTSVoice(selectedLanguage);
            mButtonSpeak.setEnabled(true);
        }
    }



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

        //
        Bundle dataMap = new Bundle();
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0f);
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
        dataMap.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_VOICE_CALL);

        // QUEUE_FLUSH option  replaces an already playing synthesis with the new entry
        // if the user calls the speak

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, dataMap, UUID.randomUUID().toString());
    }

    @SuppressLint("ResourceType")
    private void readTextFromFile(){
        ReadOutFromFileFragment readOutFromFileFragment = new ReadOutFromFileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();

        Bundle data = new Bundle();
//        data.putSerializable(TTS_SERIALIZATION_KEY, (Serializable) mTts);
        data.putString(TTS_SERIALIZATION_KEY,"hello love");
        readOutFromFileFragment.setArguments(data);

        fragmentTransaction.replace(R.id.main_activity_layout_container, readOutFromFileFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

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