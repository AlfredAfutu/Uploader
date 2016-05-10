package com.ticket.gemroc.uploader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener{

    private String TAG = getClass().getSimpleName();
    private TextView fileNameTextView;
    private EditText editText;
    private Button saveFileButton, sendFileButton;
    private String filename;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        saveFileButton.setOnClickListener(this);
        sendFileButton.setOnClickListener(this);
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

    private void initializeViews(){
        fileNameTextView = (TextView) findViewById(R.id.text_filename);
        editText = (EditText) findViewById(R.id.text);
        saveFileButton = (Button) findViewById(R.id.save_text);
        sendFileButton = (Button) findViewById(R.id.send_file);
    }

    @Override
    public void onClick(View v) {
       switch(v.getId()){
           case R.id.save_text:
               String text = editText.getText().toString();
               Date date = new Date();
               String currentDateString = DateFormat.getDateTimeInstance().format(date);
               Log.i(TAG, "Date for text filename >> " + currentDateString);
               filename = currentDateString+".txt";
               File textFile = new File(Environment.getExternalStorageDirectory(), filename);
               //if(!textFile.exists())
                   try {
                       textFile.createNewFile();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }

               Log.i(TAG, "Text File >> "+ textFile);
               sendBroadcast((new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)).setData(Uri.fromFile(textFile)));

               OutputStreamWriter outputStream = null;
               try{
                   outputStream = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator+filename, true));
                   //CharSequence charSequence = (currentDateString+"\n");
                   outputStream.append(text);


               }catch(FileNotFoundException exception){
                   Log.i(TAG, "File Not Found Exception >> " + exception);
               } catch (IOException e) {
                   e.printStackTrace();
               } finally{
                   if(outputStream!=null){
                       try {
                           outputStream.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
                   fileNameTextView.setVisibility(View.VISIBLE);
                   fileNameTextView.setText(currentDateString + ".txt");
                   sendFileButton.setVisibility(View.VISIBLE);
               }

               break;
           case R.id.send_file:
               progressDialog = new ProgressDialog(this);
               progressDialog.setMessage("Uploading file to server...");
               progressDialog.show();
               File fileToBeUploaded = new File(Environment.getExternalStorageDirectory(), filename);

               try {
                   Log.i(TAG, "File to be uploaded >> "+ fileToBeUploaded);
                   Ion.with(getApplicationContext())
                           .load("http://10.0.0.1:8000/")
                           .progressDialog(progressDialog)
                           .setLogging("File Upload", Log.VERBOSE)
                           .setMultipartParameter("filename", filename)
                           .setMultipartFile("file", fileToBeUploaded)
                           .asString()
                           .withResponse()
                           .setCallback(new FutureCallback<Response<String>>() {
                               @Override
                               public void onCompleted(Exception e, Response<String> result) {
                                   Log.i(TAG, "Result >> " + result.getHeaders().code());
                                   int responseCode = result.getHeaders().code();
                                   if (responseCode == 200) {
                                       progressDialog.dismiss();
                                       Toast.makeText(getApplicationContext(), "File Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                                   }

                               }
                           });

               }catch(Exception e){
                   e.printStackTrace();
               }


               break;
       }
    }
}
