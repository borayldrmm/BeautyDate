<img width="1000" height="1000" alt="logo_png" src="https://github.com/user-attachments/assets/51bbc41f-3796-4654-808d-fcf2c851ffb3" />


# 💄 BeautyDate - Güzellik Salonu Yönetim Uygulaması

**BeautyDate**, güzellik salonlarının müşteri, randevu, hizmet, çalışan, finans ve analiz süreçlerini uçtan uca dijital ortamda yönetmesini sağlayan modern ve ölçeklenebilir bir mobil uygulamadır. Jetpack Compose, MVVM mimarisi, Firebase altyapısı ve Hilt kullanılarak geliştirilmiştir.

---

## 👥 Kullanıcılar İçin

### Sign Up & Login  
📧 E-posta ve şifre ile hızlı ve güvenli şekilde kayıt olabilir, hesabınıza giriş yapabilirsiniz.

### Customer Management  
👩‍💼 Müşteri bilgilerini sisteme kaydedebilir, geçmiş randevularını görebilir ve özel notlar ekleyebilirsiniz.

### Appointment Scheduling  
📆 Hizmet türü, saat ve çalışan seçerek kolayca randevu oluşturabilirsiniz.

### Real-time Filtering  
🔎 Randevularınızı “Tamamlandı”, “İptal Edildi”, “Gelecek” gibi filtrelerle görüntüleyebilirsiniz.

### Financial Tracking  
💰 Yapılan işlemlerden elde edilen kazançları ve giderlerinizi analiz edebilirsiniz.

### Theme & Language  
🌓 Tema (Açık/Karanlık) ve dil (Türkçe/İngilizce) tercihleriniz sistem tarafından otomatik olarak kaydedilir.

### Account Deletion  
❌ Hesabınızı kalıcı olarak silebilir, verilerinizi sistemden kaldırabilirsiniz.

---

## 👨‍💻 Geliştiriciler İçin

### Firebase Auth  
🔐 Firebase Authentication kullanılarak güvenli kullanıcı kimlik doğrulama sağlanır.

### Firestore Multi-tenant  
🗂️ Firestore üzerinde her işletme için ayrı belge yapısı ile çoklu işletme desteği sunulur (`businessId` bazlı yapı).

### MVVM Architecture  
🏗️ ViewModel katmanı ile tüm UI State ayrı olarak yönetilir. Repository yapısı ile veriler ayrıştırılır.

### Firebase Rules  
🛡️ Kullanıcıların sadece kendi işletme verilerine erişebilmesi için güvenli Firestore kuralları tanımlanmıştır.

### Dependency Injection  
📦 Hilt kullanılarak servis bağımlılıkları modüler ve yönetilebilir hale getirilmiştir.

### Room & DataStore  
💾 Yerel veriler Room ile saklanır. Kullanıcı ayarları gibi kalıcı tercihler DataStore ile tutulur.

### UI & Performance  
⚙️ Jetpack Compose ile oluşturulan modern UI, bellek dostu yapı ve reaktif akış ile performans optimize edilmiştir.

---

## 🏗️ Project Structure Map

