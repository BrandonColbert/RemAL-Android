package exn.database.remal;

import android.os.Bundle;
import android.os.Handler;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import java.net.InetAddress;
import java.util.HashMap;

import exn.database.remal.core.RemAL;
import exn.database.remal.devices.MultiDeviceMode;
import exn.database.remal.devices.RemoteLanDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.devices.RemoteWiFiDevice;
import exn.database.remal.devices.SubDevicePack;

public class DeviceOptionsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private final Handler lanDevicesHandler = new Handler();
    private final HashMap<String, String> lanAddresses = new HashMap<>();
    private RemoteMultiDevice device;
    private boolean lanSearching;

    private final Runnable lanCallback = new Runnable() {
        public void run() {
            if(device.getPack(MultiDeviceMode.LAN).isEnabled() && lanSearching) {
                device.getSubDevice(RemoteLanDevice.class).findDevices(packs -> {
                    lanAddresses.clear();

                    for(RemoteLanDevice.LanDeviceDiscoveryPack pack : packs) {
                        InetAddress ip = pack.packet.getAddress();
                        String address = ip.getHostAddress();
                        String desc = ip.getHostName() + "@" + address;

                        lanAddresses.put(address, desc);
                    }

                    lanDevicesHandler.post(this);
                }, 1000);
            }
        }
    };

    public void setDevice(RemoteMultiDevice device) {
        this.device = device;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.device_preferences, rootKey);

        PreferenceScreen screen = getPreferenceScreen();

        for(int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);

            if(device != null)
                initPreference(pref);

            pref.setOnPreferenceChangeListener(this);
            pref.setOnPreferenceClickListener(this);
        }

        enableLanSearch();
    }

    @Override
    public void onStop() {
        super.onStop();
        disableLanSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        disableLanSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableLanSearch();
    }

    private void enableLanSearch() {
        if(!lanSearching) {
            lanSearching = true;
            lanDevicesHandler.post(lanCallback);
        }
    }

    private void disableLanSearch() {
        if(lanSearching) {
            lanSearching = false;
            lanDevicesHandler.removeCallbacks(lanCallback);
            lanAddresses.clear();
        }
    }

    private void initPreference(Preference pref) {
        switch(pref.getKey()) {
            case "device_name":
                pref.setSummary(device.getName());
                break;
            case "usb_enabled":
                ((SwitchPreference)pref).setChecked(device.getPack(MultiDeviceMode.USB).isEnabled());
                break;
            case "lan_enabled":
                ((SwitchPreference)pref).setChecked(device.getPack(MultiDeviceMode.LAN).isEnabled());
                break;
            case "lan_port":
                pref.setSummary(String.valueOf(device.getSubDevice(RemoteLanDevice.class).getPort()));
                break;
            case "lan_address":
                pref.setSummary(device.getSubDevice(RemoteLanDevice.class).getAddress());
                break;
            case "bt_enabled":
                ((SwitchPreference)pref).setChecked(device.getPack(MultiDeviceMode.BLUETOOTH).isEnabled());
                break;
            case "wifi_enabled":
                ((SwitchPreference)pref).setChecked(device.getPack(MultiDeviceMode.WIFI).isEnabled());
                break;
            case "wifi_port":
                pref.setSummary(String.valueOf(device.getSubDevice(RemoteWiFiDevice.class).getPort()));
                break;
            case "wifi_address":
                pref.setSummary(device.getSubDevice(RemoteWiFiDevice.class).getAddress());
                break;
            case "ssh_enabled":
                ((SwitchPreference)pref).setChecked(device.getPack(MultiDeviceMode.SSH).isEnabled());
                break;
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        try {
            //TODO: Make sure everything is saved

            switch(pref.getKey()) {
                case "device_name":
                    if(RemAL.renameDevice(pref.getSummary().toString(), newValue.toString()))
                        pref.setSummary(device.getName());

                    break;
                case "lan_port": {
                    RemoteLanDevice d = device.getSubDevice(RemoteLanDevice.class);

                    if(newValue.toString().length() > 0) {
                        d.setPort(Integer.valueOf(newValue.toString()));
						RemAL.saveDevice(device);

                        pref.setSummary(String.valueOf(d.getPort()));
                    }

                    break;
                }
                case "lan_address": {
                    RemoteLanDevice d = device.getSubDevice(RemoteLanDevice.class);
                    d.setAddress(newValue.toString());
                    RemAL.saveDevice(device);

                    pref.setSummary(d.getAddress());
                    break;
                }
                case "lan_device_list": {
                    RemoteLanDevice d = device.getSubDevice(RemoteLanDevice.class);
                    d.setAddress(newValue.toString());
					RemAL.saveDevice(device);

                    findPreference("lan_address").setSummary(d.getAddress());

                    break;
                }
                case "wifi_port": {
                    RemoteWiFiDevice d = device.getSubDevice(RemoteWiFiDevice.class);

                    if(newValue.toString().length() > 0) {
                        d.setPort(Integer.valueOf(newValue.toString()));
						RemAL.saveDevice(device);

                        pref.setSummary(String.valueOf(d.getPort()));
                    }

                    break;
                }
                case "wifi_address": {
                    RemoteWiFiDevice d = device.getSubDevice(RemoteWiFiDevice.class);
                    d.setAddress(newValue.toString());
					RemAL.saveDevice(device);

                    pref.setSummary(d.getAddress());
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean onPreferenceClick(Preference pref) {
        switch(pref.getKey()) {
            case "device_name":
                ((EditTextPreference)pref).setText(device.getName());

                break;
            case "usb_enabled": {
                SubDevicePack pack = device.getPack(MultiDeviceMode.USB);
                pack.setEnabled(!pack.isEnabled());
                RemAL.saveDevice(device);

                ((SwitchPreference)pref).setChecked(pack.isEnabled());

                break;
            }
            case "lan_enabled": {
                SubDevicePack pack = device.getPack(MultiDeviceMode.LAN);
                pack.setEnabled(!pack.isEnabled());
                RemAL.saveDevice(device);

                ((SwitchPreference)pref).setChecked(pack.isEnabled());

                if(pack.isEnabled())
                    enableLanSearch();
                else
                    disableLanSearch();

                break;
            }
            case "lan_port":
                ((EditTextPreference)pref).setText(String.valueOf(device.getSubDevice(RemoteLanDevice.class).getPort()));
                break;
            case "lan_address":
                ((EditTextPreference)pref).setText(device.getSubDevice(RemoteLanDevice.class).getAddress());
                break;
            case "lan_device_list": {
                ListPreference p = (ListPreference)pref;

                if(lanAddresses.size() > 0) {
                    p.setEntries(lanAddresses.values().toArray(new String[0]));
                    p.setEntryValues(lanAddresses.keySet().toArray(new String[0]));
                } else {
                    p.setEntries(new String[]{"None found"});
                    p.setEntryValues(new String[]{""});
                }
                break;
            }
            case "bt_enabled":{
                SubDevicePack pack = device.getPack(MultiDeviceMode.BLUETOOTH);
                pack.setEnabled(!pack.isEnabled());
                RemAL.saveDevice(device);

                ((SwitchPreference)pref).setChecked(pack.isEnabled());

                break;
            }
            case "wifi_enabled": {
                SubDevicePack pack = device.getPack(MultiDeviceMode.WIFI);
                pack.setEnabled(!pack.isEnabled());
                RemAL.saveDevice(device);

                ((SwitchPreference)pref).setChecked(pack.isEnabled());

                break;
            }
            case "wifi_port":
                ((EditTextPreference)pref).setText(String.valueOf(device.getSubDevice(RemoteWiFiDevice.class).getPort()));
                break;
            case "wifi_address":
                ((EditTextPreference)pref).setText(device.getSubDevice(RemoteWiFiDevice.class).getAddress());
                break;
            case "ssh_enabled": {
                SubDevicePack pack = device.getPack(MultiDeviceMode.SSH);
                pack.setEnabled(!pack.isEnabled());
                RemAL.saveDevice(device);

                ((SwitchPreference)pref).setChecked(pack.isEnabled());

                break;
            }
            case "delete_device": {
                RemAL.deleteDevice(device.getName());
                getActivity().finish();

                break;
            }
        }

        return true;
    }
}
