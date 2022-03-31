package com.web.service;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebInterface {
    /**
     * Only Admin
     */
    String addAppointment(String appointmentID, String appointmentType, int bookingCapacity) ;

    String removeAppointment(String appointmentID, String appointmentType) ;

    String listAppointmentAvailability(String appointmentType) ;

    /**
     * Both Admin and Patient
     */
    String bookAppointment(String patientID, String appointmentID, String appointmentType) ;

    String getAppointmentSchedule(String patientID) ;

    String cancelAppointment(String patientID, String appointmentID, String appointmentType) ;

    String swapAppointment(String patientID, String newAppointmentID, String newAppointmentType, String oldAppointmentID, String oldAppointmentType);
}
