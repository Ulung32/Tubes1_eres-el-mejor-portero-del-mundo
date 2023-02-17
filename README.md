# TUBES 1 Strategi Algoritma
Repository ini dibut untuk memenuhi Tugas Besar 1 matkul IF 2211 Strategi Algoritma

## Deskripsi Singkat Tugas
>Pada tugas ini, mahasiswa diminta untuk mengimplementasikan sebuah bot dengan menggunakan algoritma greedy yang dapat digunakan untuk permainan "Galaxio"

## Strategi Greedy yanng dipakai
Objektif dari permainan ini adalah bertahan dan mengeliminasi lawan sehingga menjadikan satu satunya pemain yang bertahan hingga permainan selesai. Untuk mewujudkan objektif tersebut kita harus memaksimalkan dalam pemilihan heading dan aksi yang akan diambil. Startegi greedy yang dibuat adalah menghindari pemain yang lebih besar dan obstacle, menggunakan shield, menembak torpedo, memakai teleport, menembak supernova, dan mencari makan.


## Requirement
1. [.Net core 3.1](https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/sdk-3.1.407-windows-x64-installer)
2. [Java](https://www.java.com/download/ie_manual.jsp)
3. [Stater-pack](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
4. [Maven](https://phoenixnap.com/kb/install-maven-windows)
5. [.Net 5.0](https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/runtime-5.0.17-windows-x64-installer?cid=getdotnetcore)

## Membuat jar
1. masuk ke directory repository yang sudah di clone
2. Build jar
```
mvn clean package
```

## menmbuat  pertandingan
1. buat file dengan ekstensi .bat yang berisi
```
@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
cd ../

pause
```
2. tambahkan directory file target yang sudah dibuild kedalam file bat
```
@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/
timeout /t 3
start "bot kita semua" java -jar "...\Tubes1_eres-el-mejor-portero-del-mundo\target\JavaBot.jar"
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
cd ../

pause
```
3. Masuk ke directory tempat disimpannya file bat, dan run file bat
```
./{nama_file}.bat
```

## memvisualisasi pertandingan
1. Buka folder visualisaser pada folder starter-pack yang sudah didownload
2. buka aplikasi galaxio
3. load file .json gamelog yang ingin ditampilkan 
4. klik start dan nikmati pertandingan

## Author
1. Muhammad Rizky Syab'an       (13521119)
2. Ulung Adi Putra              (13521122)
3. Muhammad Zaki Amanullah      (13521146)