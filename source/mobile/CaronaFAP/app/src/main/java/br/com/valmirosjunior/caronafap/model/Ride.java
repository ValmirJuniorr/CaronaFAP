package br.com.valmirosjunior.caronafap.model;

import com.google.firebase.database.Exclude;

import br.com.valmirosjunior.caronafap.model.enums.Type;
import br.com.valmirosjunior.caronafap.util.FaceBookManager;

/**
 * Created by junior on 28/03/17.
 */

public class Ride {

    private String id,idUser;
    private User user;
    private MyLocation origin, destination;
    private int hourInMinutes;
    private Type type;


    public Ride() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    @Exclude
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MyLocation getOrigin() {
        return origin;
    }

    public void setOrigin(MyLocation origin) {
        this.origin = origin;
    }

    public MyLocation getDestination() {
        return destination;
    }

    public int getHourInMinutes() {
        return hourInMinutes;
    }

    public void setHourInMinutes(int hourInMinutes) {
        this.hourInMinutes = hourInMinutes;
    }

    public void setDestination(MyLocation destination) {
        this.destination = destination;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String formaterTime(){
        int hour, minutes;
        String time="";
        hour =hourInMinutes/60;
        minutes = hourInMinutes % 60;
        time += (hour<10)? "0"+hour : hour;
        time += ":";
        time += ((minutes<10)? "0"+minutes:minutes);
        return time;
    }

    public int diferenceTime(Ride ride){
        int timeRide=ride.getHourInMinutes();
        int dif = timeRide-hourInMinutes;
        return (dif>0) ? dif : (-1)* dif;
    }

    public boolean isMine(){
        return user.equals(FaceBookManager.getCurrentUser());
    }

    public String toString(String userName){
        return userName+" "+toString();
    }

    @Override
    public String toString() {
        return  "Está " + ((type == Type.ORDERED) ?
                "Pedindo " : "Oferecendo ")+" as: "+formaterTime()+" Hs!"+
                "\nDe : "+origin.getAdress()+
                "\nPara : "+destination.getAdress();
    }


    public String showDescription() {
        String name = isMine()? "Você": user.getName();
        return  "<strong>"+name+"</strong> está: "+((type == Type.ORDERED) ?
                "Pedindo " : "Oferecendo ")+" Carona!" +
               showRide();
    }

    public String showShortDescrpition(){
        return  getUser().getName()+" Está " + ((type == Type.ORDERED) ?
                "Pedindo " : "Oferecendo ")+ "uma Carona! "+
                showRide();
    }

    public String showDescrpitionSolicitation(){
        return  "<strong>Quero a carona que você está " + ((type == Type.ORDERED) ?
                "Pedindo" : "Oferecendo")+"!</strong>"+
                showRide();
    }

    private String showRide(){
        return "<br><strong>Horário:</strong> "+formaterTime()+"Hs"+
                "<br><strong>De: </strong> "+origin.shortAdress()+
                "<br><strong>Para: </strong> "+destination.shortAdress();
    }
}