```groovy
// 🗺️ BeautyDate - Project Structure Map

📁 app/src/main/java/com/borayildirim/beautydate/
├── 📄 BeautyDateApplication.kt
├── 📄 MainActivity.kt
│
├── 🧩 components/
│   ├── 🧱 ActionCard.kt
│   ├── 🧱 AppointmentCard.kt
│   ├── 🧱 AppointmentColorLegend.kt
│   ├── 🧱 AppointmentDetailBottomSheet.kt
│   ├── 🧱 AppointmentFab.kt
│   ├── 🧱 AppointmentFilterChips.kt
│   ├── 🧱 AppointmentSearchBar.kt
│   ├── 🧱 AuthComponents.kt
│   ├── 🧱 BottomNavigationBar.kt
│   ├── 🧱 CommonComponents.kt
│   ├── 🧱 CustomerAppointmentHistoryBottomSheet.kt
│   ├── 🧱 CustomerDropdownSelector.kt
│   ├── 🧱 CustomerFab.kt
│   ├── 🧱 CustomerItem.kt
│   ├── 🧱 CustomerSearchBar.kt
│   ├── 🧱 DayScheduleCard.kt
│   ├── 🧱 OtherMenuItem.kt
│   ├── 🧱 PasswordField.kt
│   ├── 🧱 PasswordFieldWithInfo.kt
│   ├── 🧱 PaymentMethodBottomSheet.kt
│   ├── 🧱 ServiceFab.kt
│   └── 🧱 ThemeSelector.kt
│
├── 🧠 data/
│   ├── 🗺️ CityDistrictData.kt
│   ├── 🗂️ local/
│   │   ├── 📄 AppointmentEntity.kt
│   │   ├── 📄 CustomerDao.kt
│   │   ├── 📄 CustomerDatabase.kt
│   │   ├── 📄 CustomerEntity.kt
│   │   ├── 📄 CustomerNoteDao.kt
│   │   ├── 📄 CustomerNoteEntity.kt
│   │   ├── 📄 DateTimeConverter.kt
│   │   ├── 📄 EmployeeDao.kt
│   │   ├── 📄 EmployeeEntity.kt
│   │   ├── 📄 ServiceDao.kt
│   │   ├── 📄 ServiceEntity.kt
│   │   ├── 📄 ThemePreferences.kt
│   │   ├── 📄 TutorialPreferences.kt
│   │   └── 📄 UserPreferences.kt
│   ├── 🔧 dao/
│   │   ├── 📄 AppointmentDao.kt
│   │   ├── 📄 ExpenseDao.kt
│   │   ├── 📄 PaymentDao.kt
│   │   ├── 📄 TransactionDao.kt
│   │   └── 📄 WorkingHoursDao.kt
│   ├── 📦 entities/
│   │   ├── 📄 ExpenseEntity.kt
│   │   ├── 📄 PaymentEntity.kt
│   │   ├── 📄 TransactionEntity.kt
│   │   └── 📄 WorkingHoursEntity.kt
│   ├── 🧾 models/
│   │   ├── 📄 Appointment.kt
│   │   ├── 📄 Customer.kt
│   │   ├── 📄 CustomerFirestore.kt
│   │   ├── 📄 CustomerNote.kt
│   │   ├── 📄 DayHours.kt
│   │   ├── 📄 DayOfWeek.kt
│   │   ├── 📄 Employee.kt
│   │   ├── 📄 EmployeeFirestore.kt
│   │   ├── 📄 Expense.kt
│   │   ├── 📄 ExpenseCategory.kt
│   │   ├── 📄 ExpenseFirestore.kt
│   │   ├── 📄 Payment.kt
│   │   ├── 📄 PaymentFirestore.kt
│   │   ├── 📄 Service.kt
│   │   ├── 📄 ServiceCategory.kt
│   │   ├── 📄 ServiceFirestore.kt
│   │   ├── 📄 Statistics.kt
│   │   ├── 📄 Transaction.kt
│   │   ├── 📄 TransactionFirestore.kt
│   │   ├── 📄 Tutorial.kt
│   │   ├── 📄 User.kt
│   │   └── 📄 WorkingHours.kt
│   ├── ☁️ remote/models/
│   │   ├── 📄 AppointmentFirestore.kt
│   │   ├── 📄 CustomerNoteFirestore.kt
│   │   └── 📄 WorkingHoursFirestore.kt
│   └── 📚 repository/
│       ├── 📄 AppointmentRepository.kt
│       ├── 📄 AppointmentRepositoryImpl.kt
│       ├── 📄 AuthRepository.kt
│       ├── 📄 CustomerNoteRepository.kt
│       ├── 📄 CustomerNoteRepositoryImpl.kt
│       ├── 📄 CustomerRepository.kt
│       ├── 📄 EmployeeRepository.kt
│       ├── 📄 EmployeeRepositoryImpl.kt
│       ├── 📄 ExpenseRepository.kt
│       ├── 📄 ExpenseRepositoryImpl.kt
│       ├── 📄 FeedbackRepository.kt
│       ├── 📄 PaymentRepository.kt
│       ├── 📄 ServiceRepository.kt
│       ├── 📄 ServiceRepositoryImpl.kt
│       ├── 📄 StatisticsRepository.kt
│       ├── 📄 StatisticsRepositoryImpl.kt
│       ├── 📄 ThemeRepository.kt
│       ├── 📄 TransactionRepository.kt
│       ├── 📄 TransactionRepositoryImpl.kt
│       ├── 📄 TutorialRepository.kt
│       ├── 📄 TutorialRepositoryImpl.kt
│       ├── 📄 WorkingHoursRepository.kt
│       └── 📄 WorkingHoursRepositoryImpl.kt
│
├── 🧪 di/
│   ├── 📄 AppModule.kt
│   └── 📄 RepositoryModule.kt
│
├── 🔁 domain/
│   ├── 📋 models/
│   │   ├── 📄 FeedbackData.kt
│   │   └── 📄 OtherMenuItem.kt
│   └── 💡 usecases/
│       ├── 📄 FeedbackUseCase.kt
│       ├── 📄 LoginUseCase.kt
│       ├── 📄 RegisterUseCase.kt
│       ├── 📄 ThemeUseCase.kt
│       ├── appointment/
│       │   ├── 📄 AddAppointmentUseCase.kt
│       │   ├── 📄 GetAppointmentsUseCase.kt
│       │   └── 📄 UpdateAppointmentStatusUseCase.kt
│       └── customer/
│           ├── 📄 AddCustomerUseCase.kt
│           ├── 📄 GetCustomersUseCase.kt
│           └── 📄 SyncCustomersUseCase.kt
│
├── 🧭 navigation/
│   ├── 📄 AppNavigation.kt
│   └── 📄 BottomNavigationItem.kt
│
├── 📱 screens/
│   └── 📄 ... (tüm ekranlar üstte listelendi)
│
├── 🎨 ui/theme/
│   ├── 🎨 Color.kt
│   ├── 🎨 Theme.kt
│   └── 🎨 Type.kt
│
├── 🧰 utils/
│   ├── 🔒 AuthUtil.kt
│   ├── 🌐 NetworkMonitor.kt
│   ├── 🔁 PaginationHelper.kt
│   ├── 🔐 PasswordValidator.kt
│   ├── ☎️ PhoneNumberTransformation.kt
│   ├── 📝 RegisterUtils.kt
│   ├── ✅ RegisterValidation.kt
│   ├── 📦 RepositoryCache.kt
│   ├── ⏳ SearchDebouncer.kt
│   ├── 🔔 ToastUtils.kt
│   └── 🧪 ValidationUtils.kt
│
├── 📏 utils/validation/
│   └── 📄 AuthValidator.kt
│
└── 🧠 viewmodels/
    ├── 📄 AppointmentViewModel.kt
    ├── 📄 AuthViewModel.kt
    ├── 📄 CalendarViewModel.kt
    ├── 📄 CustomerNoteViewModel.kt
    ├── 📄 CustomerViewModel.kt
    ├── 📄 EmployeeViewModel.kt
    ├── 📄 ExpenseViewModel.kt
    ├── 📄 FeedbackViewModel.kt
    ├── 📄 FinanceViewModel.kt
    ├── 📄 OtherMenuViewModel.kt
    ├── 📄 ServiceViewModel.kt
    ├── 📄 StatisticsViewModel.kt
    ├── 📄 ThemeViewModel.kt
    ├── 📄 TutorialViewModel.kt
    ├── 📄 WorkingHoursViewModel.kt
    ├── 🎬 actions/
    │   ├── 📄 AuthActions.kt
    │   ├── 📄 FeedbackActions.kt
    │   ├── 📄 ServiceActions.kt
    │   └── 📄 ThemeActions.kt
    └── 📊 state/
        ├── 📄 AppointmentUiState.kt
        ├── 📄 AuthUiState.kt
        ├── 📄 CalendarUiState.kt
        ├── 📄 CustomerNoteUiState.kt
        ├── 📄 CustomerUiState.kt
        ├── 📄 EmployeeUiState.kt
        ├── 📄 FeedbackUiState.kt
        ├── 📄 OtherMenuState.kt
        ├── 📄 ServiceUiState.kt
        ├── 📄 StatisticsUiState.kt
        ├── 📄 ThemeState.kt
        ├── 📄 TutorialUiState.kt
        └── 📄 WorkingHoursUiState.kt
```

