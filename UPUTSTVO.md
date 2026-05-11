# 🧾 Fiskalni Skener - Tehnička Dokumentacija

**Fiskalni Skener** je napredna Android aplikacija koja premošćuje jaz između papirnog fiskalnog računa i digitalnog upravljanja finansijama u **Cashew** aplikaciji.

## 🚀 Tehnološki Stack

Aplikacija je izgrađena korišćenjem najmodernijih tehnologija za Android platformu:

### 1. Vizuelno Prepoznavanje (AI)
* **Tehnologija**: Google ML Kit Vision API.
* **Proces**: Koristi lokalne modele mašinskog učenja (On-device AI) za detekciju barkodova.
* **Optimizacija**: Za razliku od standardnih biblioteka, ovde je implementiran Custom CameraX pipeline koji forsira **1080p rezoluciju** i dinamički fokus, što omogućava čitanje i najkompleksnijih QR kodova koji sadrže velike količine podataka.

### 2. Ekstrakcija Podataka (Web Scraping & API)
Pošto portal Poreske uprave (`suf.purs.gov.rs`) koristi dinamičko učitavanje podataka, aplikacija primenjuje sledeći workflow:
* **Mrežni sloj**: `OkHttp3` za asinhronu komunikaciju.
* **HTML Analiza**: `Jsoup` biblioteka za ekstrakciju metapodataka (PIB, Prodavnica, Adresa, Vreme).
* **AJAX Emulacija**: Aplikacija simulira `XMLHttpRequest` (identično kao što to radi browser) kako bi pristupila endpointu `/specifications`.
* **JSON Procesiranje**: `Gson` biblioteka za mapiranje sirovih podataka sa servera u struktuirane objekte artikala.

### 3. Komunikacija između Aplikacija (Deep Linking)
* **Mehanizam**: `Intent.ACTION_VIEW`.
* **Protokol**: Aplikacija generiše HTTPS Deep Link koji Cashew aplikacija presreće.
* **Sanitizacija**: Implementiran je inteligentni algoritam za konverziju ćirilice u latinicu i čišćenje specijalnih karaktera. Ovo osigurava da Deep Link URL ostane validan i unutar sistemskih limita (oko 2000 karaktera), čak i kod veoma dugih računa.

## 🛠️ Detaljan Workflow

1. **Skeniranje**: ML Kit detektuje URL računa u video streamu.
2. **Dohvatanje**: `ReceiptFetcher` šalje GET zahtev portalu i preuzima osnovni HTML.
3. **Identifikacija**: Iz skripti na stranici se izvlače `InvoiceNumber` i `Token`.
4. **Ekstrakcija**: Šalje se POST zahtev za specifikacije, čime dobijamo listu artikala.
5. **Konverzija**: Svi podaci se formatiraju prema Cashew specifikaciji (negativan iznos, ISO datum, plain-text beleške).
6. **Prenos**: Otvara se Cashew sa predefinisanim parametrima transakcije.

## 📱 Sistemski Zahtevi
* **OS**: Android 8.0 (Oreo) ili noviji.
* **Hardver**: Kamera sa auto-fokusom.
* **Internet**: Neophodan za dohvatanje stavki sa portala Poreske uprave.

---
*Razvijeno za maksimalnu efikasnost u svakodnevnom životu.* 🚀
