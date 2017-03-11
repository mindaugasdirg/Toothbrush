package hakergames.toothbrush;

public class Achievement {
    private int id;
    private String name;
    private String description;
    private boolean unlocked;

    public Achievement(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
        unlocked = false;
    }

    public void unlock(){
        unlocked = true;
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
}
