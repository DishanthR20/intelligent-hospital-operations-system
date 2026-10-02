package com.medisphere.appointment;

import com.medisphere.appointment.dto.SlotSuggestion;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorSchedule;
import com.medisphere.doctor.DoctorScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Smart Appointment Slot Optimizer (spec section 11). A deliberately simple,
 * fully explainable Java heuristic: it walks each of the doctor's configured
 * availability windows in consultation-length steps, skips anything that
 * collides with an existing active appointment, and estimates the wait for
 * each free slot from how many patients are already booked ahead of it that
 * day. No black box — every number in the result can be traced back to a
 * count of existing appointments.
 */
@Component
public class SlotOptimizer {

    private static final int MAX_SUGGESTIONS = 5;

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public SlotOptimizer(DoctorScheduleRepository scheduleRepository, AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<SlotSuggestion> suggest(Doctor doctor, LocalDate date) {
        List<DoctorSchedule> windows = scheduleRepository.findByDoctor_IdAndDayOfWeek(doctor.getId(), date.getDayOfWeek());
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Appointment> existing = appointmentRepository.findActiveForDoctorOnDay(doctor.getId(), dayStart, dayEnd);
        existing.sort(Comparator.comparing(Appointment::getScheduledAt));

        int consultMinutes = doctor.getConsultationMinutes();
        List<SlotSuggestion> suggestions = new ArrayList<>();

        for (DoctorSchedule window : windows) {
            LocalDateTime slot = date.atTime(window.getStartTime());
            LocalDateTime windowEnd = date.atTime(window.getEndTime());

            while (!slot.plusMinutes(consultMinutes).isAfter(windowEnd)) {
                final LocalDateTime currentSlot = slot;
                LocalDateTime slotEnd = currentSlot.plusMinutes(consultMinutes);
                boolean collides = existing.stream().anyMatch(a ->
                        currentSlot.isBefore(a.getScheduledAt().plusMinutes(a.getDurationMinutes())) && a.getScheduledAt().isBefore(slotEnd));

                if (!collides && currentSlot.isAfter(LocalDateTime.now())) {
                    long patientsAhead = existing.stream().filter(a -> a.getScheduledAt().isBefore(currentSlot)).count();
                    int estimatedWait = (int) (patientsAhead * consultMinutes);
                    String explanation = patientsAhead == 0
                            ? "First available slot in this window; no patients scheduled ahead of it."
                            : patientsAhead + " patient(s) scheduled ahead at ~" + consultMinutes + " min each.";
                    suggestions.add(new SlotSuggestion(slot, estimatedWait, explanation));
                }
                slot = slot.plusMinutes(consultMinutes);
            }
        }

        return suggestions.stream()
                .sorted(Comparator.comparingInt(SlotSuggestion::estimatedWaitMinutes).thenComparing(SlotSuggestion::slot))
                .limit(MAX_SUGGESTIONS)
                .toList();
    }
}
