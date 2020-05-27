package com.example.td3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity  implements ListAdapter.OnFFListener {

    static final String BASE_URL = "https://raw.githubusercontent.com/kalash94/Mobile_Programming_project/API_Rest/app/src/main/java/com/example/td3/";
    private static final String TAG = "Clicked";

    private RecyclerView recyclerView;
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    public ArrayList<FinalFantasy> FFArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("application_esiea", Context.MODE_PRIVATE);

        gson = new GsonBuilder()
                .setLenient()
                .create();

        FFArrayList = getArrayDataFromCache();
        if (FFArrayList == null)
        {
            makeApiCall();
        }

        List<FinalFantasy> FinalFantasyList = getDataFromCache();
        if (FinalFantasyList != null)
        {
            showList(FinalFantasyList);
        }else {
            makeApiCall();
        }
    }

    private List<FinalFantasy> getDataFromCache(){
        String jsonFinalFantasy = sharedPreferences.getString(Constants.KEY_FINAL_FANTASY_LIST, null);

        if(jsonFinalFantasy == null){
            return null;
        }else {

            Type listType = new TypeToken<List<FinalFantasy>>() {
            }.getType();
            return gson.fromJson(jsonFinalFantasy, listType);
        }
    }

    private ArrayList<FinalFantasy> getArrayDataFromCache(){
        String jsonFinalFantasy = sharedPreferences.getString(Constants.KEY_FINAL_FANTASY_LIST, null);

        if(jsonFinalFantasy == null){
            return null;
        }else {

            Type listType = new TypeToken<ArrayList<FinalFantasy>>() {
            }.getType();
            return gson.fromJson(jsonFinalFantasy, listType);
        }
    }

    private void showList(List<FinalFantasy> FinalFantasyList) {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ListAdapter(FinalFantasyList, this);
        recyclerView.setAdapter(mAdapter);
    }

    private void makeApiCall(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        final FinalFantasyApi FinalFantasyApi = retrofit.create(FinalFantasyApi.class);

        Call<RestFinalFantasyResponse> call = FinalFantasyApi.getFinalFantasyResponse();
        call.enqueue(new Callback<RestFinalFantasyResponse>() {
            @Override
            public void onResponse(Call<RestFinalFantasyResponse> call, Response<RestFinalFantasyResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    List<FinalFantasy> FinalFantasyList = response.body().getResults();
                    saveList(FinalFantasyList);
                    showList(FinalFantasyList);
                }else{
                    showError();
                }
            }

            @Override
            public void onFailure(Call<RestFinalFantasyResponse> call, Throwable t) {
                showError();
            }
        });
    }

    private void saveList(List<FinalFantasy> finalFantasyList) {
        String jsonString = gson.toJson(finalFantasyList);
        sharedPreferences
                .edit()
                .putString(Constants.KEY_FINAL_FANTASY_LIST, jsonString)
                .apply();
        Toast.makeText(getApplicationContext(), "List saved", Toast.LENGTH_SHORT).show();

    }

    private void showError() {
        Toast.makeText(getApplicationContext(), "API Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnFFClick(int position) {

        Log.d(TAG, "OnFFClick: clicked" + position);
        //Toast.makeText(getApplicationContext(), "Clicked on " + position + " position", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, NewActivity.class);
        intent.putExtra("selected_FF", FFArrayList.get(position));

        startActivity(intent);
    }
}
