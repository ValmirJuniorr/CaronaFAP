package br.com.valmirosjunior.caronafap.model.dao;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.valmirosjunior.caronafap.patners.Observable;
import br.com.valmirosjunior.caronafap.patners.Observer;
import br.com.valmirosjunior.caronafap.model.Ride;
import br.com.valmirosjunior.caronafap.model.User;
import br.com.valmirosjunior.caronafap.model.enums.Type;
import br.com.valmirosjunior.caronafap.network.FaceBookManager;

/**
 * Created by junior on 10/04/17.
 */

public class RideDAO implements Observable {
    private DatabaseReference refToRides;
    private List<Observer> observers;
    private HashMap<String,Ride> rideMap;
    private List<Ride> rides;
    private Ride ride;
    private static RideDAO rideDao;


    private RideDAO() {
        refToRides = FirebaseFactory.getInstance().getReference("Rides");
        refToRides.keepSynced(true);
        rideMap = new HashMap<>();
        observers = new ArrayList<>();
        addChildAddEventListenerToRides();
    }

    public static RideDAO getInstance() {
        if (rideDao == null) {
            rideDao = new RideDAO();
        }
        return rideDao;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public void saveRide(Ride ride) {
        if (ride.getId() == null) {
            ride.setId(refToRides.push().getKey());
        }
        refToRides.child(ride.getId()).setValue(ride);
    }

    public void removeRide(String idRide) {
        refToRides.child(idRide).removeValue();

    }

    private void addChildAddEventListenerToRides() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                rideMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(Ride.class));
                notifyObservers();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                rideMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(Ride.class));
                notifyObservers();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                rideMap.remove(dataSnapshot.getKey());
                notifyObservers();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("Moved nodeFirebase", "move Child " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Cancelled Read Firebase", "loadPost:onCancelled", databaseError.toException());
            }
        };
        refToRides.addChildEventListener(childEventListener);
    }

    public Ride getRide(String idRide) {
        return rideMap.get(idRide);
    }

    public void getRide(String idRide, final Observer observer) {
        Ride ride = rideMap.get(idRide);
        if (ride== null){
            refToRides.child(idRide).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    observer.update(dataSnapshot.getValue(Ride.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    observer.update(null);
                }
            });
        }else{
            observer.update(ride);
        }
    }

    public List<Ride> getRides() {
        rides= new ArrayList<>();
        for (Map.Entry<String, Ride> rideEntry : rideMap.entrySet()){
            rides.add(rideEntry.getValue());
        }
        return rides;
    }

    public List<Ride> getMyRides(){
        User user = FaceBookManager.getCurrentUser();
        Ride ride;
        rides= new ArrayList<>();
        for (Map.Entry<String, Ride> rideEntry : rideMap.entrySet()){
            ride= rideEntry.getValue();
            if(ride.getUser().equals(user)){
                rides.add(rideEntry.getValue());
            }
        }
        return rides;

    }

    public List<Ride> getOtherRides(Ride myRide){
        User user = FaceBookManager.getCurrentUser();
        Ride ride;
        rides= new ArrayList<>();
        for (Map.Entry<String, Ride> rideEntry : rideMap.entrySet()){
            ride= rideEntry.getValue();
            if(!ride.getUser().equals(user)){
                if(ride.getType()!= myRide.getType()){
                    if (ride.diferenceTime(myRide)<30){
                        if(myRide.getOrigin().distanceToLocation(ride.getOrigin())< 1000 &&
                           myRide.getDestination().distanceToLocation(ride.getDestination())<1000){
                            rides.add(ride);
                        }
                    }
                }
            }
        }
        return rides;

    }

    @Override
    public void notifyObservers() {
        Type type = null;
        List<Ride> myRides =null, otherRides = null;
        for (Observer observer: observers){
            type = observer.getType();
            if(type == Type.MINE){
                if( myRides == null){
                    myRides = getMyRides();
                }
                observer.update(this,myRides);
            } else {
                if (otherRides == null){
                    if (ride !=   null)
                        otherRides = getOtherRides(ride);
                }
                observer.update(this,otherRides);
            }

        }
    }

    @Override
    public void deleteObservers() {
        observers = new ArrayList<>();
    }

    @Override
    public void addObserver(Observer o) {
        if (!observers.contains(o)){
        observers.add(o);
        }

    }

    @Override
    public void deleteObserver(Observer o) {
        observers.remove(o);
    }
}