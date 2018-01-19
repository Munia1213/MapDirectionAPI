package com.example.munia.mapdirection;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener {

    private GoogleMap map;
    private GoogleMapOptions options;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private DirectionService service;
    private String origin = "23.750931,90.393467";
    private String destination = "23.823769,90.364252";
    private String[] instructions;
    private Button btnIns;
    private Button btnNextRoute;
    private int totalRoutes = 0;
    private int routeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnIns = findViewById(R.id.btnInstruction);
        btnNextRoute = findViewById(R.id.btnNextRoute);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(DirectionService.class);

        options = new GoogleMapOptions();
        options.zoomControlsEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer,mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);
    }

    public void showInstructions(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(instructions, null)
                .show();
    }

    public void nextRoute(View view) {
        if(routeIndex < totalRoutes){
            getDirectionData();
            routeIndex++;
            if(routeIndex == totalRoutes){
                routeIndex = 0;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnPolylineClickListener(this);
        getDirectionData();

    }

    private void getDirectionData() {

        String apiKey = getString(R.string.google_direction_api);


        String urlString =
                String.format("directions/json?origin=%s&destination=%s&alternatives=true&key=%s",origin,destination,apiKey);

        Call<DirectionResponse>directionResponseCall = service.getDirection(urlString);
        directionResponseCall.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                if(response.code() == 200){

                    btnIns.setEnabled(true);

                    DirectionResponse directionResponse = response.body();

                    totalRoutes = directionResponse.getRoutes().size();
                    if(totalRoutes > 1){
                        btnNextRoute.setEnabled(true);
                    }

                    LatLng focusArea = new LatLng(directionResponse.getRoutes()
                            .get(routeIndex).getLegs().get(0)
                            .getStartLocation().getLat(),
                            directionResponse.getRoutes()
                                    .get(routeIndex).getLegs().get(0)
                                    .getStartLocation().getLng());

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(focusArea,11));

                    List<DirectionResponse.Step> steps = directionResponse.getRoutes()
                            .get(routeIndex).getLegs().get(0).getSteps();

                    instructions = new String[steps.size()];
                    map.clear();
                    for (int i = 0; i < steps.size(); i++) {
                        double startLat = steps.get(i).getStartLocation().getLat();
                        double startlng = steps.get(i).getStartLocation().getLng();
                        double endLat = steps.get(i).getEndLocation().getLat();
                        double endlng = steps.get(i).getEndLocation().getLng();

                        LatLng start = new LatLng(startLat,startlng);
                        LatLng end = new LatLng(endLat, endlng);

                        String instruction = String.valueOf(Html.fromHtml(steps.get(i).getHtmlInstructions()));
                        instructions[i] = instruction;

                        Polyline polyline =
                                map.addPolyline(new PolylineOptions()
                                        .add(start)
                                        .add(end)
                                        .clickable(true));
                        polyline.setTag(instruction);
                    }
                }

            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {

            }
        });
    }


    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this, polyline.getTag().toString(), Toast.LENGTH_LONG).show();

    }
}