---
---

## 🚀 Özellikler

- 🔐 Güvenli e-posta/şifre ile kimlik doğrulama  
- 🗂️ Çoklu işletme destekli Firestore yapısı (her işletme için ayrı veri alanı)  
- 📋 Müşteri kayıt ve geçmiş takibi  
- 📆 Saat, hizmet ve çalışan bazlı randevu oluşturma  
- 🧑‍💼 Çalışan yönetim modülü  
- 💇 Hizmet listeleme ve kategoriye ayırma (ör. manikür, lazer, masaj)  
- 📊 Görsel istatistik paneli  
- 💸 Gider ve işlem yönetimi ile finans modülü  
- 📁 Kullanıcıya özel notlar ve randevu filtreleme  
- 🧾 Tüm varlıklar için CRUD (müşteri, hizmet, çalışan, not vb.)  
- 🌐 Tam dil desteği (EN / TR)  
- ❌ Profil ayarlarından hesap silme özelliği  

> Küçük veya büyük ölçekli tüm güzellik salonları için tasarlanmıştır; sınırsız çalışan, müşteri ve randevu desteği sunar.

---

## 🛠️ Kullanılan Teknolojiler

| Kategori             | Araçlar ve Frameworkler                                                           |
| -------------------- | -------------------------------------------------------------------------------- |
| Programlama Dili     | Kotlin                                                                           |
| UI Arayüzü           | Jetpack Compose, Material 3                                                      |
| Mimari               | MVVM (Model-View-ViewModel), SOLID prensipleri                                   |
| Bağımlılık Enjeksiyonu | Hilt (Dagger)                                                                  |
| Navigasyon           | Jetpack Navigation Component                                                     |
| State Yönetimi       | UI State + ViewModel ayrımı                                                      |
| Yerel Depolama       | Room (SQLite ORM), DataStore                                                     |
| Bulut Servisleri     | Firebase (Firestore, Authentication)                                             |
| Güvenlik Kuralları   | Firebase Firestore kuralları (multi-tenant, businessId tabanlı erişim)           |
| Asenkron İşlemler    | Kotlin Coroutines                                                                |
| Görsel Yükleme       | Coil (Compose Image Loading)                                                     |
| Ağ Bağlantı Kontrolü | Özel NetworkMonitor.kt                                                           |
| Doğrulama            | Regex tabanlı özel validator'lar (PasswordValidator, ValidationUtils)            |
| Dil Desteği          | Çoklu dil desteği (strings.xml + strings-tr.xml) ve dinamik LocaleHelper         |
| Yardımcı Araçlar     | Debouncer, RepositoryCache, ToastUtils, AuthUtil, vb.                            |

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

