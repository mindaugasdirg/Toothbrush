package hakergames.toothbrush;

import java.util.Date;

/**
 * Created by Mindaugas on 2017-03-11.
 */

public class User {
    private int id;
    private String username;
    private int exp;
    private int level;
    private int averageTime;
    private int streak;
    private int longestTime;
    private Date lastTime;

    public User(String username, int exp){
        this.username = username;
        this.exp = exp;

        averageTime = 0;
        streak = 0;
        longestTime = 0;
        lastTime = null;

        updateLevel();
    }

    public String getUsername(){
        return username;
    }

    private void updateLevel() {
        level = (int)(0.01 * exp + 1);
    }

    public int getLevel(){
        return level;
    }

    public void setExp(int exp){
        this.exp = exp;
    }

    public void addExp(int exp){
        this.exp += exp;
        updateLevel();
    }

    public int getExp(){
        return level;
    }

    public int getProgress(){
        int expToNext = ((level + 1) * 10) * 100;
        return exp / (expToNext - exp) * 100;
    }

    public void setAverageTime(int averageTime){
        this.averageTime = averageTime;
    }

    public int getAverageTime(){
        return averageTime;
    }

    public void setStreak(int streak){
        this.streak = streak;
    }

    public int getStreak(){
        return streak;
    }

    public void setLongestTime(int longestTime){
        this.longestTime = longestTime;
    }

    public int getLongestTime(){
        return longestTime;
    }

    public void setLastTime(Date lastTime){
        this.lastTime = lastTime;
    }

    public Date getLastTime(){
        return lastTime;
    }
}
