

  
# Rakenduse 'Postkasti Teenus' sessiooni halduse juhend

## 1. Sissejuhatus

Selle dokumendi eesmärk on kirjeldada tehnilisi vahendeid kasutaja sessiooni halduseks **Postkasti Teenus** rakenduse integreeritud paigalduses. See rakendus on välja töötatud vastavalt ['Tarkvara arendus- ja hooldustööd II' riigihanke](https://riigihanked.riik.ee/rhr-web/#/procurement/1703912/general-info) proovitöö nõuetele ja [spetsifikatsioonile](https://riigihanked.riik.ee/rhr-web/#/procurement/1703912/documents/source-document?group=B&documentOldId=13329969).

## 2. Sessiooni haldus

Parimate praktikate kohaselt on loodud lahenduses implementeeritud sessiooni haldus, võimaldamas turvalist kommunikatsiooni autenditud kasutaja ja rakenduse vahel. Proovitöö sessiooni halduse tarbeks on sessiooni salvestus ja hoidmine lahendatud **Redis Enterprise** klastris, mis täidab lahenduses kahte eesmärki:
  - Tagab sessiooni keskse halduse, hoidmise, sessiooniga seotud andmete halduse.
  - Tagab sessiooni hoidmise klasterdatud HA keskkonnas - võimaldamas nii koormuse all skaleerimist kui ka in-memory replikatsiooni ja talituspidevust.

## 3. Sessiooni loomine

Rakendus alustab sessiooni kasutaja sisse logimisel ja hoiab sessiooni üleval kuni üks kahest tingimusest ei täitu:
 - Kasutaja ei logi välja.
 - Sessiooni elupikkuse määraja (TTL) ei saavuta hetke kellaaega.

Sessiooni alguses määratakse Redises sessioonile TTL parameeter, konfiguratsioonis määratud sessiooni elupikkuse perioodi väärtuseks, vaikimisi on see 30 minutit.

Sessiooni elupikkus määratakse auth-service mikroteenuse konfiguratsiooni failis [application.yml](https://10.0.9.217/projects/RH6/repos/rh6/browse/backend/auth-service/src/main/resources/application.yml), parameetris `spring/session/timeout`.

Iga päringuga, mida kasutaja teeb, pikendatakse TTL vastavalt siis taaskord now() + mitteaktiivsuse perioodi väärtuse võrra, konfiguratsioonist.

Juhul, kui kasutaja ei tee mitteaktiivsuse perioodi vältel päringuid, siis saabub sessioonil TTL periood ja sessioon on seeläbi sundkujul lõpetatud mitteaktiivsuse tõttu. Peale TTL perioodi saabumist, ei ole enam võimalik sessiooni TTL inkrementeerida ja, juhul, kui kasutaja saadab selle sessiooni raames uue päringu, suunatakse kasutaja automaatselt TARA autentimise lehele.

## 4. Sessiooni hoidmine klastris

Mistahes mikroteenus või selle instants ei teenindaks päringut - kogu sessiooniga seotud informatsiooni hoitakse keskses sessiooni hoidlas, milleks on **Redis Enterprise**. Redise kasutusele võtt annab võimaluse mitte muretseda, kui mõni rakenduse komponent läheb maha või keeldub teenindamast - kõik sessiooniga seotud andmed on hoitud Redises. Redis omakorda on paigaldatud HA metoodikat kasutades, seega ka Redise enda õla kukkumine ei hävita kasutaja sessiooni.

Redis on tarnitud kahe õlalises konfiguratsioonis ja õlgade vahel on pidev andmete replikeerimine Redise enda vahenditega. Seeläbi on Redis kaitstud ka ühe oma õlgadest maha mineku ees.

Redises erinevate kasutajate ja erinevate sessioonide andmed on hoitud ranges eraldatuses ja isolatsioonis. Selleks, et olla kindel, et ühe sessiooni kasutaja ei saa ligipääsu teise sessiooni andmetele, siis omistatakse igale algavale sessioonile unikaalne sessiooni ID. Selle ID olemasolu tagabki sessiooni andmete kindla eraldatuse.

Sessiooni andmed sisaldavad endas muuhulgas:

   - Sõnumi mustandit, juhul, kui kasutaja on sõnumi kirjutamist alustanud. Mis omakorda sisaldab endas ka sõnumi teksti, pealkirja, saaja andmeid ja manuseid, mida kasutaja jõudis üles laadida.
   - DigiDoc konteineri sõnumi sisu koos kõigi manustega, seda juhul, kui kasutaja jõudis sõnumi allkirjastada.
   - Kasutaja isikukoodi ning AAR rolli, mida kasutaja on sisselogimisel valinud.

Proovitöö mikroteennuste kiht on arendatud stateless rakenduste kogumina, mis tähendab, et ükski mikroteenus ei hoia endas mistahes sessiooni või muid seanssi puudutavaid andmeid. Kasutaja sessiooni andmetele ligipääsemiseks kasutavad mikroteenused justnimelt kasutaja poolt etteantud sessiooni ID-d, mis kasutajale väljastatakse igal autentimisel ja mis on iga kord unikaalne. Sessiooni ID-d salvestatakse turvaliselt kliendi küpsistes.

## 5. Sessiooni hävitamine kasutaja väljalogimisel

Kasutaja väljalogimisel, määratakse Redises sessioonile TTL=now(), seeläbi sessioon jääb Redisesse, kuid see ei ole enam kasutatav. Redise sisemine rämpsu kollektor korjab selle sessiooni üles ja kustutab Redisest ära. Tegu on standardse Redise käitumisega.

Iga päringu peale - mistahes teenus kontrollib sessiooni aktiivsust Redises (läbi auth-service mikroteenuse API) ja juhul kui sessioon ei ole enam aktiivne (TTL>=now()) või sessiooni ei leita, siis suunatakse kasutaja suunatakse TARA autentimislehele.