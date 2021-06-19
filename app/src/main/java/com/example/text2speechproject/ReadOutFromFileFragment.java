package com.example.text2speechproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import br.com.onimur.handlepathoz.HandlePathOz;
import br.com.onimur.handlepathoz.HandlePathOzListener;
import br.com.onimur.handlepathoz.model.PathOz;


/*
* A Fragment (sub-activity) that handles searching for and opening  a PDF file
* Also extracts the text and prepares it to be synthesized to speech
*
* The class also implements the  HandlePathOzListener which waits for file picking
* requests and directs them to its handler
* */
public class ReadOutFromFileFragment extends Fragment implements HandlePathOzListener.SingleUri{

    private static final int READ_REQUEST_CODE = 42;
    private static final String FRAGMENT_GENERAL_UTTERANCE_ID = "fragment_general_utterance_id";


    /*
     * This library is used for real path of files to be able to use them in the application
     * Because when we retrieve a Uri through an intent, the path we get is not the exact real path
     * of the file
     */
    private HandlePathOz handlePathOz;

    private TextToSpeech mTts;
    private Button mButtonUploadPDF;
    private Button mButtonReadText;
    private String fileTextContent;
    private TextView mHighlightedTextView;
    private TextView mFragmentWallpaperTextView;



    public ReadOutFromFileFragment(TextToSpeech tts) {
        // Required empty public constructor
        this.mTts = tts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_read_out_from_file, container, false);

        // Bind elements
        mButtonUploadPDF = view.findViewById(R.id.button_upload_pdf);
        mButtonReadText = view.findViewById(R.id.button_read_text);
        mHighlightedTextView = view.findViewById(R.id.textView_fragment_display);
        mFragmentWallpaperTextView = view.findViewById(R.id.textView_fragment_wallpaper);

        // initialize the file picker
        handlePathOz = new HandlePathOz(requireActivity(),this);

        mButtonUploadPDF.setOnClickListener(v -> {
            Intent openDocumentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openDocumentIntent.setType("*/*");
            startActivityForResult(openDocumentIntent, READ_REQUEST_CODE);
        });

        mButtonReadText.setOnClickListener(v->{
            utteranceListenerStarter();
            speakExtractedText(mTts, fileTextContent);
        });
//        mTts.


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            if(data != null) {
                Uri uri = data.getData();

                // This starts the request handler of the file picker HandlePathOZ
                handlePathOz.getRealPath(uri);

            }
//            Log.e("OAR_ERROR", "result data is NULLLLL");
        }
//        Log.e("RQ_ERROR", "REQUEST CODES ARE NOT EVEN");
    }

    /*
    * Creates a PdfReader object that gets the full file path from handlePathOz,
    * then extracts the text content of the file
    * TODO: set a threshold for the file size, or maybe make it so it could be chosen by the user
    * */
    private void readPdfFile(String path) {

        try {
            PdfReader pdfReader = new PdfReader(path);
            fileTextContent = PdfTextExtractor.getTextFromPage(pdfReader, 1).trim();
            Log.i("PDF_CONTENTS", "The pdf content: "+ fileTextContent);
            pdfReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Sets parameters for the synthesis, and calls the method speak of TTS object
    * */
    private void speakExtractedText(TextToSpeech tts, String fileTextContent){
        Bundle dataMap = new Bundle();
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0f);
        dataMap.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
        dataMap.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_VOICE_CALL);


        tts.speak(fileTextContent, TextToSpeech.QUEUE_FLUSH, dataMap, FRAGMENT_GENERAL_UTTERANCE_ID);
    }

    private void utteranceListenerStarter(){

        // Start the utterance listener in fragment
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(String utteranceId) {
                if (utteranceId.equals(FRAGMENT_GENERAL_UTTERANCE_ID)){
                    requireActivity().runOnUiThread(()->{
                        mHighlightedTextView.setVisibility(View.VISIBLE);
                        mFragmentWallpaperTextView.setVisibility(View.INVISIBLE);
                    });

                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals(FRAGMENT_GENERAL_UTTERANCE_ID)){
                    requireActivity().runOnUiThread(()->{
                        mHighlightedTextView.setVisibility(View.INVISIBLE);
                        mHighlightedTextView.setText("");
                        mFragmentWallpaperTextView.setVisibility(View.VISIBLE);

                    });
                }


            }

            @Override
            public void onError(String utteranceId) {

                if (utteranceId.equals(FRAGMENT_GENERAL_UTTERANCE_ID)){
                    requireActivity().runOnUiThread(()->{
                        Log.e("UTTERANCE_ERROR", "An Error occurred while synthesizing the given text...");
                        Activity activity = requireActivity();
                        Toast toast = Toast.makeText(activity,
                                "An Error occurred while synthesizing the given text...",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        mHighlightedTextView.setVisibility(View.INVISIBLE);
                        mHighlightedTextView.setText("");
                        mFragmentWallpaperTextView.setVisibility(View.VISIBLE);

                    });
                }

            }

            @RequiresApi(Build.VERSION_CODES.O)
            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                super.onRangeStart(utteranceId, start, end, frame);
                if (utteranceId.equals(FRAGMENT_GENERAL_UTTERANCE_ID)){
                    Log.i("Current_Synth_Progress", "onRangeStart > utteranceId: " + utteranceId + ", start: " + start
                            + ", end: " + end + ", frame: " + frame);

                    requireActivity().runOnUiThread(()->{
                        Spannable textWithHighlights = new SpannableString(fileTextContent);

                        textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLACK),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        textWithHighlights.setSpan(new BackgroundColorSpan(Color.YELLOW),
                                start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                        mHighlightedTextView.setText(textWithHighlights);
                    });
                }
            }
        });
    }


    // Request code for selecting a PDF document.

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void openFile(Uri pickerInitialUri) {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/pdf");
//
//        // Optionally, specify a URI for the file that should appear in the
//        // system file picker when it loads.
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
//
//        requireActivity().startActivityForResult(intent, PICK_PDF_FILE);
//    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * After receiving a request handles the further needed actions
    * such as passing the full path of the file to the readPdfFile method
    * */
    @Override
    public void onRequestHandlePathOz(@NotNull PathOz pathOz, @Nullable Throwable throwable) {
        readPdfFile(pathOz.getPath());
        mButtonReadText.setVisibility(View.VISIBLE);
        mHighlightedTextView.setVisibility(View.VISIBLE);
        mFragmentWallpaperTextView.setVisibility(View.INVISIBLE);
        mHighlightedTextView.setText(fileTextContent);
        Toast.makeText(getActivity(), "File Uploaded Successfully !", Toast.LENGTH_SHORT).show();
        Log.e("URI", "The URI path: "+pathOz.getPath());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mTts != null
                && mTts.isSpeaking()) {
            mTts.stop();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mTts != null
                && mTts.isSpeaking()) {
            mTts.stop();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        handlePathOz.onDestroy();
        if (mTts != null
                && mTts.isSpeaking()) {
            mTts.shutdown();
        }
        super.onDestroy();
    }


}