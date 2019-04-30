package exn.database.remal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation to connect over LAN
 */
public interface IWiFiDevice extends IRemoteDevice {


    default void disconnect() {

    }



    default boolean connect() {
        if(!isConnected()) {
            try {
                Socket socket = new Socket(getWiFiAddress(), getWiFiPort());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                setWiFiSocket(socket);
                setWiFiWriter(writer);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    default boolean sendCommand(String command) {
        try {
            getWiFiWriter().write(command);

            return true;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    int getWiFiPort();
    int setWiFiPort(int port);
    String getWiFiAddress();
    String setWiFiAddress(String address);
    Socket getWiFiSocket();
    void setWiFiSocket(Socket socket);
    BufferedWriter getWiFiWriter();
    void setWiFiWriter(BufferedWriter writer);
}
