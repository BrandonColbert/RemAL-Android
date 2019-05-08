package exn.database.remal;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import exn.database.remal.core.RemAL;
import exn.database.remal.devices.MultiDeviceMode;
import exn.database.remal.devices.RemoteLanDevice;
import exn.database.remal.devices.RemoteMultiDevice;
import exn.database.remal.devices.RemoteWiFiDevice;
import exn.database.remal.devices.SubDevicePack;

public class DeviceOptionsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private RemoteMultiDevice device;
    private volatile boolean isListPopulating;

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
                RemoteLanDevice d = device.getSubDevice(RemoteLanDevice.class);

                isListPopulating = true;

                d.findDevices(packs -> {
                    int length = packs.length;

                    System.out.println("Found: " + length);

                    if(length > 0) {
                        String[] addressStrings = new String[length];
                        String[] addressDescriptions = new String[length];

                        for(int i = 0; i < length; i++) {
                            String address = packs[i].packet.getAddress().getHostAddress();

                            addressStrings[i] = address;
                            addressDescriptions[i] = packs[i].packet.getAddress().getHostName() + " | " + address;
                        }

                        p.setEntries(addressDescriptions);
                        p.setEntryValues(addressStrings);
                    } else {
                        p.setEntries(new String[]{"None found"});
                        p.setEntryValues(new String[]{""});
                    }

                    isListPopulating = false;
                });

                while(isListPopulating);

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
