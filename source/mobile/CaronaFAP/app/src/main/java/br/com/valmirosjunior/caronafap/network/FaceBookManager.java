package br.com.valmirosjunior.caronafap.network;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.valmirosjunior.caronafap.model.User;
import br.com.valmirosjunior.caronafap.model.dao.UserDAO;
import br.com.valmirosjunior.caronafap.model.enums.Status;
import br.com.valmirosjunior.caronafap.util.MessageUtil;


/**
 * Created by junior on 10/03/17.
 */

public class FaceBookManager extends Observable {
    private Context context;
    private FirebaseAuth firebaseAuth;
    private static User user;
    private List<Observer> observers;
    private Status status;

    public FaceBookManager(Activity context){
        firebaseAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.observers = new ArrayList<>();
        this.setStatus(Status.NEUTRAL);
    }

    public void prepareLoginButton(LoginButton button,CallbackManager callbackManager){
        button.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_friends"));


        button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                    FaceBookManager.this.notifyObservers(Status.SUCCESS);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel(){
                FaceBookManager.this.notifyObservers(Status.CANCELED);
            }

            @Override
            public void onError(FacebookException error) {
                FaceBookManager.this.notifyObservers(Status.ERROR);
            }
        });
    }

    public void logout() {
        if(!this.isLoggedIn()) {
            return;
        }
        try {
            LoginManager.getInstance().logOut();
            firebaseAuth.signOut();
            this.notifyObservers(Status.SUCCESS);
        }catch (Exception e){
            this.notifyObservers(Status.ERROR);
            e.printStackTrace();

        }
    }

    public boolean isLoggedIn(){
        return AccessToken.getCurrentAccessToken() != null;
    }

    private static void saveCurrentUserOnSession(){
        Profile profile = Profile.getCurrentProfile();
        FaceBookManager.user = new User();
        FaceBookManager.user.setId(profile.getId());
        FaceBookManager.user.setName(profile.getName());
    }

    public static User getCurrentUser(){
        if(FaceBookManager.user == null) {
            FaceBookManager.saveCurrentUserOnSession();
        }
        return FaceBookManager.user;
    }

    private void setStatus(Status status) {
        this.status = status;
    }


    public void handleFacebookAccessToken(final AccessToken token) {
        final UserDAO userDAO = UserDAO.getInstance();

        MessageUtil.showProgressDialog(context);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FaceBookManager.this.setStatus(Status.NEUTRAL);
                        if (!task.isSuccessful()) {
                            FaceBookManager.this.notifyObservers(Status.ERROR.getValue());
                        }else{
                            userDAO.saveUser(getCurrentUser());
                            FaceBookManager.this.notifyObservers(Status.SUCCESS.getValue());
                        }
                    }
                });
    }

    @Override
    public synchronized void addObserver(Observer o) {
        this.observers.add(o);
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : this.observers) {
            o.update(this,status);
        }
    }

    @Override
    public void notifyObservers(Object arg) {
        try {
            setStatus((Status) arg);
            notifyObservers();
        }catch (IllegalFormatConversionException i){
            i.printStackTrace();
        }
    }

    @Override
    public synchronized void deleteObservers() {
        this.observers.clear();
    }

    @Override
    public synchronized int countObservers() {
        return this.observers.size();
    }
}