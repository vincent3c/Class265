package com.example.user.simpleui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    TextView note;
    TextView storeInfo;
    TextView menuResults;
    ImageView photo;
    ImageView mapImageView;

    String storeName;
    String address;

    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_activity);

        note = (TextView)findViewById(R.id.note);
        storeInfo = (TextView)findViewById(R.id.storeInfo);
        menuResults = (TextView)findViewById(R.id.menuResults);
//        photo = (ImageView)findViewById(R.id.photoImageView);
        mapImageView = (ImageView)findViewById(R.id.mapImageView);

        Intent intent = getIntent();
        note.setText(intent.getStringExtra("note"));
        storeInfo.setText(intent.getStringExtra("storeInfo"));

        String[] info = intent.getStringExtra("storeInfo").split(",");
        storeName = info[0];
        address = info[1];

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
//            (new GeoCodingTask(photo)).execute("台北市羅斯福路四段一號");
        }


        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.googleMapFragment);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                (new GeoCodingTask(googleMap)).execute(address);
            }
        });

//        for (int i = 1; i < 10; i++) {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        SystemClock.sleep(1000);
//                    }
//                }
//            });
//        }
    }

    private static class GeoCodingTask extends AsyncTask<String, Void , double[]> {
        GoogleMap googleMap;
        private ArrayList<Polyline> polylines;

        @Override
        protected double[] doInBackground(String... params) {
            String address = params[0] ;
            double[] latlng = Utils.addressToLatLng(address);
            return latlng;
        }

        @Override
        protected void onPostExecute(double[] latlng) {
            LatLng storeLocation = new LatLng(latlng[0], latlng[1]);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 17));
            googleMap.addMarker(new MarkerOptions().position(storeLocation));

            LatLng start = new LatLng(25.0186348, 121.5398379);
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .waypoints(start, storeLocation)
                    .withListener(new RoutingListener() {
                        @Override
                        public void onRoutingFailure(RouteException e) {

                        }

                        @Override
                        public void onRoutingStart() {

                        }

                        @Override
                        public void onRoutingSuccess(ArrayList<Route> routes, int index) {
                            if(polylines != null) {
                                for (Polyline poly : polylines) {
                                    poly.remove();
                                }
                            }

                            polylines = new ArrayList<>();
                            //add route(s) to the map.
                            for (int i = 0; i <routes.size(); i++) {

                                //In case of more than 5 alternative routes

                                PolylineOptions polyOptions = new PolylineOptions();
                                polyOptions.color(Color.BLUE);  // go way collor
                                polyOptions.width(10 + i * 3);  // go way width
                                polyOptions.addAll(routes.get(i).getPoints());
                                Polyline polyline = googleMap.addPolyline(polyOptions);
                                polylines.add(polyline);

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ routes.get(i).getDistanceValue()+": duration - "+ routes.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onRoutingCancelled() {

                        }
                    }).build();
            routing.execute();
        }

        public GeoCodingTask(GoogleMap googleMap){
            this.googleMap =googleMap;
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
