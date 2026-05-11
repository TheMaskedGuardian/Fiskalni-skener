# 🧾 Fiskalni Skener - Tehnička Dokumentacija

**Fiskalni Skener** je napredna Android aplikacija koja povezuje svet papirnih fiskalnih računa sa digitalnim upravljanjem finansijama u **Cashew** aplikaciji.

## 🚀 Tehnološki Stack

Aplikacija je izgrađena korišćenjem modernih tehnologija za Android platformu:

### 1. Vizuelno Prepoznavanje (AI)
* **Tehnologija**: Google ML Kit Vision API.
* **Proces**: Koristi lokalne modele mašinskog učenja (On-device AI) za detekciju barkodova.
* **Optimizacija**: Implementiran je Custom CameraX pipeline koji forsira **1080p rezoluciju** i dinamički fokus, što omogućava čitanje i najkompleksnijih QR kodova sa velikom količinom podataka.

### 2. Ekstrakcija Podataka (Web Scraping & API)
Pošto portal Poreske uprave (`suf.purs.gov.rs`) koristi dinamičko učitavanje, aplikacija primenjuje sledeći workflow:
* **Mrežni sloj**: `OkHttp3` za asinhronu komunikaciju.
* **HTML Analiza**: `Jsoup` biblioteka za ekstrakciju metapodataka (PIB, Prodavnica, Adresa).
* **AJAX Emulacija**: Aplikacija simulira `XMLHttpRequest` kako bi pristupila endpointu `/specifications`.
* **JSON Procesiranje**: `Gson` biblioteka za mapiranje sirovih podataka sa servera u struktuirane objekte artikala.

### 3. Komunikacija između Aplikacija (Deep Linking)
* **Mehanizam**: `Intent.ACTION_VIEW`.
* **Protokol**: Aplikacija generiše HTTPS Deep Link koji Cashew aplikacija presreće.
* **Sanitizacija**: Implementiran je agresivni algoritam za konverziju ćirilice u ASCII latinicu. Ovo osigurava da URL link ostane validan i stabilan bez obzira na korišćeno pismo na računu.
* **Preciznost**: Podaci se šalju u punom ISO 8601 formatu (Datum + Vreme), što omogućava precizno beleženje transakcija.

## 🛠️ Detaljan Workflow

1. **Skeniranje**: ML Kit detektuje URL računa u video streamu.
2. **Dohvatanje**: `ReceiptFetcher` šalje upite portalu i preuzima osnovne podatke i tokene.
3. **Ekstrakcija**: POST zahtevom se preuzima kompletna specifikacija artikala.
4. **Konverzija**: Podaci se formatiraju prema Cashew specifikaciji (negativan iznos, puna lista stavki, adresa i vreme).
5. **Prenos**: Otvara se Cashew sa predefinisanim parametrima transakcije.

## 📱 Sistemski Zahtevi
* **OS**: Android 8.0 ili noviji.
* **Hardver**: Kamera sa auto-fokusom.
* **Internet**: Neophodan za dohvatanje stavki sa portala Poreske uprave.

---
*Razvijeno za maksimalnu preciznost i automatizaciju.* 🚀
