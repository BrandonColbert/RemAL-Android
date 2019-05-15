package exn.database.remal.devices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteLanDevice extends RemoteWiFiDevice {
    public class LanDeviceDiscoveryPack {
        public final DatagramPacket packet;
        public final byte[] data;

        public LanDeviceDiscoveryPack(DatagramPacket packet, byte[] data) {
            this.packet = packet;
            this.data = data;
        }
    }

    public interface LanDeviceCallback {
        void run(LanDeviceDiscoveryPack[] discoveries);
    }

    private static final String DETECT_STRING = "REMAL_DETECT";
    private static final byte[] DETECT_BYTES = DETECT_STRING.getBytes();

    private boolean isSearching;

    public RemoteLanDevice(String name) {
        super(name);
    }

    public RemoteLanDevice() {
        super();
    }

    public String getConnectionName() {
        return "LAN";
    }

    /**
     * Searches for devices on the network with the port for RemAL
     * @param callback Function to be called with the list of devices
     * @param timeout Amount of time to sped searching in milliseconds
     */
    public void findDevices(LanDeviceCallback callback, int timeout) {
        final int port = getPort();
        isSearching = true;

        try {
            //Start broadcast search
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            //Start access request to default broadcast address
            socket.send(new DatagramPacket(DETECT_BYTES, DETECT_BYTES.length, InetAddress.getByName("255.255.255.255"), port));

            //Loop through all
            Enumeration<NetworkInterface> netints = NetworkInterface.getNetworkInterfaces();

            while(netints.hasMoreElements()) {
                NetworkInterface netint = netints.nextElement();

                if(!netint.isLoopback() && netint.isUp()) {
                    //Send access request to all addresses within the net interface
                    for(InterfaceAddress addressInterface : netint.getInterfaceAddresses()) {
                        InetAddress address = addressInterface.getAddress();

                        try {
                            if(address != null)
                                socket.send(new DatagramPacket(DETECT_BYTES, DETECT_BYTES.length, address, port));
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //Start trying to find
            new Thread(() -> {
                List<LanDeviceDiscoveryPack> devices = new ArrayList<>();

                //Keep searching for more devices
                while(isSearching) {
                    byte[] buffer = new byte[DETECT_BYTES.length];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);

                        //Add if the message received is also detect string as it proves its a RemAL device
                        if(new String(buffer).equals(DETECT_STRING))
                            devices.add(new LanDeviceDiscoveryPack(packet, buffer));
                    } catch(IOException e) {}
                }

                callback.run(devices.toArray(new LanDeviceDiscoveryPack[0]));
            }).start();

            //Schedule the timeout
            new Timer().schedule(new TimerTask() {
                public void run() {
                    isSearching = false;
                    socket.close();
                }
            }, timeout);
        } catch(Exception e) {
            isSearching = false;
            e.printStackTrace();
        }
    }
}
