package com.logistic.paperrose.mttp.oldversion.results;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.R;
import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PhotoActivity extends BaseLogisticActivity implements TextWatcher {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    public String id;
    public String table = "LIST_TRAFFIC";
    AutoCompleteTextView myAutoComplete;
    ArrayList<String> myList = new ArrayList<String>();
    ArrayAdapter<String> myAutoCompleteAdapter;
    TextView autoList;
    public String type_id;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        takePhoto();
        setLayoutId(R.layout.activity_photo);
        super.onCreate(savedInstanceState);
        table = getIntent().getStringExtra("table");
        et3 = (EditText)findViewById(R.id.jpegDate);
        et3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    final Calendar myCalendar = Calendar.getInstance();
                    long unixSeconds;
                    if (et3.getText().toString().isEmpty()) {
                        unixSeconds = System.currentTimeMillis()/1000;
                    } else {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        formatter.setLenient(false);
                        Date oldDate = null;
                        try {
                            oldDate = formatter.parse(et3.getText().toString());
                            unixSeconds = oldDate.getTime()/1000;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Date curDate = new Date();
                            unixSeconds = System.currentTimeMillis()/1000;
                        }


                    }
                    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            // TODO Auto-generated method stub
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            //String myFormat = "dd.MM.yyyy"; //In which you need put here
                            // SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                            // et.setText(sdf.format(myCalendar.getTime()));
                        }

                    };
                    Date date2 = new Date(unixSeconds*1000L);
                    myCalendar.setTime(date2);
                    final DatePickerDialog dial = new DatePickerDialog(PhotoActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    dial.setTitle("Дата документа");
                    dial.setButton(DatePickerDialog.BUTTON_POSITIVE, "Выбрать", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //adapters.get(viewPager.getCurrentItem()).getItem(info.position).setText();
                            int day = dial.getDatePicker().getDayOfMonth();
                            int month = dial.getDatePicker().getMonth()+1;
                            int year = dial.getDatePicker().getYear();
                            String yearS = ""+year;
                            String monthS = ""+month;
                            String dayS = ""+day;
                            if(month < 10){

                                monthS = "0" + monthS;
                            }
                            if(day < 10){

                                dayS  = "0" + dayS;
                            }
                            String time = Long.toString((new Date(year-1900,month,day)).getTime()/1000);
                            et3.setText(dayS + "." + monthS + "." + yearS);
                        }
                    });
                    dial.show();
                }else {

                }
            }
        });
        et3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                long unixSeconds;
                if (et3.getText().toString().isEmpty()) {
                    unixSeconds = System.currentTimeMillis()/1000;
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    formatter.setLenient(false);
                    Date oldDate = null;
                    try {
                        oldDate = formatter.parse(et3.getText().toString());
                        unixSeconds = oldDate.getTime()/1000;
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Date curDate = new Date();
                        unixSeconds = System.currentTimeMillis()/1000;
                    }


                }
                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        //String myFormat = "dd.MM.yyyy"; //In which you need put here
                        // SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        // et.setText(sdf.format(myCalendar.getTime()));
                    }

                };
                Date date2 = new Date(unixSeconds*1000L);
                myCalendar.setTime(date2);
                final DatePickerDialog dial = new DatePickerDialog(PhotoActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dial.setTitle("Дата документа");
                dial.setButton(DatePickerDialog.BUTTON_POSITIVE, "Выбрать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //adapters.get(viewPager.getCurrentItem()).getItem(info.position).setText();
                        int day = dial.getDatePicker().getDayOfMonth();
                        int month = dial.getDatePicker().getMonth()+1;
                        int year = dial.getDatePicker().getYear();
                        String yearS = ""+year;
                        String monthS = ""+month;
                        String dayS = ""+day;
                        if(month < 10){

                            monthS = "0" + monthS;
                        }
                        if(day < 10){

                            dayS  = "0" + dayS;
                        }
                        String time = Long.toString((new Date(year-1900,month,day)).getTime()/1000);
                        et3.setText(dayS + "." + monthS + "." + yearS);
                    }
                });
                dial.show();
            }
        });
        id = getIntent().getStringExtra("number");
        myList.clear();
        myAutoComplete = (AutoCompleteTextView) findViewById(R.id.idDocNomenclature);

        for (int i = 0; i < ApplicationParameters.doc_nomenclatures.size(); i++) {
            myList.add(ApplicationParameters.doc_nomenclatures.get(i).getText());
        }
        myAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                type_id = ApplicationParameters.getTableFieldKeyByName(charSequence.toString(), ApplicationParameters.doc_nomenclatures);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        myAutoComplete.setThreshold(1);
        myAutoCompleteAdapter = new ArrayAdapter<String>(PhotoActivity.this,
                android.R.layout.simple_dropdown_item_1line, myList);

        myAutoComplete.setAdapter(myAutoCompleteAdapter);


    }
    public String jpegText = "";

    public void searchAct() {
        //hideKeyboard();

    }

    public void test(View v) {
        EditText et = (EditText)findViewById(R.id.jpegName);
        if (et.getText().toString().isEmpty()) return;
        jpegText = et.getText().toString() + ".jpg";
        if (jpegText.isEmpty()) jpegText = "photo.jpg";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                byte[] data = null;
                try {
                    String url = "http://"+ ApplicationParameters.MAIN_DOMAIN +"/testUploadDoc";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    MultipartEntity entity = new MultipartEntity();
                    data = "".getBytes();
                    entity.addPart("upload_img", new ByteArrayBody(data,"image/jpeg", jpegText));
                    entity.addPart("name", new StringBody(jpegText,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("id", new StringBody(id,"text/plain", Charset.forName("UTF-8")));
                    httppost.setEntity(entity);
                    HttpResponse resp = httpclient.execute(httppost);
                    HttpEntity resEntity = resp.getEntity();
                    //String string= EntityUtils.toString(resEntity);
                    InputStream is = null;
                    String json;
                    is = resEntity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    return "/mobile/LIST_TRAFFIC/" + id + "/" + jpegText;

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(PhotoActivity.this, msg, Toast.LENGTH_LONG).show();
                photo.recycle();
                ApplicationParameters.trafficDocuments.get(id).add(msg);
                finish();
            }
        }.execute(null, null, null);


    }

    String jpegNumber = "";
    String jpegDate = "";
    EditText et3;
    public void sendPhoto(View v){
        showProgress(true);
        EditText et = (EditText)findViewById(R.id.jpegName);
        if (et.getText().toString().isEmpty()) return;
        EditText et2 = (EditText)findViewById(R.id.jpegNumber);
        //= (EditText)findViewById(R.id.jpegNumber);
        jpegText = et.getText().toString() + ".jpg";
        jpegNumber = et2.getText().toString();
        jpegDate = et3.getText().toString();
        if (jpegText.isEmpty()) jpegText = "photo.jpg";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                byte[] data = null;
                try {
                    String url = "http://"+ ApplicationParameters.MAIN_DOMAIN +"/uploadDoc";
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    MultipartEntity entity = new MultipartEntity();

                    if(photo!=null){
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                        data = bos.toByteArray();
                        entity.addPart("upload_img", new ByteArrayBody(data,"image/jpeg", jpegText));
                    }
                    // entity.addPart("category", new StringBody(categoryname,"text/plain",Charset.forName("UTF-8")));
                    entity.addPart("name", new StringBody(jpegText,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("number", new StringBody(jpegNumber,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("date", new StringBody(jpegDate,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("id", new StringBody(id,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("type_id", new StringBody(type_id,"text/plain", Charset.forName("UTF-8")));
                    entity.addPart("list_table_name", new StringBody(table,"text/plain", Charset.forName("UTF-8")));
                    httppost.setEntity(entity);
                    HttpResponse resp = httpclient.execute(httppost);
                    HttpEntity resEntity = resp.getEntity();
                    //String string= EntityUtils.toString(resEntity);
                    InputStream is = null;
                    String json;
                    is = resEntity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    return "/mobile/LIST_TRAFFIC/" + id + "/" + jpegText;

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(PhotoActivity.this, msg, Toast.LENGTH_LONG).show();
                photo.recycle();
                if (ApplicationParameters.trafficDocuments.get(id) == null) {
                    ApplicationParameters.trafficDocuments.put(id, new ArrayList<String>());
                }
                ApplicationParameters.trafficDocuments.get(id).add(msg);
                finish();
            }
        }.execute(null, null, null);

    }
    private Uri mImageUri;
    public void takePhoto(){
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            File photo = null;
            try
            {
                photo = this.createTemporaryFile("picture", ".jpg");
                photo.delete();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if (photo!=null)
                mImageUri = Uri.fromFile(photo);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }

    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public Bitmap decodeSampledBitmapFromUri() throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 4;

        AssetFileDescriptor fileDescriptor =null;
        fileDescriptor =
                getContentResolver().openAssetFileDescriptor( mImageUri, "r");

        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
    }

    public void grabImage()
    {
        this.getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        try
        {
            if (photo != null) photo.recycle();
            photo = decodeSampledBitmapFromUri();// android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    Bitmap photo;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
           // Bitmap photo1 = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(photo1);
            grabImage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
