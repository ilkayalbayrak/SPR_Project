package com.example.text2speechproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

import br.com.onimur.handlepathoz.HandlePathOz;
import br.com.onimur.handlepathoz.HandlePathOzListener;
import br.com.onimur.handlepathoz.model.PathOz;


public class ReadOutFromFileFragment extends Fragment implements HandlePathOzListener.SingleUri{

    private final String TTS_SERIALIZATION_KEY = "tts_serialization_key";
    private static final String PRIMARY = "primary";
    private static final int READ_REQUEST_CODE = 42;
    private static final int PICK_PDF_FILE = 2;
    private static final String COLON = ":";

    private HandlePathOz handlePathOz;
    private TextToSpeech mTts;
    private Button mButtonUploadPDF;
    private String stringParser;


    public ReadOutFromFileFragment(TextToSpeech tts) {
        // Required empty public constructor
        this.mTts = tts;
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

        // initialize the file picker
        handlePathOz = new HandlePathOz(getActivity(),this);

        // TODO: check if permission is already given
        // Request permissions to access the storage
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


        mButtonUploadPDF.setOnClickListener(v -> {
            Intent openDocumentIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openDocumentIntent.setType("*/*");
            startActivityForResult(openDocumentIntent, READ_REQUEST_CODE);
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == READ_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            if(data != null) {
                Uri uri = data.getData();
                // This starts the request handler of the file picker
                // HandlePathOZ
                handlePathOz.getRealPath(uri);

            }
            Log.e("OAR_ERROR", "result data is NULLLLL");
        }
        Log.e("RQ_ERROR", "REQUEST CODES ARE NOT EVEN");
    }

    private void readPdfFile(String path) {

        try {
            PdfReader pdfReader = new PdfReader(path);
            stringParser = PdfTextExtractor.getTextFromPage(pdfReader, 1).trim();
            Log.i("PDF_CONTENTS", "The pdf content: "+ stringParser);
            pdfReader.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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

    @Override
    public void onRequestHandlePathOz(@NotNull PathOz pathOz, @Nullable Throwable throwable) {
        readPdfFile(pathOz.getPath());
        Toast.makeText(getActivity(), "File Uploaded Successfully !", Toast.LENGTH_SHORT).show();
        Log.e("URI", "The URI path: "+pathOz.getPath());
    }

    @Override
    public void onDestroy() {
        handlePathOz.onDestroy();
        super.onDestroy();
    }
}