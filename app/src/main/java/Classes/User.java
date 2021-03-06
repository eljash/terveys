package Classes;

import java.util.Date;

/**
 * @author Eljas Hirvelä ja Jukka Hallikainen
 */
public class User {

    private String name;
    private double startWeight;
    private double weightGoal;
    private int sportTimeGoal;
    private int screenTimeGoal;
    private int year;
    private int month;
    private int day;
    private Date creationDate;

    public User(String name, double startWeight, int sportTimeGoal, int screenTimeGoal) {
        this.name = name;
        this.startWeight = startWeight;
        this.sportTimeGoal = sportTimeGoal;
        this.screenTimeGoal = screenTimeGoal;
        this.creationDate = new Date();
    }

    /**
     * Konstruktori ottaa tarvittavat parametrit vastaan luodakseen uuden profiilin käyttäjän tiedoilla.
     */
    public User(String n, double sW, double wG, int sportTG, int screenTG, int y, int m, int d, Date cD){
        this.name = n;
        this.startWeight = sW;
        this.weightGoal = wG;
        this.sportTimeGoal = sportTG;
        this.screenTimeGoal = screenTG;
        this.year = y;
        this.month = m;
        this.day = d;
        this.creationDate = cD;
    }

    public void setName(String n){this.name = n;}
    public void setStartWeight(double sw){this.startWeight = sw;}
    public void setWeightGoal(double wg){this.weightGoal = wg;}
    public void setSportTimeGoal(int stg){this.sportTimeGoal = stg;}
    public void setScreenTimeGoal(int stg){this.screenTimeGoal = stg;}
    public void setYear(int y){this.year = y;}
    public void setMonth(int m){this.month = m;}
    public void setDay(int d){this.day = d;}
    public void setDate(Date d){this.creationDate = d;}

    public String name(){return this.name;}
    public double startWeight(){return this.startWeight;}
    public double weightGoal(){return this.weightGoal;}
    public int sportTimeGoal(){return this.sportTimeGoal;}
    public int screenTimeGoal(){return this.screenTimeGoal;}
    public int year(){return this.year;}
    public int month(){return this.month;}
    public int day(){return this.day;}
    public Date date(){return this.creationDate;}
}
