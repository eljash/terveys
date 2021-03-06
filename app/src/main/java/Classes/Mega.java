package Classes;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author Eljas Hirvelä
 *
 */
public class Mega {

    private Context activityContext;

    private ArrayList<dataOlio> SaveList = new ArrayList<>();

    private ArrayList<dataOlio> thisMonthList = new ArrayList<>();
    private dataOlio today;
    private String currentDate;

    private String currentSavePackage = null;
    private dataOlio dayToHandle;

    private SuperMetodit SM = new SuperMetodit();

    private static final String LOGTAG = "MEGA.JAVA";

    public Mega(Context context){
        /**
         * Luokan konstruktori ottaa parametrinä aktiviteetin Context konstekstin, jotta se voi käyttää
         * SharedPreferencesiä ja sen ominaisuuksia.
         */
        this.activityContext = context;
        Log.d(LOGTAG, "Constructing");
    }

    /** Katsoo onko päivämäärälle jo olemassa dataa
     * Jos ei ole, niin tekee uuden "päivän" listaan
     * ja tallentaa listan saveData() metodilla
     * */
    public void todayData(){
        Log.d(LOGTAG, "todayData()");

        String year = SM.customDigit(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),4);
        String month = SM.customDigit(Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1),2);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        //Log.d(LOGTAG,"checking if data package exists...");
        if(!checkForDataPackage(year,month)){
            //Log.d(LOGTAG,"data package not found. Creating.");
            createDataPackage(year,month);
        }

        thisMonthList = getSavedData(year,month);
        currentDate = SM.createPackageName(year,month);
        //Log.d(LOGTAG,"this months data package opened");

        today = dayFromList(day,thisMonthList);
        if(today == null){
            //Log.d(LOGTAG, "no data for this day");
            today = new dataOlio(currentDate,year,month);
            if(thisMonthList.size() != 0){
                today.insertWeight(thisMonthList.get(thisMonthList.size()-1).returnWeight());
                //Log.d(LOGTAG, "adding new day to array with weight value [OK]");
            }
            today.changeDay(day);
            thisMonthList.add(today);
            savePackage(year,month,thisMonthList);
        } else {
            //Log.d(LOGTAG, "this day has data");
        }
    }

    /** Palauttaa nykyisen päivän dataolion, joka sisältää tämän päivän tiedot*/
    public dataOlio todayObject(){
        if(today == null){
            todayData();
        }else{
            saveToday();
        }
        return today;
    }

    /** Datapaketin tallennus metodi. Metodi tallentaa aukinaisen paketin SharedPreferencesiin. */
    public void saveDataPackage(){
        Log.d(LOGTAG,"saveDataPackage()");
        SharedPreferences dataSaving = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = dataSaving.edit();
        Gson gson = new Gson();
        String json = gson.toJson(SaveList);
        //Log.d(LOGTAG,"JSON format:");
        //Log.d(LOGTAG,json);
        dataEditor.putString(currentSavePackage, json);
        dataEditor.apply();
    }

    /** Tallentaa nykyisen päivän. */
    public void saveToday(){
        Log.d(LOGTAG,"saveToday()");
        SharedPreferences dataSaving = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = dataSaving.edit();
        Gson gson = new Gson();
        String json = gson.toJson(thisMonthList);
        //Log.d(LOGTAG,"Json format:");
        //Log.d(LOGTAG,json);
        dataEditor.putString(currentDate, json);
        dataEditor.apply();
    }

    /** Tallentaa tietyn oliolistan tiettyyn datapakettiin. Oliolista otetaan "ArrayList<dataOlio>" muodossa ja
     * datapaketin nimi luodaan parametreinä saaduista vuodesta ja kuukaudesta. */
    private void savePackage(String v, String m, ArrayList<dataOlio> dataList){
        Log.d(LOGTAG,"savePackage()");
        v = SM.customDigit(v,4);
        m = SM.customDigit(m, 2);
        String saveName = SM.createPackageName(v,m);
        SharedPreferences dataSaving = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = dataSaving.edit();
        Gson gson = new Gson();
        String json = gson.toJson(dataList);
        //Log.d(LOGTAG,"JSON format:");
        //Log.d(LOGTAG,json);
        dataEditor.putString(saveName, json);
        dataEditor.apply();
    }

    /**
     * Metodi ottaa parametreinä vuoden ja kuukauden merkkijonoina ja etsii
     * näiden perusteella oikean datapaketin, jonka metodi sitten palauttaa.
     */
    public ArrayList<dataOlio> loadData(String year, String month){
        Log.d(LOGTAG,"loadData() with parameters");
        //Log.d(LOGTAG,"Parameters: y:"+year+" m: "+month);
        if(year.matches("[0-9]+")&&month.matches("[0-9]+")){
            //Log.d(LOGTAG,"all Strings have only digits[OK]");
            if(year.length() == 4 && month.length() == 2){
                //Log.d(LOGTAG,"every parameter has correct length [OK]");
            } else {
                //Log.d(LOGTAG,"not every parameter has the correct length [CONVERTING]");
                year = SM.customDigit(year,4);
                month = SM.customDigit(month,2);
                //Log.d(LOGTAG,"Parameters: y:"+year+" m: "+month);
            }
            saveDataPackage();
            return getSavedData(year,month);
        } else {
            //Log.d(LOGTAG,"some of the values have characters [FAILED]");
            //Log.d(LOGTAG,"Parameters: y:"+year+" m: "+month);
        }
        return null;
    }

    private ArrayList<dataOlio> getSavedData(String v, String m){
        String saveName = SM.createPackageName(v,m);
        Log.d(LOGTAG,"getSavedData() - "+saveName);
        SharedPreferences dataSaving = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dataSaving.getString(saveName, "Empty");
        //Log.d(LOGTAG,json);
        if(!json.equals("Empty")){
            this.SaveList = gson.fromJson(json, new TypeToken<ArrayList<dataOlio>>(){}.getType());
            currentSavePackage = saveName;
            SaveList = gson.fromJson(json, new TypeToken<ArrayList<dataOlio>>(){}.getType());
            //Log.d(LOGTAG,"Data package '"+saveName+"' loaded successfully! [OK]");
            if(gson.fromJson(json, new TypeToken<ArrayList<dataOlio>>(){}.getType()) == null || ! (gson.fromJson(json, new TypeToken<ArrayList<dataOlio>>(){}.getType()) instanceof ArrayList)){
                SaveList = new ArrayList<>();
                return new ArrayList<>();
            }
            return gson.fromJson(json, new TypeToken<ArrayList<dataOlio>>(){}.getType());
        } else {
            //Log.d(LOGTAG,"no data package with name: "+saveName+" [FAILED]");
        }
        return null;
    }

    /** Katsoo onko vuotta ja kuukautta vastaavaa datapakettia olemassa: true = on, false = ei*/
    private boolean checkForDataPackage(String v, String m){
        String saveName = SM.createPackageName(v,m);
        Log.d(LOGTAG,"checkForDataPackage() - "+saveName);
        SharedPreferences dataSaving = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        String json = dataSaving.getString(saveName, "Empty");
        if(!json.equals("Empty")){
            //Log.d(LOGTAG,"Data package '"+saveName+"' found! [OK]");
            return true;
        } else {
            //Log.d(LOGTAG,"no data package with name: "+saveName);
        }
        return false;
    }

    /** Etsii halutun päivän listasta. Metodi ottaa parametreinä olilistan "ArrayList<dataOlio>" ja kokonaislukuna päivän */
    public dataOlio dayFromList(int d,ArrayList<dataOlio> objectList){
        Log.d(LOGTAG,"dayFromList()");
        //Log.d(LOGTAG, "Searching object with day: "+d);
        if(objectList != null){
            int size = objectList.size();
            //Log.d(LOGTAG,"object list size: "+size);
            if (size > 0) {
                for (int i = (size - 1); i >= 0; i--) {
                    if (objectList.get(i).getDay() == d) {
                        //Log.d(LOGTAG,"Object found with day: "+d+"[OK]");
                        return objectList.get(i);
                    }
                }
            }
        } else {
            //Log.d(LOGTAG,"object list empty: value = null [FAILED]");
            return null;
        }

        //Log.d(LOGTAG,"Object with day: "+d+" not found... Returning null [FAILED]");
        return null;
    }

    /** Luo uuden datapaketin. Parametreinä vuosi ja kuukausi, jotta voidaan luoda oikeanlainen nimi paketille. */
    private void createDataPackage(String v, String m){
        Log.d(LOGTAG,"createDataPackage()");
        if(!checkForDataPackage(v,m)){
            String packageName = SM.createPackageName(v,m);
            //Log.d(LOGTAG,"creating new data package with name: "+packageName);
            SharedPreferences newPackage = activityContext.getSharedPreferences("saveData",Activity.MODE_PRIVATE);
            SharedPreferences.Editor newPackageEditor = newPackage.edit();

            Gson gson = new Gson();
            String json = gson.toJson(new ArrayList<dataOlio>());
            //Log.d(LOGTAG,"JSON format:");
            //Log.d(LOGTAG,json);
            newPackageEditor.putString(packageName, json);
            newPackageEditor.apply();
            //Log.d(LOGTAG,"new package created! [OK]");
        } else {
            //Log.d(LOGTAG,"data package with year: "+v+" and month: "+m+" already exists! [FAILED]");
        }
    }

    /** Metodi tyhjentää koko SharedPreference tiedot. */
    public void clearData(){
        SharedPreferences SPref = activityContext.getSharedPreferences("saveData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor SprefEdit = SPref.edit();
        SprefEdit.clear();
        SprefEdit.apply();
        SPref = activityContext.getSharedPreferences("profile", Activity.MODE_PRIVATE);
        SprefEdit = SPref.edit();
        SprefEdit.clear();
        SprefEdit.apply();
    }

    /** Debuggaamista varten tehty metodi joka ottaa parametreinä päivämäärän ja kaikki arvot mitä halutaan sille päivälle
     *  lisätä. Tällä metodilla luodaan testaamista varten dataa useille päiville */
    public void insertData(String v, String m, int d, int sport, int screen, double weight, boolean day){
        Log.d(LOGTAG,"insertData()");
        ArrayList<dataOlio> dataList;
        dataOlio insertDay;

        //Log.d(LOGTAG,"inserting date year:"+v+"month: "+m+" day: "+d+" sport "+sport+" screet "+screen+" weight "+weight);

        v = SM.customDigit(v,4);
        m = SM.customDigit(m,2);

        if(!checkForDataPackage(v,m)){
            createDataPackage(v,m);
        }

        dataList = getSavedData(v,m);
        String packageName = SM.createPackageName(v,m);

        insertDay = dayFromList(d,dataList);

        if(insertDay == null){
            insertDay = new dataOlio(packageName,v,m);
        }
        insertDay.changeDay(d);
        insertDay.insertSport(sport);
        insertDay.insertScreen(screen);
        insertDay.insertWeight(weight);
        insertDay.insertWeightBoolean(day);
        dataList.add(insertDay);
        savePackage(v,m,dataList);

    }
}