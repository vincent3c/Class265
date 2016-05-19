package com.example.user.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
//import java.util.jar.Manifest;
import android.Manifest;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 0;
    private static final int REQUEST_CODE_CAMERA_ACTIVITY = 1;

    private boolean hasPhoto = false;

    TextView textView;
    EditText editText;
    RadioGroup radioGroup;
    ArrayList<Order> orders;
    String drinkName;
    String note = "";
    CheckBox checkBox;
    ListView listView;
    Spinner spinner;
    ProgressBar progressBar;
    ImageView photoImageView;
    ProgressDialog progressDialog;

    String menuResults = "";

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    Realm realm;
    private CallbackManager callBackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("debug", "Main Activity OnCreate");

//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
////        ParseObject testObject = new ParseObject("HomeworkParse");  //�@�~��
////        testObject.put("sid", "And26518");  //�@�~��
////        testObject.put("email", "vincent3c_11@hotmail.com");    //�@�~��
//        testObject.saveInBackground(new SaveCallback() {
//            // �N���~�T�L�X��
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "save Success", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        textView = (TextView)findViewById(R.id.textView);
        editText = (EditText)findViewById(R.id.editText);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        checkBox = (CheckBox)findViewById(R.id.hideCheckBox);
        listView = (ListView)findViewById(R.id.listView);
        spinner = (Spinner)findViewById(R.id.spinner);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        photoImageView = (ImageView)findViewById(R.id.imageView);

        progressDialog = new ProgressDialog(this);
        orders = new ArrayList<>();

        sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sp.edit();

        // Create a RealmConfiguration which is to locate Realm file in package's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);

        // Get a Realm instance for this thread
//        realm = Realm.getInstance(realmConfig);
        realm = Realm.getDefaultInstance();

        editText.setText(sp.getString("editText", ""));

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = editText.getText().toString();
                editor.putString("editText", text);
                editor.apply();

                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    click(v);
                    return true;
                }
                return false;
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    click(v);
                    return true;
                }
                return false;
            }
        });

//        int checkedId = sp.getInt("radioGroup", R.id.blackTeaRadioButton);
//        radioGroup.check(checkedId);
//
//        RadioButton radioButton = (RadioButton) findViewById(checkedId);
//        drinkName = radioButton.getText().toString();
//
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                editor.putInt("radioGroup", checkedId);
//                editor.apply();
//
//                RadioButton radioButton = (RadioButton) findViewById(checkedId);
//                drinkName = radioButton.getText().toString();
//            }
//        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    photoImageView.setVisibility(View.GONE);
                } else {
                    photoImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = (Order) parent.getAdapter().getItem(position);
                goToDetailOrder(order);
                Snackbar.make(view, order.getNote(), Snackbar.LENGTH_SHORT).show();
            }
        });



        setupListView();
        setupSpinner();

        setupFaceBook();

    }

    void setupFaceBook(){
        callBackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.loginButten);
        loginButton.registerCallback(callBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                 AccessToken accessToken = AccessToken.getCurrentAccessToken();
                GraphRequest request = GraphRequest.newGraphPathRequest(accessToken
                        , "/v2.5/me",
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject object = response.getJSONObject();
                                try {
                                    String name = object.getString("name");
                                    Toast.makeText(MainActivity.this, "Hello " + name, Toast.LENGTH_SHORT).show();
                                    textView.setText("Hello " + name);
                                    Log.d("debug", object.toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    void setupListView()
    {
        progressBar.setVisibility(View.VISIBLE);

        final RealmResults results = realm.allObjects(Order.class);
        OrderAdapter adapter = new OrderAdapter(MainActivity.this, results.subList(0, results.size()));
        listView.setAdapter(adapter);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();

                    progressBar.setVisibility(View.GONE);

                    return;
                }
                List<Order> orders = new ArrayList<Order>();

                Realm realm = Realm.getDefaultInstance();

                for (int i = 0; i < objects.size(); i++) {
                    Order order = new Order();
                    order.setNote(objects.get(i).getString("note"));
                    order.setMenuResults(objects.get(i).getString("menuResults"));
                    order.setStoreInfo(objects.get(i).getString("storeInfo"));
                    if (objects.get(i).getParseFile("photo") != null) {
                        //取得photo的url
                        order.photoURL = objects.get(i).getParseFile("photo").getUrl();
                    }
                    orders.add(order);

                    if (results.size() <= i) {
                        realm.beginTransaction();
                        realm.copyToRealm(order);
                        realm.commitTransaction();
                    }
                }

                realm.close();

                progressBar.setVisibility(View.GONE);

                OrderAdapter adapter = new OrderAdapter(MainActivity.this, orders);
                listView.setAdapter(adapter);
            }
        });
    }

    void  setupSpinner()
    {

        // my homework answer
        progressBar.setVisibility(View.VISIBLE);
        final Spinner storeName = (Spinner) findViewById(R.id.spinner);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("storeInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                ArrayList<String> nameList = new ArrayList<>();
                for (ParseObject object : list) {
                    nameList.add(object.getString("name") + "," + object.getString("address"));
                }

                ArrayAdapter adapter = new ArrayAdapter(
                        MainActivity.this, android.R.layout.simple_list_item_1, nameList);
                storeName.setAdapter(adapter);
            }
        });


