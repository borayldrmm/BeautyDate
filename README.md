---

## 🧾 Introduction - Project Overview

---

## 👥 For Users (English)

**BeautyDate** is a modern mobile app designed for beauty salons.  
It helps you manage your customers, appointments, services, staff, and financial records — all in one place, without needing any technical knowledge.

- 📲 **Sign Up & Login:** You can easily register with your email and password, and securely log in.  
- 👤 **Customer Management:** Add customers, track their visit history, and attach notes to their profiles.  
- 📅 **Appointment Scheduling:** Quickly book appointments by selecting a date, time, service, and staff.  
- 🧑‍💼 **Staff Tracking:** See which employee provides which service and how many appointments they have.  
- 💇‍♀️ **Services:** List your offered services (like manicure, massage, laser, etc.) and set their prices.  
- 📊 **Statistics:** Understand your busiest days, most popular services, and overall performance.  
- 💸 **Finance Management:** Monitor income and expenses to better understand your business performance.  
- ❌ **Delete Account:** Users can remove their account completely from the profile settings.  

---

## 👨‍💻 For Developers (English)

**BeautyDate** is a multi-tenant, Firebase-powered mobile appointment management app for beauty salons. Built with Jetpack Compose, MVVM, and SOLID principles, it allows salon businesses to securely manage clients, appointments, services, employees, and financial records in real-time.

- ✅ **Authentication:** Firebase Auth (Email/Password) with validation and reset flows.  
- 🔍 **Client Records:** CRUD operations and Firestore integration per businessId.  
- 🕡 **Appointment System:** Create, categorize (upcoming/completed/canceled), and visualize with filters.  
- 🧱 **Service & Employee Modules:** Modular management with assignment relationships.  
- 💳 **Finance & Expense Tracking:** Track income, expenses, transactions, and generate statistics.  
- 🌐 **Language Support:** Bilingual (English/Turkish) via `strings.xml` and dynamic LocaleHelper.  
- 💪 **Secure Firestore Rules:** Isolated multi-tenant data per authenticated user.  
- ❌ **Account Deletion:** Fully integrated profile/account deletion module.  

> The app follows scalable patterns with Repository, ViewModel, UI State separation, and fully reactive flows.

---

## 🧾 Giriş - Proje Özeti

---

## 👥 Kullanıcılar İçin (Türkçe)

**BeautyDate**, güzellik salonları için geliştirilmiş modern bir mobil uygulamadır.  
Tek bir yerden müşterilerinizi, randevularınızı, hizmetlerinizi, çalışanlarınızı ve finansal kayıtlarınızı teknik bilgi gerekmeden kolayca yönetmenizi sağlar.

- 📲 **Kayıt Ol & Giriş Yap:** E-posta ve şifre ile kolayca kayıt olabilir, güvenli şekilde giriş yapabilirsiniz.  
- 👤 **Müşteri Yönetimi:** Müşteri ekleyebilir, geçmiş ziyaretlerini takip edebilir ve profil notları ekleyebilirsiniz.  
- 📅 **Randevu Planlama:** Tarih, saat, hizmet ve personel seçerek hızlıca randevu oluşturabilirsiniz.  
- 🧑‍💼 **Çalışan Takibi:** Hangi çalışan hangi hizmeti veriyor ve kaç randevusu var görebilirsiniz.  
- 💇‍♀️ **Hizmetler:** Manikür, masaj, lazer gibi hizmetleri listeleyebilir, fiyatlarını belirleyebilirsiniz.  
- 📊 **İstatistik:** En yoğun günleri, en çok tercih edilen hizmetleri ve genel performansı analiz edebilirsiniz.  
- 💸 **Finans Yönetimi:** Gelir ve giderleri takip ederek işletme performansınızı ölçebilirsiniz.  
- ❌ **Hesap Silme:** Kullanıcılar, profil ayarlarından hesaplarını tamamen silebilir.

---

## 👨‍💻 Geliştiriciler İçin (Türkçe)

**BeautyDate**, güzellik salonlarına yönelik olarak geliştirilen, Firebase destekli çok işletmeli bir mobil randevu yönetim uygulamasıdır. Jetpack Compose, MVVM ve SOLID prensiplerine göre inşa edilmiştir ve gerçek zamanlı olarak müşteri, randevu, hizmet, çalışan ve finansal verilerin güvenli şekilde yönetilmesini sağlar.

- ✅ **Kimlik Doğrulama:** Firebase Auth (E-posta/Şifre) ile kayıt, giriş, doğrulama ve şifre sıfırlama işlemleri.  
- 🔍 **Müşteri Kayıtları:** CRUD işlemleri ve businessId bazlı Firestore entegrasyonu.  
- 🕡 **Randevu Sistemi:** Randevular oluşturulabilir, tamamlanmış/iptal edilmiş/güncel olarak kategorize edilir ve filtrelenebilir.  
- 🧱 **Hizmet & Personel Modülü:** Hizmet ve çalışanlar modüler şekilde atanabilir ve yönetilebilir.  
- 💳 **Finans & Gider Takibi:** Gelir, gider, işlem verileri izlenebilir, analiz ve grafiklerle desteklenir.  
- 🌐 **Dil Desteği:** strings.xml ve dinamik LocaleHelper ile İngilizce/Türkçe dil desteği.  
- 💪 **Güvenlik Kuralları:** Firebase Firestore'da çok işletmeli kullanıcı izolasyonu sağlayan güvenlik kuralları.  
- ❌ **Hesap Silme:** Profil ayarlarından tam entegre hesap silme özelliği.

> Uygulama; Repository, ViewModel, UI State ayrımı ve reaktif veri akışı gibi ölçeklenebilir yapı kalıplarını takip eder.

---

## 🚀 Features

