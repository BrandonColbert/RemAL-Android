package exn.database.remal;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import exn.database.remal.config.PersistenceUtils;
import exn.database.remal.core.RemAL;
import exn.database.remal.events.ColumnAmountChangedEvent;
import exn.database.remal.events.DeckColorChangedEvent;

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
            case "color_deck_background":
                pref.setSummary(PersistenceUtils.loadValue(pref.getKey(), RemAL.convertColorToCode(getResources().getColor(R.color.colorPrimaryDark))));
                break;
            case "color_deck_tile":
                pref.setSummary(PersistenceUtils.loadValue(pref.getKey(), RemAL.convertColorToCode(getResources().getColor(R.color.colorAccent))));
                break;
            case "color_deck_text":
                pref.setSummary(PersistenceUtils.loadValue(pref.getKey(), RemAL.convertColorToCode(getResources().getColor(R.color.colorAltText))));
                break;
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        switch(pref.getKey()) {
            case "appearance_columns": {
                String v = newValue.toString();

                pref.setSummary(v);
                PersistenceUtils.saveValue(pref.getKey(), v);
                RemAL.post(new ColumnAmountChangedEvent(Integer.valueOf(v)));
                break;
            }
            case "color_deck_background": case "color_deck_tile": case "color_deck_text": {
                String s = newValue.toString();

                while(s .length() < 6)
                    s = s.concat("0");

                String v = "#" + s;

                pref.setSummary(v);
                PersistenceUtils.saveValue(pref.getKey(), v);
                RemAL.post(new DeckColorChangedEvent());
                break;
            }
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        switch(pref.getKey()) {
            case "appearance_columns":
                ((EditTextPreference)pref).setText(pref.getSummary().toString());
                break;
            case "color_deck_background": case "color_deck_tile": case "color_deck_text":
                ((EditTextPreference)pref).setText(pref.getSummary().toString().substring(1));
                break;
            case "deck_colors_reset":
                RemAL.post(new DeckColorChangedEvent(true));
                findPreference("color_deck_background").setSummary(PersistenceUtils.loadValue("color_deck_background"));
                findPreference("color_deck_tile").setSummary(PersistenceUtils.loadValue("color_deck_tile"));
                findPreference("color_deck_text").setSummary(PersistenceUtils.loadValue("color_deck_text"));
                break;
        }

        return true;
    }
}
