package com.example.text2speechproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;


public class ReadOutFromFileFragment extends Fragment {

    private final String TTS_SERIALIZATION_KEY = "tts_serialization_key";
    private static final String PRIMARY = "primary";
    private static final int READ_REQUEST_CODE = 42;
    private static final int PICK_PDF_FILE = 2;
    private static final String COLON = ":";

    private TextToSpeech mTts;
    private Button mButtonUploadPDF;


    public ReadOutFromFileFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            // Get initialized TTS object from the main activity
            String someData =  getArguments().getString(TTS_SERIALIZATION_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_read_out_from_file, container, false);

        // Bind elements
        mButtonUploadPDF = view.findViewById(R.id.button_upload_pdf);

        // Request permissions to access the storage
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        mButtonUploadPDF.setOnClickListener(v -> {
            Intent openDocumentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openDocumentIntent.setType("*/*");
            getActivity().startActivityForResult(openDocumentIntent, READ_REQUEST_CODE);
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == READ_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if(data != null) {
                Uri uri = data.getData();
                Toast.makeText(getActivity(), "File Uploaded Successfully !", Toast.LENGTH_SHORT).show();
                Log.v("URI", uri.getPath());
//                readPdfFile(uri);
            }
        }
    }

//    private void readPdfFile(Uri uri) {
//        String fullPath;
//        //convert from uri to full path
//        if(uri.getPath().contains(PRIMARY)) {
//
//            fullPath = uri.getPath().split(COLON)[1];//varies from device to device , also LOCAL_STORAGE + uri.getPath().split(COLON)[1];
//        }
//        else {
//            fullPath = uri.getPath().split(COLON)[1];//varies from device to device ,also EXT_STORAGE + uri.getPath().split(COLON)[1];
//
//        }
//        Log.v("URI", uri.getPath()+" "+fullPath);
//
//        try {
//            PdfReader pdfReader = new PdfReader(fullPath);
//            stringParser = PdfTextExtractor.getTextFromPage(pdfReader, 1).trim();
//            pdfReader.close();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    // Request code for selecting a PDF document.

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        getActivity().startActivityForResult(intent, PICK_PDF_FILE);
    }

}