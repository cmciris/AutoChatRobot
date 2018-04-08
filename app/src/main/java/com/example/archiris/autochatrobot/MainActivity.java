package com.example.archiris.autochatrobot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

import com.google.android.gms.appindexing.Action;
import com.turing.androidsdk.InitListener;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;


import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private TuringApiManager mTuringApiManager;

    // user input his chat content
    public final int CHAT_START = 0;

    // robot reply
    public final int CHAT_RESULT = 1;
    /**
     * 申请的turing的apikey
     **/
    private final String TURING_APIKEY = "ee9e63c5c2f98de47f8cb54b193e2235";
    /**
     * 申请的secret
     **/
    private final String TURING_SECRET = "c20c187e2552afa1";
    /**
     * 填写一个任意的标示，没有具体要求，，但一定要写，
     **/
    private final String UNIQUEID = "131313131";

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE=1;

    private static final int READ_PHONE_STATE_REQUEST_CODE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_STATE_REQUEST_CODE);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                // Permission Denied
                onStop();
            }
        }
        else if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                // turingSDK初始化
                SDKInitBuilder builder = new SDKInitBuilder(this)
                        .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
                SDKInit.init(builder, new InitListener() {
                    @Override
                    public void onFail(String error) {
                        Log.d(TAG, error);
                    }

                    @Override
                    public void onComplete() {
                        // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                        mTuringApiManager = new TuringApiManager(MainActivity.this);
                        mTuringApiManager.setHttpListener(myHttpConnectionListener);
                    }
                });
            }
        }
    }

    private void init() {
        // 申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        else {
            // turingSDK初始化
            SDKInitBuilder builder = new SDKInitBuilder(this)
                    .setSecret(TURING_SECRET).setTuringKey(TURING_APIKEY).setUniqueId(UNIQUEID);
            SDKInit.init(builder, new InitListener() {
                @Override
                public void onFail(String error) {
                    Log.d(TAG, error);
                }

                @Override
                public void onComplete() {
                    // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                    mTuringApiManager = new TuringApiManager(MainActivity.this);
                    mTuringApiManager.setHttpListener(myHttpConnectionListener);
                }
            });
        }

    }

    public void add_msg(String msg) {
        // stylesheet edit here
        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText(msg);
        textView.setGravity(Gravity.RIGHT);

        LinearLayout layout = (LinearLayout) findViewById(R.id.content);
        assert layout != null;
        layout.addView(textView);
    }

    public void add_reply(String msg) {
        // stylesheet edit here
        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText(msg);
        textView.setGravity(Gravity.LEFT);

        LinearLayout layout = (LinearLayout) findViewById(R.id.content);
        assert layout != null;
        layout.addView(textView);
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.send_msg);
        String msg = editText.getText().toString();
        add_msg(msg);
        editText.setText("");
        mTuringApiManager.requestTuringAPI(msg);
    }

    HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {
        @Override
        public void onError(ErrorMessage errorMessage) {
            Log.d(TAG, errorMessage.getMessage());
        }

        @Override
        public void onSuccess(RequestResult result) {
            if (result != null) {
                try {
                    Log.d(TAG, result.getContent().toString());
                    JSONObject result_obj = new JSONObject(result.getContent()
                            .toString());
                    if (result_obj.has("text")) {
                        Log.d(TAG, result_obj.get("text").toString());
                        add_reply(result_obj.get("text").toString());
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException:" + e.getMessage());
                }
            }
        }
    };

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

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.archiris.autochatrobot/http/host/path")
        );

    }
}
