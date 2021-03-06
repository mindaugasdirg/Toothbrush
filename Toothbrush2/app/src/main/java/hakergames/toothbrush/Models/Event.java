package hakergames.toothbrush.Models;

import android.util.Log;

import java.util.Date;

public class Event {
    private Date eventDate;
    private long startTime;
    private long deltaTime;
    private boolean inProgress;

    public Event(){
        Log.d("SB", "creating new event");
        eventDate = new Date();
        startTime = System.currentTimeMillis();

        inProgress = true;
    }

    public Event(Date eventDate, long deltaTime){
        this.eventDate = eventDate;
        this.deltaTime = deltaTime;
    }

    public void endEvent(){
        Log.d("SB", "ending event");
        deltaTime = System.currentTimeMillis() - startTime;

        inProgress = false;
    }

    public boolean isInProgress(){
        return inProgress;
    }

    public Date getEventDate(){
        return eventDate;
    }

    public long getDeltaTime(){
        return deltaTime;
    }
}
