package br.com.valmirosjunior.caronafap.model;

/**
 * Created by junior on 10/04/17.
 */

public class User {
    private String id, name;
    private double note;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getNote() {
        return note;
    }

    public void setNote(double note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User){
            User otherUser =(User) obj;
            if (this.getId().equals(otherUser.getId())){
                return true;
            }
        }
        return false;
    }
}
