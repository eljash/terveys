package com.example.sovellus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;

import Classes.Counter;
import Classes.Mega;
import Classes.SuperMetodit;
import Classes.User;
import Classes.UserProfileEditor;
import Classes.dataOlio;
import pl.pawelkleczkowski.customgauge.CustomGauge;
/**
 * @author Jukka Hallikainen, Arttu Pösö ja Eljas Hirvelä
 */
public class MainActivity extends AppCompatActivity {

    private Mega mega;
    private SuperMetodit SM;

    private static final String LOGTAG = "MainActivity.java";
    private boolean eRunning = false;
    private boolean sRunning = false;
    private Counter eCounter;
    private Counter sCounter;
    private User user;
    private UserProfileEditor profile;

    private SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");

    private SwitchCompat sportSwitch;
    private SwitchCompat screenSwitch;

    private Button weightButton;
    private EditText input;

    private dataOlio todayObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weightButton = findViewById(R.id.Pp_button);

        //Etsitään alanavigaatio elementti
        BottomNavigationView botNav = findViewById(R.id.navigationView);

        //Kerrotaan mikä valittavista on auki: MainActivity = navigation_home
        botNav.setSelectedItemId(R.id.navigation_home);

        botNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
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

        createChangeButtons();

        this.mega = new Mega(this);
        this.SM = new SuperMetodit();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(LOGTAG,"onStart()");
        checkForProfile();
        setGaugeEndValue();
        setLatestWeight();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(LOGTAG,"onResume()");

        setGaugeEndValue();
        mega.todayData();
        todayObject = mega.todayObject();

        sportSwitch = findViewById(R.id.Sport_switch);
        screenSwitch = findViewById(R.id.Screen_switch);

        checkLastExit();

