package com.web.client;
import Info.AppointmentInfo;

import Logger.Logger;
import com.web.service.WebInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Scanner;
public class Client {
    public static final int USER_TYPE_PATIENT = 1;
    public static final int USER_TYPE_ADMIN = 2;
    public static final int PATIENT_BOOK_APPOINTMENT = 1;
    public static final int PATIENT_GET_APPOINTMENT_SCHEDULE = 2;
    public static final int PATIENT_CANCEL_APPOINTMENT = 3;
    public static final int PATIENT_SWAP_APPOINTMENT = 4;
    public static final int PATIENT_LOGOUT = 5;
    public static final int ADMIN_ADD_APPOINTMENT = 1;
    public static final int ADMIN_REMOVE_APPOINTMENT = 2;
    public static final int ADMIN_LIST_APPOINTMENT_AVAILABILITY = 3;
    public static final int ADMIN_BOOK_APPOINTMENT = 4;
    public static final int ADMIN_GET_APPOINTMENT_SCHEDULE = 5;
    public static final int ADMIN_CANCEL_APPOINTMENT = 6;
    public static final int ADMIN_SWAP_APPOINTMENT = 7;
    public static final int ADMIN_LOGOUT = 8;

    private static WebInterface obj ;

    private static Service montrealService;
    private static Service quebecService;
    private static Service sherbrookService;
    static Scanner input;

    public static void main(String args[]) throws Exception {
        URL montrealURL = new URL("http://localhost:8080/montreal?wsdl");
        QName montrealQName = new QName("http://implementation.service.web.com/","AppointmentManagementService");
        montrealService = Service.create(montrealURL,montrealQName);

        URL quebecURL = new URL("http://localhost:8080/quebce?wsdl");
        QName quebecQName = new QName("http://implementation.service.web.com/","AppointmentManagementService");
        quebecService = Service.create(quebecURL,quebecQName);

        URL sherbrookURL = new URL("http://localhost:8080/sherbrook?wsdl");
        QName sherbrookQName = new QName("http://implementation.service.web.com/","AppointmentManagementService");
        sherbrookService = Service.create(sherbrookURL,sherbrookQName);
        init();
    }

