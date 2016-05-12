package com.example.user.simpleui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderActivity extends AppCompatActivity {

    TextView note;
    TextView storeInfo;
    TextView menuResults;
    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_activity);

        note = (TextView)findViewById(R.id.note);
        storeInfo = (TextView)findViewById(R.id.storeInfo);
        menuResults = (TextView)findViewById(R.id.menuResults);
        photo = (ImageView)findViewById(R.id.photoImageView);

        Intent intent = getIntent();
        note.setText(intent.getStringExtra("note"));
        storeInfo.setText(intent.getStringExtra("storeInfo"));

        String results = intent.getStringExtra("menuResults");
        String text = "";
        try {
            JSONArray jsonArray = new JSONArray(results);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                text += object.getString("name") + "：大杯" + object.getString("l") + "杯    中杯" + object.getString("m") + "杯" + "\n";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        menuResults.setText(text);
        String url = intent.getStringExtra("photoURL");
        if (!url.equals("")) {
//            Picasso.with(this).load(intent.getStringExtra("photoURL")).into(photo);

//            Picasso.with(this).load(url).into(photo);

//            for (int i = 0; i < 10; i++) {
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            wait(10000);
//                        } catch (InterruptedException e) {
//                           e.printStackTrace();
//                        }
//                    }
//                });
//                thread.start();
//            }
            (new ImageLoadingTask(photo)).execute(url);
        }
    }

    private static class ImageLoadingTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView; //取得的圖檔放置位子

        public ImageLoadingTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            byte[] bytes = Utils.urlToBytes(url);
            if (bytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {   //取得doInBackground所return的bitmap
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}