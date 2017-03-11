package hakergames.toothbrush;

import android.util.Log;

import java.util.Date;

/**
 * Created by Mindaugas on 2017-03-11.
 */

public class User {
    private int id;
    private String username;
    private int exp;
    private int level;

    public User(String username, int exp){
        this.username = username;
        this.exp = exp;
        level = 1;

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
        Log.d("DEBUG", expToNext + " " + expToCurrent);
        int progress = (int)((exp - expToCurrent) / (double)(expToNext - expToCurrent) * 100);
        return progress;
    }
}
