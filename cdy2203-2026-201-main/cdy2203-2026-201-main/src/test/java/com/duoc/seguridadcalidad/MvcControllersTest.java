package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MvcControllersTest {

    @Test
    void appointmentControllerReturnsExpectedViews() {
        AppointmentController controller = new AppointmentController();

        assertEquals("appointments", controller.listAppointments());
        assertEquals("new_appointment", controller.showCreateForm());
        assertEquals("redirect:/appointments", controller.saveAppointment());
    }

    @Test
    void patientControllerReturnsExpectedViewsAndRedirects() {
        PatientController controller = new PatientController();

        assertEquals("patients", controller.listPatients());
        assertEquals("new_patient", controller.showCreateForm());
        assertEquals("new_patient", controller.showEditForm());
        assertEquals("redirect:/patients", controller.savePatient());
        assertEquals("redirect:/patients", controller.updatePatient());
    }

    @Test
    void petControllerReturnsExpectedViews() {
        PetController controller = new PetController();

        assertEquals("pets", controller.listPets());
        assertEquals("new_pet", controller.showCreateForm());
    }

    @Test
    void homeControllerStoresDefaultAndCustomNameInModel() {
        HomeController controller = new HomeController();
        ConcurrentModel defaultModel = new ConcurrentModel();
        ConcurrentModel customModel = new ConcurrentModel();

        assertEquals("home", controller.home("Seguridad y Calidad en el Desarrollo", defaultModel));
        assertEquals("Seguridad y Calidad en el Desarrollo", defaultModel.getAttribute("name"));

        assertEquals("home", controller.root("Francisca", customModel));
        assertEquals("Francisca", customModel.getAttribute("name"));
    }

    @Test
    void loginControllerReturnsLoginView() {
        LoginController controller = new LoginController();

        assertEquals("login", controller.login());
    }
}
