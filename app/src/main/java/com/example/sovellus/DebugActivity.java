package com.example.sovellus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DebugActivity extends AppCompatActivity {

    Mega mega = new Mega(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        //Etsitään alanavigaatio elementti
        BottomNavigationView botNav = findViewById(R.id.navigationView);

        //Asetetaan navigaatio vaihtoehdoille toiminnot
        botNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        goMain(botNav);
                        return true;

                    case R.id.navigation_history:
                        goHistory(botNav);
                        return true;

                    case R.id.navigation_profile:
                        goProfile(botNav);
                        return true;
                }
                return false;
            }
        });

    }

    public void goProfile(View v){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);//siirrytään oikealle ->
    }

    public void goHistory(View v){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);//siirrytään vasemmalle <-
    }

    public void goMain(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);//siirrytään vasemmalle <-
    }

    public void createFakeData(View v){
        mega.insertData("2020","01",1,696969,420420,60,true);
        mega.insertData("2020","01",3,696969,420420,60,true);
        mega.insertData("2020","01",5,696969,420420,60,true);
        mega.insertData("2020","01",9,696969,420420,60,true);
        mega.insertData("2020","01",10,696969,420420,60,true);
        mega.insertData("2020","02",15,696969,420420,60,true);
        mega.insertData("2020","02",17,696969,420420,60,true);
        mega.insertData("2020","03",1,696969,420420,60,true);
        mega.insertData("2020","03",2,696969,420420,60,true);
        mega.insertData("2020","03",3,696969,420420,60,true);
        mega.insertData("2020","04",4,696969,420420,60,true);
        mega.insertData("2020","04",6,696969,420420,60,true);
        mega.insertData("2020","04",10,696969,420420,60,true);
    }

    public void clearSave(View v){
        mega.clearData();
    }
}