- 🔐 Secure email/password authentication  
- 🗂️ Multi-tenant Firestore structure (per business)  
- 📋 Customer registration & history tracking  
- 📆 Appointment creation with time, service, and employee  
- 🧑‍💼 Employee management module  
- 💇 Service listing and categorization (e.g. manicure, laser, massage)  
- 📊 Visual statistics dashboard  
- 💸 Finance module with expense and transaction management  
- 📁 User-specific notes and appointment filters  
- 🧾 CRUD support for all entities (customers, services, employees, notes, etc.)  
- 🌐 Full language toggle (EN / TR)  
- ❌ Account deletion from profile settings  

> Designed for both small and large-scale beauty salons, supporting unlimited staff, customers, and appointments.

---

## 🛠️ Tech Stack

| Category             | Tools & Frameworks                                                               |
| -------------------- | -------------------------------------------------------------------------------- |
| Programming Language | Kotlin                                                                           |
| UI Toolkit           | Jetpack Compose, Material 3                                                      |
| Architecture         | MVVM (Model-View-ViewModel), SOLID principles                                    |
| Dependency Injection | Hilt (Dagger)                                                                    |
| Navigation           | Jetpack Navigation Component                                                     |
| State Management     | UI State + ViewModel State separation                                            |
| Local Storage        | Room (SQLite ORM), DataStore                                                     |
| Cloud Backend        | Firebase (Firestore, Authentication)                                             |
| Security Rules       | Firebase Firestore Rules (multi-tenant, businessId-based)                        |
| Asynchronous Ops     | Kotlin Coroutines                                                                |
| Image Handling       | Coil (Compose Image Loading), uCrop (optional)                                   |
| Network Utils        | Custom NetworkMonitor.kt for connection state                                    |
| Validation           | Regex-based custom validators (PasswordValidator, ValidationUtils)               |
| Language Support     | Multi-language support (strings.xml + strings-tr.xml) using dynamic LocaleHelper |
| Misc Utilities       | Debouncer, RepositoryCache, ToastUtils, AuthUtil, etc.                           |

---

## 📸 Screenshots

<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/592b00c4-cfe2-4a23-911f-34aad3e8cf8a" />
<img width="1920" height="1080" alt="2" src="https://github.com/user-attachments/assets/77fdb643-cc7d-44ac-b958-7abb4c428f90" />
<img width="1920" height="1080" alt="3" src="https://github.com/user-attachments/assets/ebb61a53-cdab-4f0f-b030-27b546d685c5" />
<img width="1920" height="1080" alt="4" src="https://github.com/user-attachments/assets/adb547b1-a3d3-4ec2-863c-7eb3739fb5a4" />
<img width="1920" height="1080" alt="5" src="https://github.com/user-attachments/assets/ccbe8e9e-c380-4e24-9072-e8b2fa0b0bb3" />
<img width="1920" height="1080" alt="6" src="https://github.com/user-attachments/assets/ffb7c88f-7d7d-4921-9445-1acff1d0b9da" />
<img width="1920" height="1080" alt="7" src="https://github.com/user-attachments/assets/df0d1c5e-628b-4e60-885d-ef3cbcfacf36" />

---

## 📬 Contact

If you’d like to connect or have questions about the project:

**Bora Yıldırım**  
[🔗 LinkedIn](https://www.linkedin.com/in/borayldrmm/)  
[📨 Email](mailto:borayldrm@hotmail.com)

---

## 🏁 Final Note

This project was built as part of my journey through Android development using Kotlin.  
It reflects my focus on scalable architecture, real-world usability, and modern design principles.

**BeautyDate** is not just a functional application — it's a complete salon management tool tailored for real business scenarios.

Feel free to clone, review, or contribute. Your feedback is welcome!




<p align="center">
<img width="600" height="600" alt="android" src="https://github.com/user-attachments/assets/2580e1c0-e529-44e5-9527-0d39622a944d" />
<img width="225" height="225" alt="androidstudio" src="https://github.com/user-attachments/assets/5814cb0b-c23d-41c5-9669-4e1b50942010" />
<img width="512" height="512" alt="Room" src="https://github.com/user-attachments/assets/c8b96971-4656-45d5-bcfe-31a9f3112084" />
<img width="920" height="919" alt="authentication" src="https://github.com/user-attachments/assets/a617cf09-4f93-4697-8c1c-b12df6f9fcc1" />
<img width="1400" height="698" alt="coroutine" src="https://github.com/user-attachments/assets/122caa9d-410b-47c9-a3f9-51b379fcf649" />
<img width="640" height="640" alt="firebase" src="https://github.com/user-attachments/assets/2275f8fc-1ab8-44b2-9ce7-80bf83febe89" />
![firebasestorage](https://github.com/user-attachments/assets/d319d689-ff2f-4519-80a2-34d334f23510)
<img width="1024" height="1024" alt="jetpack_compose" src="https://github.com/user-attachments/assets/26fbaf47-59ab-4832-988f-3602be2d7006" />
<img width="458" height="417" alt="json" src="https://github.com/user-attachments/assets/906227a6-6c7d-437d-9133-cc4fff17e6d0" />
<img width="600" height="600" alt="kotlin" src="https://github.com/user-attachments/assets/41747e93-5cc8-4831-886e-fefd96832c65" />
<img width="512" height="512" alt="MVVM" src="https://github.com/user-attachments/assets/c838a38b-749d-4106-95f5-256cb943d70a" />
<img width="752" height="503" alt="Hilt" src="https://github.com/user-attachments/assets/af5ed77a-c07e-4c1c-b79f-5edd3a18dd8e" />
<img width="416" height="159" alt="Coil" src="https://github.com/user-attachments/assets/b69c4ac9-7998-4339-83ab-21e54e3c7312" />
</p>
