export interface Department {
  id: string;
  name: string;
  capacity: number;
  active: boolean;
  description?: string;
  defaultConsultationMinutes: number;
}

export interface Doctor {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  departmentId: string;
  departmentName: string;
  specialization: string;
  qualification?: string;
  experienceYears: number;
  consultationMinutes: number;
  rating: number;
  active: boolean;
}

export interface Patient {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  age: number;
  gender: string;
  bloodGroup?: string;
  address?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  allergies: string[];
  chronicConditions: string[];
  currentMedications: string[];
}

export interface Appointment {
  id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  departmentName: string;
  scheduledAt: string;
  durationMinutes: number;
  status: string;
  reason?: string;
  tokenNumber?: string;
}

export interface DecisionFactor {
  name: string;
  points: number;
  reason: string;
}

export interface DecisionExplanation {
  subject: string;
  factors: DecisionFactor[];
}

export interface QueueEntryResponse {
  id: string;
  patientId: string;
  patientName: string;
  doctorId?: string;
  tokenNumber: string;
  status: string;
  emergency: boolean;
  clinicalRisk: number;
  waitingMinutes: number;
  position: number;
  priorityScore: number;
  estimatedWaitMinutes: number;
  explanation: DecisionExplanation;
}

export interface DepartmentStatus {
  departmentId: string;
  departmentName: string;
  queueLength: number;
  availableDoctors: number;
  avgWaitingMinutes: number;
  loadStatus: "LOW" | "MODERATE" | "HIGH" | "CRITICAL";
}

export interface HospitalMetrics {
  patientsToday: number;
  appointmentsToday: number;
  emergencyCasesToday: number;
  avgWaitingMinutes: number;
  bedOccupancyPercent: number;
  icuOccupancyPercent: number;
  patientExperienceIndex: number;
  revenueToday: number;
  pharmacyStockAlerts: number;
}

export interface Ward {
  id: string;
  name: string;
  icu: boolean;
  isolation: boolean;
  genderPolicy?: string;
}

export interface Bed {
  id: string;
  bedNumber: string;
  status: "AVAILABLE" | "RESERVED" | "OCCUPIED" | "CLEANING" | "INSPECTION";
  currentPatientId?: string;
}

export interface Medicine {
  id: string;
  name: string;
  genericName?: string;
  unit: string;
  pricePerUnit: number;
  reorderThreshold: number;
  totalDispensed: number;
}

export interface MedicineDemandReport {
  medicineId: string;
  medicineName: string;
  currentStock: number;
  averageDailyUsage: number;
  estimatedRemainingDays?: number;
  status: "OK" | "REORDER_RECOMMENDED" | "URGENT_REORDER";
  explanation: string;
}

export interface LabTestCatalog {
  id: string;
  name: string;
  price: number;
  unit?: string;
  normalRangeLow?: number;
  normalRangeHigh?: number;
  criticalLow?: number;
  criticalHigh?: number;
}

export interface PersonRef {
  id: string;
  fullName: string;
}

export interface LabOrder {
  id: string;
  patient: PersonRef;
  orderingDoctor: PersonRef;
  test: LabTestCatalog;
  status: string;
  sampleId?: string;
  resultValue?: number;
  resultNotes?: string;
  critical: boolean;
}

export interface PrescriptionItem {
  id: string;
  medicineId: string;
  medicineName: string;
  dosageInstructions: string;
  quantity: number;
  dispensed: boolean;
}

export interface PendingPrescriptionItem {
  id: string;
  patientName: string;
  medicineId: string;
  medicineName: string;
  dosageInstructions: string;
  quantity: number;
}

export interface Prescription {
  id: string;
  patientId: string;
  doctorId: string;
  doctorName: string;
  items: PrescriptionItem[];
  createdAt: string;
}

export interface Invoice {
  id: string;
  patientId: string;
  status: string;
  items: { description: string; category: string; unitPrice: number; quantity: number }[];
  payments: { amount: number; method: string }[];
}

export interface Notification {
  id: string;
  message: string;
  priority: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  read: boolean;
  createdAt: string;
}
