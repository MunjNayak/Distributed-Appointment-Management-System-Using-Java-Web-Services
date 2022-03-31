package Server;

import Info.AppointmentInfo;
import Logger.Logger;
import com.web.service.implementation.AppointmentManagement;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
public class ServerInstance {

    private String serverID;
    private String serverName;
    private int serverUdpPort;
    private String serverEndPoint;

    public ServerInstance(String serverID, String args[]) throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "MTL":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_MONTREAL;
                serverUdpPort = AppointmentManagement.Montreal_Server_Port;
                serverEndPoint = "http://localhost:8080/montreal";
                break;
            case "QUE":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_QUEBEC;
                serverUdpPort = AppointmentManagement.Quebec_Server_Port;
                serverEndPoint = "http://localhost:8080/quebce";
                break;
            case "SHE":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_SHERBROOK;
                serverUdpPort = AppointmentManagement.Sherbrooke_Server_Port;
                serverEndPoint = "http://localhost:8080/sherbrook";
                break;
        }
        try {
            System.out.println(serverName + " Server Started");
            Logger.serverLog(serverID, " Server Started");
            AppointmentManagement service = new AppointmentManagement(serverID, serverName);

            Endpoint endpoint = Endpoint.publish(serverEndPoint,service);
            System.out.println(serverName+" Server is Up and Running");
            Logger.serverLog(serverID, " Server is Up and Running");
            Runnable task = () -> {
                listenForRequest(service, serverUdpPort,serverName,serverID);
            };
            Thread t = new Thread(task);
            t.start();

        }catch (Exception e)
        {
            e.printStackTrace(System.out);
            Logger.serverLog(serverID, " Exception: "+e);
        }
    }

        private static void listenForRequest(AppointmentManagement obj, int serverUdpPort, String serverName, String serverID) {
            DatagramSocket aSocket = null;
            String sendingResult = "";
            try {
                aSocket = new DatagramSocket(serverUdpPort);
                byte[] buffer = new byte[1000];
                System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
                Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String sentence = new String(request.getData(), 0,
                            request.getLength());
                    String[] parts = sentence.split(";");
                    String method = parts[0];
                    String patientID = parts[1];
                    String appointmentType = parts[2];
                    String appointmentID = parts[3];
                    if (method.equalsIgnoreCase("removeAppointment")) {
                        Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " eventType: " + appointmentType + " ", " ...");
                        String result = obj.removeAppointmentUDP(appointmentID, appointmentType, patientID);
                        sendingResult = result + ";";
                    } else if (method.equalsIgnoreCase("listAppointmentAvailability")) {
                        Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentType: " + appointmentType + " ", " ...");
                        String result = obj.listAppointmentAvailabilityUDP(appointmentType);
                        sendingResult = result + ";";
                    } else if (method.equalsIgnoreCase("bookAppointment")) {
                        Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", " ...");
                        String result = obj.bookAppointment(patientID, appointmentID, appointmentType);
                        sendingResult = result + ";";
                    } else if (method.equalsIgnoreCase("cancelAppointment")) {
                        Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", " ...");
                        String result = obj.cancelAppointment(patientID, appointmentID, appointmentType);
                        sendingResult = result + ";";
                    }
                    byte[] sendData = sendingResult.getBytes();
                    DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                            request.getPort());
                    aSocket.send(reply);
                    Logger.serverLog(serverID, patientID, " UDP reply sent " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", sendingResult);
                }
            } catch (SocketException e) {
                System.out.println("SocketException: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            } finally {
                if (aSocket != null)
                    aSocket.close();
            }
        }
}
