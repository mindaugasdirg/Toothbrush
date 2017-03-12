package hakergames.toothbrush.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static hakergames.toothbrush.MainScreen.UNLOCKED_ACHIEVEMENTS;

public class Achievement {
    private int id;
    private String name;
    private String description;
    private boolean unlocked;
    private int exp;
    private Requirement requirement;

    public Achievement(int id, String name, String description, int exp, Requirement requirement){
        this.id = id;
        this.name = name;
        this.description = description;
        this.exp = exp;
        this.unlocked = false;
        this.requirement = requirement;
    }

    private void unlock(){
        unlocked = true;
    }

    public void unlockNoXp(){
        unlocked = true;
    }

    public int check(){
        if(requirement.check() && !unlocked){
            unlock();
            return exp;
        }

        return 0;       // if requirement not met 0 xp will be added
    }

    public boolean isUnlocked(){
        return unlocked;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public interface Requirement {
        public boolean check();
    }

    public static List<Achievement> loadAchievements(Context context, final User user){
        SharedPreferences achievementsInfo = context.getSharedPreferences(UNLOCKED_ACHIEVEMENTS, 0);
        List<Achievement> Achievements = new ArrayList<Achievement>();
        String[] Names = {
                "30 min",
                "2 kartai",
                "2 min"
        };
        String[] Descriptions = {
                "Iš viso valeisi 30 min.",
                "Dantys išvalyti 2 kartus.",
                "Dantys buvo valomi 2 min nesustojus."
        };
        int[] Exps = {
                34, 33, 33
        };
        Achievement.Requirement[] Requirements = {
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("TT", Float.toString(user.getTotalTime() / 1000f));
                        return (user.getTotalTime() / 1000f) >= 1800;
                    }
                },
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("GT", Integer.toString(user.getTimes()));
                        return user.getTimes() >= 2;
                    }
                },
                new Achievement.Requirement() {
                    @Override
                    public boolean check() {
                        Log.d("LT", Float.toString(user.getLastTime() / 1000f));
                        return (user.getLastTime() / 1000f) >= 120;
                    }
                }
        };

        for(int i = 0; i < Names.length; i++){
            Achievement achievement = new Achievement(i, Names[i], Descriptions[i], Exps[i], Requirements[i]);

            String key = Integer.toString(i);
            if(achievementsInfo.getBoolean(key, false)){
                achievement.unlockNoXp();
            }

            Achievements.add(achievement);
        }

        return Achievements;
    }
}
