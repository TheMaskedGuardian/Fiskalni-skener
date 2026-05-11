# 🧾 Fiskalni Skener (Serbian Receipt Scanner)

[English](#english) | [Srpski](#srpski)

---

<a name="english"></a>
## English

**Fiskalni Skener** is an Android application for scanning and parsing Serbian fiscal receipts. It retrieves receipt details from the official Tax Administration portal and integrates them into the **Cashew** finance management app.

### 🚀 Key Features
- **QR Scanning**: Uses Google ML Kit to read Serbian fiscal QR codes.
- **Data Retrieval**: Fetches merchant info and the full item list from `suf.purs.gov.rs`.
- **Cashew Integration**: Sends amount, date, store name, and items to the Cashew app.
- **Text Processing**: Automatic Cyrillic-to-Latin conversion for better compatibility.

### 🛠️ Technologies
- Kotlin, Google ML Kit, OkHttp3, Jsoup, Gson.

### 🏗️ How to Build
1. **Clone the repository**:
   ```bash
   git clone https://github.com/TheMaskedGuardian/Fiskalni-skener.git
   ```
2. **Open in Android Studio**: Select the folder `Fiskalni-skener/FiskalniSkenerApp`.
3. **Build**: 
   - Via menu: **Build > Generate Signed App Bundle / APK...**
   - Via terminal: `./gradlew assembleDebug`

---

<a name="srpski"></a>
## Srpski

**Fiskalni Skener** je Android aplikacija za skeniranje i očitavanje srpskih fiskalnih računa. Aplikacija preuzima detalje računa sa zvaničnog portala Poreske uprave i šalje ih direktno u aplikaciju **Cashew** za upravljanje finansijama.

### 🚀 Glavne Funkcije
- **QR Skeniranje**: Koristi Google ML Kit za očitavanje srpskih fiskalnih QR kodova.
- **Preuzimanje podataka**: Dozvata informacije o prodavnici i listu artikala sa sajta `suf.purs.gov.rs`.
- **Cashew Integracija**: Šalje iznos, datum, naziv prodavnice i stavke računa u Cashew aplikaciju.
- **Obrada teksta**: Automatska konverzija ćirilice u latinicu radi bolje kompatibilnosti.

### 🛠️ Korišćene Tehnologije
- Kotlin, Google ML Kit, OkHttp3, Jsoup, Gson.

### 🏗️ Kako instalirati i pokrenuti
1. **Preuzmite kod**:
   ```bash
   git clone https://github.com/TheMaskedGuardian/Fiskalni-skener.git
   ```
2. **Otvorite u Android Studiju**: Izaberite folder `Fiskalni-skener/FiskalniSkenerApp`.
3. **Build**: 
   - Preko menija: **Build > Generate Signed App Bundle / APK...**
   - Ili preko terminala: `./gradlew assembleDebug`
