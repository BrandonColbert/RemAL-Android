package exn.database.remal;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

public class TileOptionsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tile_preferences, rootKey);

        PreferenceScreen screen = getPreferenceScreen();

        for(int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            initPreference(pref);
            pref.setOnPreferenceChangeListener(this);
            pref.setOnPreferenceClickListener(this);
        }
    }

    private void initPreference(Preference pref) {
        switch(pref.getKey()) {

        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch(pref.getKey()) {

        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        switch(pref.getKey()) {

        }

        return true;
    }
}
