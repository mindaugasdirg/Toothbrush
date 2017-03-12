package hakergames.toothbrush;

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
        if(requirement.check()){
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
}
