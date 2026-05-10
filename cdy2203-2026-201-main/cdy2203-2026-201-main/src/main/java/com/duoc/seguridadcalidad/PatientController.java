package com.duoc.seguridadcalidad;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class PatientController {

    @GetMapping("/patients")
    public String listPatients() {
        return "patients";
    }

    @GetMapping("/patients/new")
    public String showCreateForm() {
        return "new_patient";
    }

    @GetMapping("/patients/edit")
    public String showEditForm() {
        return "new_patient";
    }

    @PostMapping("/patients")
    public String savePatient() {
        return "redirect:/patients";
    }

    @PutMapping("/patients/{id}")
    public String updatePatient() {
        return "redirect:/patients";
    }
}