        correctCounterStates();
    }

    /**
     * Metodilla asetetaan aktiviteetin mittareihin maksimi arvot, jotka vastaavat käyttäjän asettamia tavotteita ruutu- ja urheilu-ajalle
     */
    public void setGaugeEndValue() {
        CustomGauge sportGauge = findViewById(R.id.sportTV);
        CustomGauge screenGauge = findViewById(R.id.screenTV);
        sportGauge.setEndValue(user.sportTimeGoal());
        screenGauge.setEndValue(user.screenTimeGoal());
        sportGauge.setPointStartColor(Color.rgb(51,204,255));
        sportGauge.setPointEndColor(Color.rgb(51,204,255));
        screenGauge.setPointStartColor(Color.rgb(255,112,77));
        screenGauge.setPointEndColor(Color.rgb(255,51,0));
    }

    public void setLatestWeight() {
        TextView LW = findViewById(R.id.latestWeightText);
        LW.setText((user.startWeight()) + " kg");
    }

    public void sportTimeClicked(View v) {
        CustomGauge sportGauge = findViewById(R.id.sportTV);
        if (!eRunning) {
            eCounter.run();
            eRunning = true;
            if(sRunning){
                sCounter.stop();
                sRunning = false;
                screenSwitch.setChecked(false);
            }
        } else {
            eCounter.stop();
            eRunning = false;
        }
    }

    /**
     * Metodilla siirrytään debug aktiviteettiin.
     */
    public void goDebug(View v){
        Intent intent = new Intent(this, DebugActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);//siirrytään oikealle ->
    }

    /**
     * Metodilla siirrytään profiili aktiviteettiin.
     */
    public void goProfile(View v){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("urheilu", eCounter.getCurrent());
        intent.putExtra("ruutu",sCounter.getCurrent());
        intent.putExtra("paino",todayObject.returnWeight());
        startActivity(intent);

        //Lisätään animaatio aktiviteetin vaihtoon
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);//siirrytään oikealle ->
    }

    /**
     * Metodilla siirrytään historia aktiviteettiin.
     */
    public void goHistory(View v){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);//siirrytään oikealle ->
    }

    public void screenTimeClicked(View v) {
        CustomGauge screenGauge = findViewById(R.id.screenTV);
        if (!sRunning) {
            sCounter.run();
            sRunning = true;
            if(eRunning){
                eCounter.stop();
                eRunning = false;
                sportSwitch.setChecked(false);
            }
        } else {
            sCounter.stop();
            sRunning = false;
        }
    }

    public void insertWeight(View v){

    }

    /**
     * Metodi varmistaa, että switchin boolean on sama kuin ajastimen pyörimisen boolean. Eli jos switch on päällä, kuuluisi
     * mittarin/laskurin olla päällä.
     */
    private void correctCounterStates(){
        if(sportSwitch.isChecked()!=eCounter.isRunning() || screenSwitch.isChecked() != sCounter.isRunning()){

            // KATSOO URHEILU SWITCHIN JA AJASTIMEN
            if(sportSwitch.isChecked() && !eCounter.isRunning()){
                eCounter.run();
            } else if(!sportSwitch.isChecked() && eCounter.isRunning()){
                eCounter.stop();
            }

            // KATSOO RUUTU SWITCHIN JA AJASTIMEN
            if(screenSwitch.isChecked() && !sCounter.isRunning()){
                sCounter.run();
            } else if(!screenSwitch.isChecked() && sCounter.isRunning()){
                sCounter.stop();
            }
        }
    }

    /** METODI KATSOO ONKO SOVELLUKSESSA LUOTU JO PROFIILIA */
    private void checkForProfile(){
        profile = new UserProfileEditor(this);
        user = profile.returnProfile();
        if (user.name().equals("No name")) {
            Log.d("Message", "No user found, switching to NewUserActivity");
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
        }
    }

    /** KATSOO MILLAINEN TILA TALLENNETTIIN VIIMEKSI AKTIVITEETISTA POISTUESSA */
    /**
     * Metodi katsoo millainen tila tallennettiin viimeksi aktiviteetista poistuessa.
     * Eli jos jompi kumpi switcheistä oli päällä viimeksi poistuessa, laitetaan se päälle sekä lasketaan
     * kulunut aika viime avaamisesta, jotta saadaan se lisättyä mittariin sekä päivän dataan.
     */
    public void checkLastExit(){
        Log.d(LOGTAG,"checkLastExit()");
        Date timeNow = new Date();

        // HAKEE SHAREDPREFERENCES:ISTÄ TIEDOSTON JOHON TILA TALLENNETTIIN
        SharedPreferences shpref = this.getSharedPreferences("AppState", Activity.MODE_PRIVATE);

        String dateExit = shpref.getString("exitDate","0");
        String dateNow = DateFor.format(timeNow);
        Log.d(LOGTAG,"quit date was: "+dateExit+" date now: "+dateNow);

        eRunning = shpref.getBoolean("sportON",false);
        sRunning = shpref.getBoolean("screenON",false);

        // ASETTAA VARMUUDEN VUOKSI AKTIVITEETIN SWITCH:IT OFF TILAAN
        sportSwitch.setChecked(false);
        screenSwitch.setChecked(false);

        // TÄMÄ BOOLEAN TOTEUTUU JOS URHEILU SWITCH OLI PÄÄLLÄ AKTIVITEETISTÄ POISTUESSA
        if(eRunning){
            sportSwitch.setChecked(true);
            Date exitTime = new Date(shpref.getLong("sportOnDate",0));
            long seconds = (timeNow.getTime()-exitTime.getTime())/1000;

            // JOS POISTUMIS PÄIVÄ OLI SAMA KUIN UUDELLEEN AVAUS PÄIVÄ TAI SIITÄ ON ALLE 8 TUNTIA (28800SEC)
            // LAUSEKE LISÄÄ NYKYISELLE PÄIVÄLLE KULUNEEN AJAN, ELI JOS VUOROKAUSI ON KERENNYT VAIHTUA
            // MUTTA AIKAERO ON ALLE 8 TUNTIA ASETETAAN KULUNUT AIKA NYKYISELLE PÄIVÄLLE
            if(dateExit.equals(dateNow)|| seconds < 28800){
                todayObject.insertSport(todayObject.sportSec()+(int)seconds);
            } else {
                eRunning = false;
                sRunning = false;
            }

        }else if(sRunning){ // TÄMÄ BOOLEAN TOTEUTUU JOS RUUTU SWITCH OLI PÄÄLLÄ POISTUESSA
            screenSwitch.setChecked(true);
            Date exitTime = new Date(shpref.getLong("screenOnDate",0));
            long seconds = (timeNow.getTime()-exitTime.getTime())/1000;

            // JOS POISTUMIS PÄIVÄ OLI SAMA KUIN UUDELLEEN AVAUS PÄIVÄ TAI SIITÄ ON ALLE 8 TUNTIA (28800SEC)
            // LAUSEKE LISÄÄ NYKYISELLE PÄIVÄLLE KULUNEEN AJAN, ELI JOS VUOROKAUSI ON KERENNYT VAIHTUA
            // MUTTA AIKAERO ON ALLE 8 TUNTIA ASETETAAN KULUNUT AIKA NYKYISELLE PÄIVÄLLE
            if(dateExit.equals(dateNow)|| seconds < 28800){
                todayObject.insertScreen(todayObject.screenSec()+(int)seconds);
            } else {
                eRunning = false;
                sRunning = false;
            }

        }

        // LUO UUDET AJASTIMET
        eCounter = new Counter(todayObject.sportSec(),findViewById(R.id.sportTV),user.sportTimeGoal());
        sCounter = new Counter(todayObject.screenSec(),findViewById(R.id.screenTV),user.screenTimeGoal());

        // LAITTAA AJASTIMEN PÄÄLLE SEN PERUSTEELLA OLIKO JOMPI KUMPI PÄÄLLÄ VIIMEKSI
        if(eRunning)eCounter.run();
        else if(sRunning)sCounter.run();

        // NOLLATAAN TALLENNETTU "POISTUMIS" TILA

        SharedPreferences.Editor predit = shpref.edit();
        predit.putBoolean("sportON",false);
        predit.putBoolean("screenON",false);
        predit.putString("exitDate", "0");

        predit.putString("exitPackage", "0");
        predit.putLong("sportOnDate",0);
        predit.putLong("screenOnDate",0);

        predit.apply();
    }

    /** Tallennetaan aktiviteetin tila poistuessa, jotta sovellus muistaa mikä switchi on jätetty päälle ja ajan milloin
     * aktiviteetista poistuttiin. Tämän avulla saadaan laskettua kulunut aika vaikkei aktiviteetti ole auki. */
    public void saveExitState(){
        Log.d(LOGTAG,"saveExitState()");

        SharedPreferences shpref = this.getSharedPreferences("AppState", Activity.MODE_PRIVATE);
        SharedPreferences.Editor predit = shpref.edit();

        predit.putBoolean("sportON",sportSwitch.isChecked());
        predit.putBoolean("screenON",screenSwitch.isChecked());

        Date timeNow = new Date();
        predit.putString("exitDate", DateFor.format(timeNow));
        Log.d(LOGTAG,"quit date: "+DateFor.format(timeNow));

        predit.putString("exitPackage", todayObject.getPackageName());
        Log.d(LOGTAG,"package used before quit: "+todayObject.getPackageName());

        if(sportSwitch.isChecked()){
            predit.putLong("sportOnDate",timeNow.getTime());
        } else {
            predit.putLong("sportOnDate",0);
        }
        if(screenSwitch.isChecked()){
            predit.putLong("screenOnDate",timeNow.getTime());
        } else {
            predit.putLong("screenOnDate",0);
        }

        predit.apply();
    }

    private void createChangeButtons(){
        input = new EditText(this);

        //Luodaan dialogi nimen vaihdolle
        //Mahdollistaa nappia painaessa ponnahdusikkunan esiin tulon joka kysyy uutta nimeä
        AlertDialog.Builder weightBuilder = new AlertDialog.Builder(this);

        //Asetetaan otsikko, ikoni ja viesti dialogiin
        weightBuilder.setTitle(R.string.weight);
        weightBuilder.setIcon(R.drawable.retrad);
        weightBuilder.setMessage(R.string.weight);

        weightBuilder.setView(input);

        //Lisätään "syötä" napin toiminnallisuus sekä napin teksti painon syötölle
        weightBuilder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                boolean convertable = true;
                double wValue = 0;

                try {
                    wValue = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    convertable = false;
                }

                if(convertable){
                    mega.todayObject().insertWeight(wValue);
                    mega.todayObject().insertWeightBoolean(true);
                    mega.saveToday();
                    Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),"SYÖTÄ PAINO KILOISSA", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Lisätään "peruuta" napin toiminnallisuus sekä napin teksti nimen vaihdolle
        weightBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog changeWeight = weightBuilder.create();

        //Lisätään aktiviteetin nappiin toiminnallisuus joka avaa nimenvaihto dialogin
        weightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeWeight.show();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(LOGTAG,"onPause()");
        todayObject.insertSport(eCounter.getCurrent());
        todayObject.insertScreen(sCounter.getCurrent());
        Log.d(LOGTAG,"Counter values (sport, screen) "+eCounter.getCurrent()+", "+sCounter.getCurrent());
        mega.saveToday();
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(LOGTAG,"onStop()");
        saveExitState();
    }

}
