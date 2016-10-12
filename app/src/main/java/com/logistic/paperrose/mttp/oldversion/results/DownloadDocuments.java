package com.logistic.paperrose.mttp.oldversion.results;

import android.app.DownloadManager;
  import android.app.Notification;
  import android.app.NotificationManager;
  import android.app.PendingIntent;
  import android.content.BroadcastReceiver;
  import android.content.Context;
  import android.content.Intent;
  import android.content.IntentFilter;
  import android.content.pm.PackageManager;
  import android.content.pm.ResolveInfo;
  import android.net.Uri;
  import android.os.AsyncTask;
  import android.os.Build;
  import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
  import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.logistic.paperrose.mttp.oldversion.BaseLogisticActivity;
import com.logistic.paperrose.mttp.R;
  import com.logistic.paperrose.mttp.oldversion.pushes.MyActivity;
  import com.logistic.paperrose.mttp.oldversion.settings.ApplicationParameters;
import com.logistic.paperrose.mttp.oldversion.utils.ColoredArrayAdapter;

import org.apache.http.HttpStatus;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
  import java.io.FileOutputStream;
  import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
  import java.net.URISyntaxException;
  import java.net.URL;
import java.util.ArrayList;
import java.util.List;
  import java.util.Random;

public class DownloadDocuments extends BaseLogisticActivity {
    String num;
    String filename;
    ListView listView1;
    ArrayList<String> items;
    ColoredArrayAdapter<String> adapter;
    String table = "LIST_TRAFFIC";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setLayoutId(R.layout.activity_download_documents);
        super.onCreate(savedInstanceState);
        num = getIntent().getStringExtra("number");
        table = getIntent().getStringExtra("table");
        //setContentView(R.layout.activity_download_documents);
        listView1 = (ListView) findViewById(R.id.documentsList);
        LinearLayout makePhoto = (LinearLayout) findViewById(R.id.makePhoto);
        makePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadDocuments.this, PhotoActivity.class);
                intent.putExtra("number", num);
                intent.putExtra("table", table);
                startActivity(intent);
            }
        });

        refresh();
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String docStr = items.get(i);
                int type = 1;
                if (!ApplicationParameters.trafficDocuments2.isEmpty() && ApplicationParameters.trafficDocuments2.get(num).contains(docStr)) {
                    type = 2;
                }
                String[] dcs = docStr.split("/");
                //docStr = docStr.replace(dcs[dcs.length - 1], URLEncoder.encode(dcs[dcs.length - 1]));
                //docStr = docStr.replace("+", "%20");
                checkExternalStorage();
               // if (isDownloadManagerAvailable(DownloadDocuments.this)) {
                    //String urlStr = "http://" + ApplicationParameters.SERVER_ADDRESS + ApplicationParameters.docString + docStr;
                    try {
                        URI uri;
                        if (type == 1) {
                             uri = new URI(
                                    "http",
                                    ApplicationParameters.SERVER_ADDRESS,
                                    ApplicationParameters.docString + docStr,
                                    null);
                        } else {
                            uri = new URI(
                                    "http",
                                    ApplicationParameters.SERVER_ADDRESS,
                                    docStr,
                                    null);
                        }
                        String request = uri.toASCIIString();

                       /* URL url = new URL(urlStr);
                        URI uri = null;
                        uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                        url = uri.toURL();
                        */
                        showProgress(true);
                        if (mExternalStorageAvailable)
                            downloadFromUrlInt(request, "/" + dcs[dcs.length - 1], type);
                        else
                            downloadFromUrlInt(request, "/" + dcs[dcs.length - 1], type);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                //}
            }
        });

        if (items.size() > 0) {
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    public void refresh() {

        items = new ArrayList<String>();
        if (ApplicationParameters.trafficDocuments.get(num) != null) {
            items.addAll(ApplicationParameters.trafficDocuments.get(num));
        }
        if (ApplicationParameters.trafficDocuments2.get(num) != null) {
            items.addAll(ApplicationParameters.trafficDocuments2.get(num));
        }

       // items = ApplicationParameters.trafficDocuments.get(num) != null ? ApplicationParameters.trafficDocuments.get(num) : new ArrayList<String>();
        adapter = new ColoredArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items);
        listView1.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    protected String getRandomName(String ext) {
        String s = "document";
        Random rand = new Random();
        for (int i = 0; i < 10; i++)
            s += Integer.toString(rand.nextInt(10));
        s += "." + ext;
        return s;
    }

    protected void openFile() {


        try {
            Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (!extension.equals("xps")) {
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                myIntent.setDataAndType(uri, mimetype);
            } else {
                myIntent.setDataAndType(uri, "application/vnd.ms-xpsdocument");
            }
            startActivity(myIntent);
        } catch (Exception e) {
            // TODO: handle exception
            String data = e.getMessage();
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            uri = manager.getUriForDownloadedFile(id);
            openFile();
        }
    };

    public boolean downloadFile(final String path, final String savePath) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    public void checkExternalStorage() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    public void downloadFromUrlExt(String url, String filename) {

        String[] pts = filename.split("\\.");
        this.filename = getRandomName(pts[pts.length - 1].toLowerCase());
        url = url.replace(" ", "%20");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //request.setDescription("Some descrition");\
        request.setTitle("Загрузка файла: " + filename);
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationInExternalFilesDir(DownloadDocuments.this, Environment.DIRECTORY_DOWNLOADS, this.filename);
        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        request.setMimeType("application/" + pts[pts.length - 1]);
        request.allowScanningByMediaScanner();

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        try {
            id = manager.enqueue(request);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    Notification notification;

    public class CheckToken2 extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            String fpath = "";
            String path = args[0];
            String savePath = args[1];
            String type = args[2];

            try {

                path = path.replace("/gtd/", "/ffd6f7e0c257ceee5b753afbb0e4cd3b/");
                URL url = new URL(path);
                File file = new File(savePath);
                if (file.exists()) {
                    file.delete();
                }
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                long startTime = System.currentTimeMillis();
                //final String basicAuth = "Basic " + Base64.encodeToString("GOLUB:transbusiness1212".getBytes(), Base64.NO_WRAP);
                HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
               // ucon.setRequestProperty ("Authorization", basicAuth);
                ucon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
                int status = ucon.getResponseCode();
                InputStream is;
                if (status >= HttpStatus.SC_BAD_REQUEST) {
                    is = ucon.getErrorStream();
                } else {
                    is = ucon.getInputStream();
                }
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int len = ucon.getContentLength();
                int current = 0;
                int total = 0;
                int previousProgress;
                String msg = "";
                while ((current = bis.read()) != -1) {
                    total += current;
                    previousProgress = (int) (total * 100 / len);
                    baf.append((byte) current);

                }
                Log.e("err_file", new String(baf.toByteArray()));
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baf.toByteArray());
                fos.close();
                uri = Uri.fromFile(file);
                openFile();
                fpath = savePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return fpath;

        }

        @Override
        protected void onPostExecute(String res) {
            showProgress(false);
            if (res.isEmpty()) return;
        }

        @Override
        public void onProgressUpdate(String... progress) {


            Notification notif = new Notification(R.drawable.g_soft_logo_canavara, "Загрузка файла", System.currentTimeMillis());
            notif.flags = Notification.FLAG_AUTO_CANCEL;

            Intent notificationIntent = new Intent(getApplicationContext(), MyActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

            notif.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.company), Integer.parseInt(progress[0]) + "% загружено", contentIntent);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(ns);
            mNotificationManager.notify(1, notif);
            super.onProgressUpdate(progress);
        }
    }


    public void downloadFromUrlInt(String url, String filename, int type) {
        String[] pts = filename.split("\\.");
        String fname = getRandomName(pts[pts.length - 1].toLowerCase());
        (new CheckToken2()).execute(url, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Logistic/Documents/" + fname, Integer.toString(type));
    }

    DownloadManager manager;
    long id;

    Uri uri;

    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return (list.size() > 0);
        } catch (Exception e) {
            return false;
        }
    }


}
