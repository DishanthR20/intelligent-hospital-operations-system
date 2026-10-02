package com.medisphere.patient;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Role-specific profile for a patient. Allergies, chronic conditions and current
 * medications are stored as normalized collection tables (patient_allergies,
 * patient_chronic_conditions, patient_medications) rather than crammed into one
 * text field, per the platform's data design rules.
 */
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    public enum Gender { MALE, FEMALE, OTHER }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private String bloodGroup;

    @Column(length = 500)
    private String address;

    private String emergencyContactName;
    private String emergencyContactPhone;

    private String insuranceProvider;
    private String insurancePolicyNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy", nullable = false)
    private Set<String> allergies = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_chronic_conditions", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "condition_name", nullable = false)
    private Set<String> chronicConditions = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_current_medications", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "medication", nullable = false)
    private Set<String> currentMedications = new LinkedHashSet<>();

    protected Patient() {
    }

    public Patient(User user, LocalDate dateOfBirth, Gender gender, String bloodGroup) {
        this.user = user;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public Gender getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public Set<String> getAllergies() {
        return allergies;
    }

    public Set<String> getChronicConditions() {
        return chronicConditions;
    }

    public Set<String> getCurrentMedications() {
        return currentMedications;
    }
}
