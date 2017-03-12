package hakergames.toothbrush;

import android.util.Log;

import java.util.Date;

public class User {
    private int id;
    private String username;
    private int exp;
    private int level;
    private long totalTime;
    private long lastTime;
    private Date lastDate;
    private int times;

    public User(String username, int exp){
        this.username = username;
        this.exp = exp;
        level = 1;
        totalTime = 0;
        times = 0;

        updateLevel();
    }

    public String getUsername(){
        return username;
    }

    private void updateLevel() {
        int expNeed = (level + 1) * 10 - 10;
        while(exp >= expNeed){
            level++;
            expNeed = (level + 1) * 10 - 10;
        }
    }

    public void setTimes(int times){
        this.times = times;
    }

    public int getTimes(){
        return times;
    }

    public void setLastDate(Date date){
        lastDate = date;
    }

    public Date getLastDate(){
        return lastDate;
    }

    public void setLastTime(long time){
        lastTime = time;
    }

    public long getLastTime(){
        return lastTime;
    }

    public void addTotalTime(long time){
        totalTime += time;
    }

    public long getTotalTime(){
        return totalTime;
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
        return exp;
    }

    public int getProgress(){
        int expToNext = (level + 1) * 10 - 10;
        int expToCurrent = level * 10 - 10;
        int progress = (int)((exp - expToCurrent) / (double)(expToNext - expToCurrent) * 100);
        return progress;
    }
}
