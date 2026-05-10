package com.duoc.backend;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
}