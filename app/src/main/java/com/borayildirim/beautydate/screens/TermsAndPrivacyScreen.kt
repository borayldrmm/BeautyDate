package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.R

/**
 * Terms and Privacy Policy screen for BeautyDate app
 * Displays legal text with accept confirmation
 * Material Design 3 compliant with Turkish content
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TermsAndPrivacyScreen(
    onAcceptTerms: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Kullanım Koşulları ve Gizlilik Politikası",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Terms content
            Text(
                text = """1. KULLANIM KOŞULLARI

Bu metin, BeautyDate mobil uygulamasının kullanımına ilişkin esasları, yükümlülükleri ve tarafların haklarını düzenlemektedir. Uygulamayı indiren, yükleyen veya herhangi bir şekilde kullanan her kullanıcı, bu koşulları okuyup anladığını ve tamamen kabul ettiğini beyan etmiş sayılır.

Kullanıcı, uygulamayı sadece yürürlükteki mevzuata, genel ahlaka ve kamu düzenine uygun biçimde kullanmayı taahhüt eder. Uygulamada sunulan hizmetlerin amacı; güzellik salonlarının müşteri, randevu ve hizmet takibini dijital olarak kolaylaştırmak ve organize etmektir. Uygulamanın kaynak kodu, veritabanı yapısı, içerikleri ve tüm fikri mülkiyet hakları geliştiriciye aittir. Herhangi bir unsur izinsiz şekilde kopyalanamaz, değiştirilemez veya dağıtılamaz.

BeautyDate, önceden bildirimde bulunmaksızın uygulamayı değiştirme, geçici olarak durdurma veya tamamen kapatma hakkına sahiptir. Hizmetin kesintisiz, hatasız veya belirli bir performans düzeyinde çalışacağına dair herhangi bir garanti verilmemektedir.

Kullanıcı, hesabına ait tüm işlemlerden ve bu işlemlerden doğabilecek yasal sonuçlardan tamamen sorumludur. Şifre ve giriş bilgileri üçüncü kişilerle paylaşılmamalı, kişisel verilerin güvenliği sağlanmalıdır. Uygulama kapsamında gerçekleştirilen işlemlerin sorumluluğu kullanıcıya aittir.

BeautyDate, hizmetin doğası gereği üçüncü taraf altyapılarla (örneğin Firebase gibi) entegre çalışabilir. Bu bağlamda kullanıcı verileri, sadece hizmetin sağlanabilmesi amacıyla ve ilgili sistemlerin güvenlik politikaları çerçevesinde paylaşılabilir.

İşbu koşullar zaman zaman güncellenebilir. Güncellenen metinler, uygulama içerisinden veya kullanım sırasında kullanıcıya sunulacaktır. Kullanıcı, güncellenmiş koşulları kabul etmediği takdirde hizmeti kullanmaya devam etmemelidir.

Taraflar arasında doğabilecek ihtilaflarda Türkiye Cumhuriyeti yasaları geçerli olup, İstanbul Merkez Mahkemeleri ve İcra Daireleri yetkilidir.

⸻

2. GİZLİLİK POLİTİKASI

BeautyDate, kullanıcılarının kişisel verilerinin gizliliğine ve korunmasına büyük önem vermektedir. Bu politika, uygulama kapsamında toplanan kişisel verilerin hangi amaçlarla toplandığını, nasıl saklandığını, kimlerle hangi koşullarda paylaşıldığını ve kullanıcıların haklarını açıklamak amacıyla hazırlanmıştır.

Uygulama üzerinden kullanıcıya ait ad, soyad, telefon numarası, e-posta adresi, randevu detayları, işlem geçmişi gibi bilgiler toplanabilir. Bu veriler, sadece hizmetin sunulması, uygulama deneyiminin geliştirilmesi, sistem güvenliğinin sağlanması ve yasal yükümlülüklerin yerine getirilmesi amacıyla kullanılacaktır.

Toplanan kişisel veriler, kullanıcı onayı olmadan üçüncü taraflara aktarılmaz. Ancak yasal zorunluluklar, resmi kurum talepleri veya uygulamanın çalışmasını sağlayan teknik altyapı sağlayıcıları ile (örneğin Firebase) sınırlı düzeyde paylaşım yapılabilir.

Kullanıcılar, kişisel verilerinin ne amaçla işlendiğini, ne kadar süre saklandığını, kimlerle paylaşıldığını öğrenme; verilerinin düzeltilmesini, silinmesini veya işlenmesine itiraz etmeyi talep etme hakkına sahiptir. Bu talepler, uygulama içerisindeki destek menüsünden veya belirlenen iletişim kanalı üzerinden iletilebilir.

Kişisel veriler, yürürlükteki mevzuata uygun şekilde, gerekli teknik ve idari güvenlik önlemleri alınarak korunur. Ancak internet altyapısının doğası gereği, yüzde yüz güvenlik garanti edilememektedir.

Bu gizlilik politikası zaman zaman değiştirilebilir. Güncellemeler uygulama içerisinde yayımlanır ve kullanıcılar bu değişiklikleri takip etmekle yükümlüdür.

⸻

3. KVKK AYDINLATMA METNİ

İşbu Aydınlatma Metni, 6698 sayılı Kişisel Verilerin Korunması Kanunu ("KVKK") uyarınca, veri sorumlusu sıfatıyla hareket eden BeautyDate tarafından hazırlanmıştır.

Kullanıcılara ait kişisel veriler; kimlik bilgileri, iletişim bilgileri, müşteri ve randevu kayıtları, işlem ve uygulama kullanım verileri dahil olmak üzere, BeautyDate mobil uygulamasının işleyişi ve kullanıcı deneyiminin sağlıklı biçimde sürdürülebilmesi amacıyla işlenmektedir.

Bu veriler otomatik yollarla (örneğin mobil uygulama arayüzü üzerinden) toplanmakta olup, yalnızca hizmetin sunulması amacıyla yurt içinde veya yurtdışında yer alan teknik hizmet sağlayıcılarla (örneğin Firebase, Google hizmetleri) paylaşılabilmektedir. Veriler, KVKK'nın 5. ve 6. maddelerinde belirtilen işleme şartlarına dayanılarak, açık rıza olmaksızın da işlenebilecek hukuki sebepler kapsamında değerlendirilebilir.

Veri sahipleri olarak, KVKK'nın 11. maddesi uyarınca şu haklara sahipsiniz:
	•	Kişisel verilerinizin işlenip işlenmediğini öğrenme,
	•	İşlenmişse buna ilişkin bilgi talep etme,
	•	Amacına uygun kullanılıp kullanılmadığını öğrenme,
	•	Yurt içinde veya yurt dışında verilerin aktarıldığı üçüncü kişileri bilme,
	•	Verilerin düzeltilmesini veya silinmesini talep etme,
	•	İşleme faaliyetlerine itiraz etme.

Bu haklarınıza ilişkin taleplerinizi, uygulama içerisindeki destek bölümünden bize iletebilirsiniz.

Kişisel verileriniz, yasal saklama süreleri boyunca güvenli şekilde muhafaza edilmekte ve bu sürenin sonunda silinmekte, yok edilmekte veya anonim hale getirilmektedir.""",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        // Bottom accept button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onAcceptTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Okudum, Onaylıyorum",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
} 