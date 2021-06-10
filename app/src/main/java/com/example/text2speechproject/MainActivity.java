package com.example.text2speechproject;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
    private TextToSpeech mTts;
    private EditText mEditText;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private Button mButtonSpeak;
    private Button mButtonRandom;
    private RadioGroup mRadioGroupLanguages;
    private RadioGroup mRadioGroupVoices;
    private AutoCompleteTextView mAutoCompleteLanguages, mAutoCompleteVoices;

    private boolean isAppReady = false;

    private final HashMap<Locale, List<String>> voiceOptionsList = new HashMap<>();
    private final List<String> usVoices = new ArrayList<>();
    private final List<String> ukVoices = new ArrayList<>();
    private final List<String> frenchVoices = new ArrayList<>();
    private final List<String> italianVoices = new ArrayList<>();
    private final List<String> germanVoices = new ArrayList<>();

    private final int ttsInstalled = 0;

    private String currentLanguage = "English-US";
//    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonSpeak = findViewById(R.id.button_speak);
        mButtonRandom = findViewById(R.id.button_random_test);
        mEditText = findViewById(R.id.input_text);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        mRadioGroupLanguages = findViewById(R.id.radioGroup_languages);
        mRadioGroupVoices = findViewById(R.id.radioGroup_voices);
        mAutoCompleteLanguages = findViewById(R.id.autoComplete_languages);
        mButtonRandom.setOnClickListener(v -> {
            for (String info : returnMapForVoice(mTts.getVoice()).values()){
                Log.e("VOICE_INFO", info);
            }
        });

//            String [] languageList = getResources().getStringArray(R.array.array_languages);
            ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this,R.array.array_languages,R.layout.dropdown_item);
//        ArrayAdapter<String> adapterLanguage = new ArrayAdapter<String>(this,R.layout.dropdown_item, languageList);
            mAutoCompleteLanguages.setAdapter(adapterLanguage);
            mAutoCompleteLanguages.setOnItemClickListener((parent, view, position, id) -> {
                currentLanguage = parent.getItemAtPosition(position).toString();
                Log.e("ITEM_SELECTION", "ITEM NAME: "+ currentLanguage);
                setTTSLanguage();

            });

//        mRadioGroupLanguages.setOnCheckedChangeListener((group, checkedId) -> setTTSLanguage());
        mRadioGroupVoices.setOnCheckedChangeListener((group, checkedId) -> setTTSVoice(this.getUserSelectedLanguageV2(currentLanguage)));
        mButtonSpeak.setOnClickListener(v -> speak());



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


                }

                @Override
                public void onDone(String utteranceId) {

                }

                @Override
                public void onError(String utteranceId) {

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


//    private void addItemsToSpinnerLanguage(){
//        List<String> languageList = new ArrayList<>();
//        languageList.add("English-US");
//        languageList.add("English-UK");
//        languageList.add("Italian");
//        languageList.add("German");
//        languageList.add("French");
//
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);
//    }

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
    private Locale getUserSelectedLanguage(){
        int checkedRadioId = this.mRadioGroupLanguages.getCheckedRadioButtonId();
        if (checkedRadioId == R.id.radio_en_us){
            return Locale.US;
        }else if(checkedRadioId == R.id.radio_en_uk){
            return Locale.UK;
        }else if(checkedRadioId == R.id.radio_fr){
            return Locale.FRENCH;
        }else if(checkedRadioId == R.id.radio_it){
            return Locale.ITALIAN;
        }else if(checkedRadioId == R.id.radio_de) {
            return Locale.GERMAN;
        }
        return null;
    }

    private Locale getUserSelectedLanguageV2(String language){
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
    }


    private String getSelectedVoiceName(){
        Locale selectedLanguage = this.getUserSelectedLanguageV2(currentLanguage);
        int checkedRadioId = this.mRadioGroupVoices.getCheckedRadioButtonId();

        if(checkedRadioId == R.id.radio_voice_1){
            return Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(0);
        } else if (checkedRadioId == R.id.radio_voice_2){
            return Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(1);
        }
        // return index 0 by default
        return Objects.requireNonNull(voiceOptionsList.get(selectedLanguage)).get(0);
    }

    private void setTTSVoice(Locale selectedLanguage){
        String voiceName = this.getSelectedVoiceName();
        Voice voice = new Voice(voiceName,selectedLanguage,Voice.QUALITY_VERY_HIGH,
                Voice.LATENCY_NORMAL, false, new HashSet<>());
        Log.e("VOICE_NAME", "Selected Voice name: " + voiceName);
        mTts.setVoice(voice);
    }



    private void setTTSLanguage(){
        Locale selectedLanguage = this.getUserSelectedLanguageV2(currentLanguage);
//        int isLanguageAvailable = mTts.isLanguageAvailable(selectedLanguage);

        // if the user did not selected any language, set the language and the voice to default
        if(selectedLanguage == null){
            Toast.makeText(this, "Failed to set the main language for the TTS engine!"
                    + " Using default locale instead...", Toast.LENGTH_SHORT).show();

            mTts.setLanguage(Locale.getDefault());
            mTts.setVoice(new Voice(mTts.getVoice().getName(), Locale.getDefault(), Voice.QUALITY_VERY_HIGH,
                    Voice.LATENCY_NORMAL, false, new HashSet<>()));
            mButtonSpeak.setEnabled(true);

        }else if (mTts.isLanguageAvailable(selectedLanguage) != TextToSpeech.LANG_MISSING_DATA && mTts.isLanguageAvailable(selectedLanguage) != TextToSpeech.LANG_NOT_SUPPORTED){
            String voiceName = this.getSelectedVoiceName();
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