# 🏥 Medical Management System - Android App

An Android application built in Java to manage doctor-patient interactions, medical appointments, treatment history, and billing. The system offers a dual-module structure: one for **Patients** and one for **Doctors**, with tailored features for each.

## 🚀 Features

### 👩‍⚕️ Doctor Module
- 🔐 Register/Login using Firebase
- 📋 View & manage **Pending Appointments** (Accept/Reject)
- 📆 See **Today's Appointments**
- 🧾 Update **Patient History** after consultation
- 📝 Add **Prescriptions**, **Diagnosis**, and **Progress Notes**
- 💵 Generate and manage **Patient Bills**

### 🧑 Patient Module
- 🔐 Register/Login using Firebase
- 👤 View and update **Profile**
- 📅 Book & manage **Appointments**
- 📖 View **Treatment History**
- 💰 Access **Bill History**

## 💡 Technologies Used
- 🔧 Android Studio (Java)
- ☁️ Firebase Authentication
- 🔄 Firebase Realtime Database
- 🎨 XML Layouts for UI Design

## 📦 Folder Structure
```
MedicalManagementSystem/
├── app/
│   ├── activities/
│   ├── adapters/
│   ├── models/
│   ├── firebase/
│   └── utils/
```

## 🛠️ Setup Instructions
1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/medical-management-system.git
   ```
2. Open in **Android Studio**
3. Configure Firebase:
   - Add your **google-services.json** file in `app/`
   - Enable Authentication and Realtime Database in Firebase Console
4. Run the app on an emulator or device.

## 🤝 Contribution
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

---

## 📧 Contact
For any questions or feedback, reach out to: `2022ag7999@uaf.edu.pk`
