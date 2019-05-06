package exn.database.remal;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.core.RemAL;
import exn.database.remal.events.ColumnAmountChangedEvent;

public class DeckSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.deck_preferences, rootKey);

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
            case "appearance_columns":
                pref.setSummary(PersistenceUtils.loadValue(pref.getKey(), String.valueOf(Deck.DEFAULT_COLUMNS)));
                break;
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch(pref.getKey()) {
            case "appearance_columns":
                String v = newValue.toString();

                pref.setSummary(v);
                PersistenceUtils.saveValue(pref.getKey(), v);
                RemAL.post(new ColumnAmountChangedEvent(Integer.valueOf(v)));
                break;
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        switch(pref.getKey()) {
            case "appearance_columns":
                ((EditTextPreference)pref).setText(pref.getSummary().toString());
                break;
        }

        return true;
    }
}
