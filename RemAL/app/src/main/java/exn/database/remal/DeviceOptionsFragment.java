package exn.database.remal;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class DeviceOptionsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_preferences);
    }
}
