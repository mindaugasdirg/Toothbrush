package hakergames.toothbrush;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import static android.support.v4.content.ContextCompat.getColor;

public class AchievementAdapter extends BaseAdapter {
    private Context context;
    private List<Achievement> Achievements;

    public AchievementAdapter(Context context, List<Achievement> Achievements){
        this.context = context;
        this.Achievements = Achievements;
    }

    public int getCount(){
        return Achievements.size();
    }

    public View getView(int position, View view, ViewGroup viewholder){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View achievementAdapter;

        achievementAdapter = new View(context);
        achievementAdapter = inflater.inflate(R.layout.achievement_grid, null);

        TextView name = (TextView)achievementAdapter.findViewById(R.id.name);
        name.setText(Achievements.get(position).getName());

        TextView unlocked = (TextView)achievementAdapter.findViewById(R.id.unlocked);
        if(Achievements.get(position).isUnlocked()){
            unlocked.setText("Unlocked");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                unlocked.setBackgroundColor(context.getResources().getColor(R.color.colorAccent, null));
            }
        } else {
            unlocked.setText("Locked");
        }

        return achievementAdapter;
    }

    public Object getItem(int position){
        return null;
    }

    public long getItemId(int position){
        return 0;
    }
}
