package com.example.user.simpleui;

import com.parse.ParseException;
import com.parse.SaveCallback;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by user on 2016/5/5.
 */
public class SaveCallbackWithRealm implements SaveCallback{
    RealmObject realmObject;
    SaveCallback saveCallback;

    public  SaveCallbackWithRealm(RealmObject realmObject, SaveCallback saveCallback) {
        this.realmObject = realmObject;
        this.saveCallback = saveCallback;
    }

    @Override
    public void done(ParseException e) {
        Realm realm = Realm.getDefaultInstance();

        // Persist your data easily
        realm.beginTransaction();
        realm.copyToRealm(realmObject);
        realm.commitTransaction();

        realm.close();

        saveCallback.done(e);
    }
}
