import { Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import RequireRole from "./components/RequireRole";
import Login from "./pages/Login";
import RoleHome from "./pages/RoleHome";

import CommandCenter from "./pages/admin/CommandCenter";
import Departments from "./pages/admin/Departments";
import AdminDoctors from "./pages/admin/Doctors";
import AdminPatients from "./pages/admin/Patients";
import Staff from "./pages/admin/Staff";
import Beds from "./pages/admin/Beds";
import AdminPharmacy from "./pages/admin/Pharmacy";
import AdminLaboratory from "./pages/admin/Laboratory";
import DecisionReplay from "./pages/admin/DecisionReplay";
import AuditLog from "./pages/admin/AuditLog";
import Configuration from "./pages/admin/Configuration";

import DoctorDashboard from "./pages/doctor/Dashboard";
import DoctorQueue from "./pages/doctor/Queue";
import DoctorAppointments from "./pages/doctor/Appointments";

import NurseQueues from "./pages/nurse/Queues";
import NurseBeds from "./pages/nurse/Beds";

import RegisterPatient from "./pages/reception/RegisterPatient";
import ReceptionAppointments from "./pages/reception/Appointments";
import ReceptionQueues from "./pages/reception/Queues";
import ReceptionBilling from "./pages/reception/Billing";

import PharmacyPrescriptions from "./pages/pharmacy/Prescriptions";
import PharmacyInventory from "./pages/pharmacy/Inventory";

import LabOrders from "./pages/lab/Orders";

import PatientDashboard from "./pages/patient/Dashboard";
import PatientAppointments from "./pages/patient/Appointments";
import PatientRecords from "./pages/patient/Records";
import PatientBills from "./pages/patient/Bills";
import PatientFeedback from "./pages/patient/Feedback";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<Layout />}>
        <Route path="/" element={<RoleHome />} />

        <Route path="/admin" element={<RequireRole roles={["ADMIN"]}><CommandCenter /></RequireRole>} />
        <Route path="/admin/departments" element={<RequireRole roles={["ADMIN"]}><Departments /></RequireRole>} />
        <Route path="/admin/doctors" element={<RequireRole roles={["ADMIN"]}><AdminDoctors /></RequireRole>} />
        <Route path="/admin/patients" element={<RequireRole roles={["ADMIN"]}><AdminPatients /></RequireRole>} />
        <Route path="/admin/staff" element={<RequireRole roles={["ADMIN"]}><Staff /></RequireRole>} />
        <Route path="/admin/beds" element={<RequireRole roles={["ADMIN"]}><Beds /></RequireRole>} />
        <Route path="/admin/pharmacy" element={<RequireRole roles={["ADMIN"]}><AdminPharmacy /></RequireRole>} />
        <Route path="/admin/laboratory" element={<RequireRole roles={["ADMIN"]}><AdminLaboratory /></RequireRole>} />
        <Route path="/admin/decisions" element={<RequireRole roles={["ADMIN"]}><DecisionReplay /></RequireRole>} />
        <Route path="/admin/audit" element={<RequireRole roles={["ADMIN"]}><AuditLog /></RequireRole>} />
        <Route path="/admin/configuration" element={<RequireRole roles={["ADMIN"]}><Configuration /></RequireRole>} />

        <Route path="/doctor" element={<RequireRole roles={["DOCTOR"]}><DoctorDashboard /></RequireRole>} />
        <Route path="/doctor/queue" element={<RequireRole roles={["DOCTOR"]}><DoctorQueue /></RequireRole>} />
        <Route path="/doctor/appointments" element={<RequireRole roles={["DOCTOR"]}><DoctorAppointments /></RequireRole>} />

        <Route path="/nurse" element={<RequireRole roles={["NURSE"]}><NurseQueues /></RequireRole>} />
        <Route path="/nurse/beds" element={<RequireRole roles={["NURSE"]}><NurseBeds /></RequireRole>} />

        <Route path="/reception" element={<RequireRole roles={["RECEPTIONIST"]}><RegisterPatient /></RequireRole>} />
        <Route path="/reception/appointments" element={<RequireRole roles={["RECEPTIONIST"]}><ReceptionAppointments /></RequireRole>} />
        <Route path="/reception/queues" element={<RequireRole roles={["RECEPTIONIST"]}><ReceptionQueues /></RequireRole>} />
        <Route path="/reception/billing" element={<RequireRole roles={["RECEPTIONIST"]}><ReceptionBilling /></RequireRole>} />

        <Route path="/pharmacy" element={<RequireRole roles={["PHARMACIST"]}><PharmacyPrescriptions /></RequireRole>} />
        <Route path="/pharmacy/inventory" element={<RequireRole roles={["PHARMACIST"]}><PharmacyInventory /></RequireRole>} />

        <Route path="/lab" element={<RequireRole roles={["LAB_TECHNICIAN"]}><LabOrders /></RequireRole>} />

        <Route path="/patient" element={<RequireRole roles={["PATIENT"]}><PatientDashboard /></RequireRole>} />
        <Route path="/patient/appointments" element={<RequireRole roles={["PATIENT"]}><PatientAppointments /></RequireRole>} />
        <Route path="/patient/records" element={<RequireRole roles={["PATIENT"]}><PatientRecords /></RequireRole>} />
        <Route path="/patient/bills" element={<RequireRole roles={["PATIENT"]}><PatientBills /></RequireRole>} />
        <Route path="/patient/feedback" element={<RequireRole roles={["PATIENT"]}><PatientFeedback /></RequireRole>} />
      </Route>
    </Routes>
  );
}