    public static void init() throws Exception {
        input = new Scanner(System.in);
        String userID;
        System.out.println("Please Enter your UserID: ");
        userID = input.next().trim().toUpperCase();
        Logger.patientLog(userID, " login attempt");
        switch (checkUserType(userID)) {
            case USER_TYPE_PATIENT:
                try {
                    System.out.println("Patient Login successful (" + userID + ")");
                    Logger.patientLog(userID, " Patient Login successful");
                    patient(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case USER_TYPE_ADMIN:
                try {
                    System.out.println("Admin Login successful (" + userID + ")");
                    Logger.patientLog(userID, " Admin Login successful");
                    admin(userID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("!!UserID is not in correct format");
                Logger.patientLog(userID, " UserID is not in correct format");
                Logger.deleteALogFile(userID);
                init();
        }
    }
    private static String getServerID(String userID) {
        String hos = userID.substring(0, 3);
        if (hos.equalsIgnoreCase("MTL")) {
            obj = montrealService.getPort(WebInterface.class);
            return hos;
        } else if (hos.equalsIgnoreCase("SHE")) {
            obj = sherbrookService.getPort(WebInterface.class);
            return hos;
        } else if (hos.equalsIgnoreCase("QUE")) {
            obj = quebecService.getPort(WebInterface.class);
            return hos;
        }
        return "1";
    }

    private static int checkUserType(String userID) {
        if (userID.length() == 8) {
            if (userID.substring(0, 3).equalsIgnoreCase("MTL") ||
                    userID.substring(0, 3).equalsIgnoreCase("QUE") ||
                    userID.substring(0, 3).equalsIgnoreCase("SHE")) {
                if (userID.substring(3, 4).equalsIgnoreCase("P")) {
                    return USER_TYPE_PATIENT;
                } else if (userID.substring(3, 4).equalsIgnoreCase("A")) {
                    return USER_TYPE_ADMIN;
                }
            }
        }
        return 0;
    }

    private static void patient(String patientID) throws Exception {
        String serverID = getServerID(patientID);
        if (serverID.equals("1")) {
            init();
        }

        boolean repeat = true;
        printMenu(USER_TYPE_PATIENT);
        int menuSelection = input.nextInt();
        String appointmentType;
        String appointmentID;
        String serverResponse;
        switch (menuSelection) {
            case PATIENT_BOOK_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(patientID, " attempting to bookAppointment");
                serverResponse = obj.bookAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case PATIENT_GET_APPOINTMENT_SCHEDULE:
                Logger.patientLog(patientID, " attempting to getAppointmentSchedule");
                serverResponse = obj.getAppointmentSchedule(patientID);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " null ", serverResponse);
                break;
            case PATIENT_CANCEL_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(patientID, " attempting to cancelAppointment");
                serverResponse = obj.cancelAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case PATIENT_SWAP_APPOINTMENT:
                System.out.println("Please Enter the OLD appointment to be replaced");
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                System.out.println("Please Enter the NEW appointment to be replaced");
                String newAppointmentType = promptForAppointmentType();
                String newAppointmentID = promptForAppointmentID();
                Logger.patientLog(patientID, " attempting to swapAppointment");
                serverResponse = obj.swapAppointment(patientID, newAppointmentID, newAppointmentType, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " swapAppointment", " oldAppointmentID: " + appointmentID + " oldAppointmentType: " + appointmentType + " newAppointmentID: " + newAppointmentID + " newAppointmentType: " + newAppointmentType + " ", serverResponse);
                break;

            case PATIENT_LOGOUT:
                repeat = false;
                Logger.patientLog(patientID, " attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            patient(patientID);
        }
    }


    private static void admin(String adminID) throws Exception {

        String serverID = getServerID(adminID);
        if (serverID.equals("1")) {
            init();
        }
        //ServerObjectInterface servant = ServerObjectInterfaceHelper.narrow(ncRef.resolve_str(serverID));
        boolean repeat = true;
        printMenu(USER_TYPE_ADMIN);
        String patientID;
        String appointmentType;
        String appointmentID;
        String serverResponse;
        int capacity;
        int menuSelection = input.nextInt();
        switch (menuSelection) {
            case ADMIN_ADD_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                capacity = promptForCapacity();
                Logger.patientLog(adminID, " attempting to addAppointment");
                serverResponse = obj.addAppointment(appointmentID, appointmentType, capacity);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " addAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " appointmentCapacity: " + capacity + " ", serverResponse);
                break;
            case ADMIN_REMOVE_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to removeEvent");
                serverResponse = obj.removeAppointment(appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " removeAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_LIST_APPOINTMENT_AVAILABILITY:
                appointmentType = promptForAppointmentType();
                Logger.patientLog(adminID, " attempting to listAppointmentAvailability");
                serverResponse = obj.listAppointmentAvailability(appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " listAppointmentAvailability", " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_BOOK_APPOINTMENT:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to bookAppointment");
                serverResponse = obj.bookAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " bookAppointment", " patientID: " + patientID + " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_GET_APPOINTMENT_SCHEDULE:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                Logger.patientLog(adminID, " attempting to getAppointmentSchedule");
                serverResponse = obj.getAppointmentSchedule(patientID);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " getAppointmentSchedule", " patientID: " + patientID + " ", serverResponse);
                break;
            case ADMIN_CANCEL_APPOINTMENT:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to cancelAppointment");
                serverResponse = obj.cancelAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " cancelAppointment", " patientID: " + patientID + " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_SWAP_APPOINTMENT:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                System.out.println("Please Enter the OLD appointment to be replaced");
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                System.out.println("Please Enter the NEW appointment to be replaced");
                String newAppointmentType = promptForAppointmentType();
                String newAppointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to swapAppointment");
                serverResponse = obj.swapAppointment(patientID, newAppointmentID, newAppointmentType, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " swapAppointment", " oldAppointmentID: " + appointmentID + " oldAppointmentType: " + appointmentType + " newAppointmentID: " + newAppointmentID + " newAppointmentType: " + newAppointmentType + " ", serverResponse);
                break;

            case ADMIN_LOGOUT:
                repeat = false;
                Logger.patientLog(adminID, "attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            admin(adminID);
        }
    }

    private static String askForPatientIDFromAdmin(String hos) {
        System.out.println("Please enter a patientID(Within " + hos + " Server):");
        String userID = input.next().trim().toUpperCase();
        if (checkUserType(userID) != USER_TYPE_PATIENT || !userID.substring(0, 3).equals(hos)) {
            return askForPatientIDFromAdmin(hos);
        } else {
            return userID;
        }
    }

    private static void printMenu(int userType) {
        System.out.println("*************************************");
        System.out.println("Please choose an option below:");
        if (userType == USER_TYPE_PATIENT) {
            System.out.println("1.Book Appointment");
            System.out.println("2.Get Appointment Schedule");
            System.out.println("3.Cancel Appointment");
            System.out.println("4.Swap Appointment");
            System.out.println("5.Logout");
        } else if (userType == USER_TYPE_ADMIN) {
            System.out.println("1.Add Appointment");
            System.out.println("2.Remove Appointment");
            System.out.println("3.List Appointment Availability");
            System.out.println("4.Book Appointment");
            System.out.println("5.Get Appointment Schedule");
            System.out.println("6.Cancel Appointment");
            System.out.println("7.Swap Appointment");
            System.out.println("8.Logout");

        }
    }

    private static String promptForAppointmentType() {
        System.out.println("*************************************");
        System.out.println("Please choose an appointmentType below:");
        System.out.println("1.Physician");
        System.out.println("2.Surgeon");
        System.out.println("3.Dental");
        switch (input.nextInt()) {
            case 1:
                return AppointmentInfo.PHYSICIAN;
            case 2:
                return AppointmentInfo.SURGEON;
            case 3:
                return AppointmentInfo.DENTAL;
        }
        return promptForAppointmentType();
    }

    private static String promptForAppointmentID() {
        System.out.println("*************************************");
        System.out.println("Please enter the AppointmentID (e.g MTLM190120)");
        String appointmentID = input.next().trim().toUpperCase();
        if (appointmentID.length() == 10) {
            if (appointmentID.substring(0, 3).equalsIgnoreCase("MTL") ||
                    appointmentID.substring(0, 3).equalsIgnoreCase("SHE") ||
                    appointmentID.substring(0, 3).equalsIgnoreCase("QUE")) {
                if (appointmentID.substring(3, 4).equalsIgnoreCase("M") ||
                        appointmentID.substring(3, 4).equalsIgnoreCase("A") ||
                        appointmentID.substring(3, 4).equalsIgnoreCase("E")) {
                    return appointmentID;
                }
            }
        }
        return promptForAppointmentID();
    }

    private static int promptForCapacity() {
        System.out.println("*************************************");
        System.out.println("Please enter the booking capacity:");
        return input.nextInt();
    }
}