//        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("storeInfo");
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> objects, ParseException e) {
//                if (e != null || objects == null) {
//                    return;
//                }
//
//                String[] storeInfo = new String[objects.size()];
//                for (int i = 1; i < objects.size(); i++) {
//                    ParseObject object = objects.get(i);
//                    storeInfo[i] = object.getString("name") + "," + object.getString("address");
//
//                }
//
//                ArrayAdapter adapter = new ArrayAdapter(
//                        MainActivity.this, android.R.layout.simple_list_item_1, storeInfo);
//                spinner.setAdapter(adapter);
//            }
//        });

//        String[] data = getResources().getStringArray(R.array.storeInfo);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data);
//
//        spinner.setAdapter(adapter);
    }

    public void click(View view)
    {
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        note = editText.getText().toString();
        String text = note;
        textView.setText(text);

        Order order = new Order();
        order.setMenuResults(menuResults);
        order.setNote(note);
        order.setStoreInfo((String) spinner.getSelectedItem());

        if (hasPhoto) {
            Uri uri = Utils.getPhotoURI();

            byte[] photo = Utils.uriToBytes(this, uri);

            if (photo == null) {
                Log.d("Debug", "Read Photo Fail");
            } else {
                order.photo = photo;
            }
        }

//        // Persist your data easily
//        realm.beginTransaction();
//        realm.copyToRealm(order);
//        realm.commitTransaction();

        SaveCallbackWithRealm saveCallbackWithRealm = new SaveCallbackWithRealm(order, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // �����~�ɧi��
                if (e != null) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                editText.setText("");
                menuResults = "";

                photoImageView.setImageResource(0);
                hasPhoto = false;

                //讓loading消失
                progressDialog.dismiss();

                setupListView();
            }
        });

        order.saveToRemote(saveCallbackWithRealm);

    }

    public void goToMenu(View view)
    {
        Intent intent = new Intent();

        intent.setClass(this, DrinkMenuActivity.class);

        startActivityForResult(intent, REQUEST_CODE_MENU_ACTIVITY);
    }

    public void goToDetailOrder(Order order) {
        Intent intent = new Intent();
        intent.setClass(this, OrderActivity.class);
        intent.putExtra("note", order.getNote());
        intent.putExtra("storeInfo", order.getStoreInfo());
        intent.putExtra("menuResults", order.getMenuResults());
        intent.putExtra("photoURL", order.photoURL);
        startActivity(intent);  //將資料帶到OrderActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MENU_ACTIVITY)
        {
            if(resultCode == RESULT_OK)
            {
                menuResults = data.getStringExtra("result");
            }
        } else if (requestCode == REQUEST_CODE_CAMERA_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                photoImageView.setImageURI(Utils.getPhotoURI());
                hasPhoto = true;
            }
        }
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_take_photo) {
            Toast.makeText(this, "Take Photo", Toast.LENGTH_LONG).show();
            gotoCamera();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void gotoCamera() {
        //確認是23版後的是否允許可以存取sd卡資料
        if (Build.VERSION.SDK_INT >= 23) {
            // 確認之前是否已有授權過
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                return;
            }
        }

        // 照相
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getPhotoURI());  // 放照片位置
        startActivityForResult(intent, REQUEST_CODE_CAMERA_ACTIVITY);   //將照片放到檔案中
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debug", "Main Activity OnStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug", "Main Activity OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug", "Main Activity OnPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debug", "Main Activity OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        Log.d("debug", "Main Activity OnDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("debug", "Main Activity OnRestart");
    }
}
