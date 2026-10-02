package com.medisphere.appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatient_IdOrderByScheduledAtDesc(UUID patientId);

    List<Appointment> findByDoctor_IdOrderByScheduledAtDesc(UUID doctorId);

    /**
     * Candidate appointments that might overlap [start, end): anything for this doctor,
     * not cancelled/no-show, starting within a 4-hour lookback of {@code start} through
     * {@code end}. The 4-hour pad comfortably covers any realistic appointment duration;
     * the caller does the exact overlap check in Java against each candidate's real
     * end time, since that arithmetic isn't portable across every JPA provider/dialect.
     */
    @Query("select a from Appointment a where a.doctor.id = :doctorId " +
           "and a.status not in ('CANCELLED', 'NO_SHOW') " +
           "and a.scheduledAt < :end and a.scheduledAt > :lookback")
    List<Appointment> findCandidateOverlaps(@Param("doctorId") UUID doctorId,
                                             @Param("lookback") LocalDateTime lookback,
                                             @Param("end") LocalDateTime end);

    default List<Appointment> findOverlapping(UUID doctorId, LocalDateTime start, LocalDateTime end) {
        return findCandidateOverlaps(doctorId, start.minusHours(4), end).stream()
                .filter(a -> a.getScheduledAt().isBefore(end) && start.isBefore(a.getEndsAt()))
                .toList();
    }

    @Query("select a from Appointment a where a.doctor.id = :doctorId " +
           "and a.status not in ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "and a.scheduledAt >= :dayStart and a.scheduledAt < :dayEnd order by a.scheduledAt")
    List<Appointment> findActiveForDoctorOnDay(@Param("doctorId") UUID doctorId,
                                                @Param("dayStart") LocalDateTime dayStart,
                                                @Param("dayEnd") LocalDateTime dayEnd);

    @Query("select count(a) from Appointment a where a.doctor.id = :doctorId " +
           "and a.status in ('BOOKED','CONFIRMED','CHECKED_IN','WAITING') " +
           "and a.scheduledAt >= :dayStart and a.scheduledAt < :dayEnd")
    long countActiveForDoctorOnDay(@Param("doctorId") UUID doctorId,
                                    @Param("dayStart") LocalDateTime dayStart,
                                    @Param("dayEnd") LocalDateTime dayEnd);

    long countByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