## 🏁 Son Not

Bu proje, Kotlin ile Android geliştirme yolculuğumun bir parçası olarak inşa edildi. Uygulamayı yaparken amacım temiz kodlama ve solid prensiplerine uyumluluk, ölçeklenebilir ve modern mimari, kullanılabilirlik, sürdürülebilirlik ve modern tasarım ilkelerine odaklanmaktı. **BeautyDate**, oluşturulurken sadece fonksiyonel bir uygulama değil — gerçek işletmeler için eksiksiz bir salon yönetim aracı olarak kullanılması düşünülmüştür. Projeyi klonlayabilir, inceleyebilir veya katkı sağlayabilirsiniz. Geri bildirimleriniz benim için çok değerlidir!

[← Ana Sayfaya Dön](https://github.com/borayldrmm/BeautyDate/tree/main)



<p align="center">
  <img src="https://github.com/user-attachments/assets/2580e1c0-e529-44e5-9527-0d39622a944d" alt="android" width="50" />
  <img src="https://github.com/user-attachments/assets/5814cb0b-c23d-41c5-9669-4e1b50942010" alt="androidstudio" width="50" />
  <img src="https://github.com/user-attachments/assets/c8b96971-4656-45d5-bcfe-31a9f3112084" alt="room" width="50" />
  <img src="https://github.com/user-attachments/assets/a617cf09-4f93-4697-8c1c-b12df6f9fcc1" alt="authentication" width="50" />
  <img src="https://github.com/user-attachments/assets/122caa9d-410b-47c9-a3f9-51b379fcf649" alt="coroutines" width="50" />
  <img src="https://github.com/user-attachments/assets/2275f8fc-1ab8-44b2-9ce7-80bf83febe89" alt="firebase" width="50" />
  <img src="https://github.com/user-attachments/assets/d319d689-ff2f-4519-80a2-34d334f23510" alt="firebase-storage" width="50" />
  <img src="https://github.com/user-attachments/assets/26fbaf47-59ab-4832-988f-3602be2d7006" alt="jetpack_compose" width="50" />
  <img src="https://github.com/user-attachments/assets/906227a6-6c7d-437d-9133-cc4fff17e6d0" alt="json" width="50" />
  <img src="https://github.com/user-attachments/assets/41747e93-5cc8-4831-886e-fefd96832c65" alt="kotlin" width="50" />
  <img src="https://github.com/user-attachments/assets/c838a38b-749d-4106-95f5-256cb943d70a" alt="mvvm" width="50" />
  <img src="https://github.com/user-attachments/assets/af5ed77a-c07e-4c1c-b79f-5edd3a18dd8e" alt="hilt" width="50" />
  <img src="https://github.com/user-attachments/assets/b69c4ac9-7998-4339-83ab-21e54e3c7312" alt="coil" width="50" />
</p>
