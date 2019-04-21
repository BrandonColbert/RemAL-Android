package exn.database.remal.devices;

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
    public interface LanDeviceCallback {
        void run(InetAddress[] devices);
    }

    private static final byte[] DETECT_BYTES = "REMAL_DETECT".getBytes();

    public RemoteLanDevice(String name) {
        super(name);
    }

    /**
     * Searches for devices on the network with the port for RemAL
     * @param callback Function to be called with the list of devices
     */
    public void findDevices(LanDeviceCallback callback) {
        final int port = getWiFiPort();

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

                        if(address != null) {
                            try {
                                socket.send(new DatagramPacket(DETECT_BYTES, DETECT_BYTES.length, address, port));
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            Thread findThread = new Thread(() -> {
                List<InetAddress> devices = new ArrayList<>();

                while(!Thread.currentThread().isInterrupted()) {
                    byte[] buffer = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);
                        devices.add(packet.getAddress());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                socket.close();
                callback.run(devices.toArray(new InetAddress[0]));
            });

            findThread.start();

            new Timer().schedule(new TimerTask() {
                public void run() {
                    findThread.interrupt();
                }
            }, 1000);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
