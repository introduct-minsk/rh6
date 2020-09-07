
# Proovitöö - Postkasti Teenus

Proovitöö on teostatud vastavalt [spetsifikatsioonile](https://riigihanked.riik.ee/rhr-web/#/procurement/1703912/docuaments/source-document?group=B&documentOldId=13368625), mis on avaldatud Riigi Infosüsteemi Ameti poolt [riigihanke 220867 "Tarkvara arendus- ja hooldustööd II"](https://riigihanked.riik.ee/rhr-web/#/procurement/1703912/general-info) raames ja arvesse on võetud kõiki spetsifikatsiooni nõudeid.

## 1. Üldinformatsioon

Rakenduse **Postkasti Teenus** peamine eesmärk on võimaldada autentitud kasutajal lugeda oma postkasti saabunud teateid, saata teateid, lisada, kustutada ning allalaadida teadete manuseid, saada lugemise kinnitusi oma saadetud sõnumitele ning **põhilise eesmärgina: digitaalselt allkirjastada saadetavaid sõnumeid ja veritifseerida saabunud sõnumite digiallkirjade valiidsust**.

Toetatud funktsioonid:

- Kasutaja autentimine ja isiklike andmete pärimine läbi X-Tee:
- kasutaja autentimine TARA teenuse kaudu
- rolli valik, AAR-i registrist saadud isikule võimaldatud rollide hulgast
- Isikuandmete pärimine Rahvastikuregistrist RR414 teenuse kaudu
- Postkasti sõnumitega seotud toimingud:
- sõnumite loendite vaatamine
- sõnumite otsing
- sõnumite lugemine
- sõnumite saatmine
- sõnumitele manuste lisamine, muutmine, kustutamine ja allalaadimine
- kogu sõnumi digitaalne allkirjastamine, koos manustega
- sõnumi digitaalse allkirja verifitseerimine
- Lugemise kinnituste automaatne genereerimine

## 2. Arhitektuursed otsustuspunktid

**Autentimine**

Rakendus on integreeritud TARA raamistikuga. See tähendab, et lahendus ei piira isikute arvu, kes omavad lahendusele ligipääsu - mistahes isik, kes on võimeline ennast autentima **TARA raamistiku testkeskkonnas**, on võimeline lahendusele ligipääsu saama ja lahenduse funktsionaalsust kasutama.

Proovimiseks pakume kasutada RIA keskkonnas asuvat [test keskkonda](https://10.1.19.35/).

Keskkond on liidestatud TARA testkeskkonnaga ja sisse logida võib mistahes TARA testkonnas toetatud [test andmetega](https://e-gov.github.io/TARA-Doku/Testimine#testimine-testnumbrite-ja-id-kaardiga).

See ühtlasi tähendab ka seda, et rakenduse ülesse ehitamisel tuleb kasutajal sõlmida RIA-ga leping TARA testkeskkonna kasutamiseks, sest iga leping on seotud konkreetse keskkonnaga (nn. callback-URL'iga) ja määrata rakenduse ehituse konfiguratsioonis vastavad lepingu parameetrid (kliendi identifikaator, mida väljastab RIA koos lepinguga jt.). Kuna prooviülesanne on mõeldud RIA sisemiseks valideerimiseks, siis me usume, et kasutajal ei teki sellega muret, nii nagu ei tekkinud seda meil.

Otsustuspunkt kasutada TARA raamistiku, lisaks selgele nõudmisele [spetsifikatsioonis](https://riigihanked.riik.ee/rhr-web/#/procurement/1703912/documents/source-document?group=B&documentOldId=13368625), tuleneb ka Pakkuja kindlast veendumusest, et taoliste projektide realisatsiooni puhul tuleks maksimaalselt taaskasutada seda, mis on juba loodud ja kui riigil on olemas keskne autentimisteenus avalike riigiasutuste tarbeks, ei ole ühtegi põhjust hakata ehitama oma autentimisteenust.

**VEERA raamistik**

Hanke ühe nõudena on seatud VEERA disainraamistiku kasutamine. VEERA raamistik on oma varajases alfa staadiumis ja sellel eksisteerivad olemasolevad realisatsioonid nii Angular kui React raamistikus. Pakkuja on neid põhjalikult läbi vaatanud ja jõudnud otsusele, et olemasolevad realisatsioonid vajavad olulist edasiarendust (mida kinnitavad ka nende autorid) ning ei realiseeri VEERA printsiipe ühtlaselt. Omades kompetentsi nii Angular kui React raamistikes, on Pakkuja siiski otsustanud kirjutada oma realisatsiooni VEERA raamistikust Vue.JS-is, mida hetkel, Pakkujale teadaolevalt veel ei eksisteeri. Pakkuja on lähtunud ka arvamusest, et Vue.JS raamistik on antud ülesande tarbeks tehniliselt kõige optimaalsem. (Loe: Pakkuja arvamusel, et Veera komponentide libra loomine nullist Vue.JS-is oli lihtsam, kiirem ja mõistlikum kui olemasoleva Angular.JS realisatsiooni optimiseerimine soovitud kujule. Pakkuja on seda põhjalikult läbi testinud ka praktikas.)

**X-Tee pärimine**

Hanke ühe nõudena on nõutud autenditud kasutaja andmete pärimine Rahvastikuregistrist läbi X-Tee ning autenditud kasutaja rollide nimekirja pärimine AAR registrist.

Nagu Hankija Confluence lehekülgedel (Proovitöö üldinfo, Lisainfo) on soovitanud, siis kasutusele on võetud Rahvastikuregistri teenus RR414_v3 (Isiku suhete ja hooldusõiguse päring isikukoodi järgi. ADS andmed) isikuandmete pärimiseks.

Mitmetest päritavatest andmetest, kuvatakse kasutajale tema kasutajaliideses:

- Ees- ja perekonnanimi
- Sünnikuupäev
- Elukoha aadress

Kasutaja sisselogimisel TARA raamistiku kaudu, peale edukat autentimist, edastab süsteem päringu AAR (Ühine õiguste haldamise infosüsteem) registrisse, pärimaks kasutajale omistatud rolle. Selleks on kasutuses aar.oigused.v1 (Õiguste otsing) päring.

Juhul, kui kasutajal on AAR registris omistatud rohkem kui üks roll, kuvatakse kasutajale valik, võimalusega valida roll, milles ta sooviks postkasti siseneda. Sõltuvalt valitud rollist, siis kasutaja opereerib süsteemis üksnes selle rolli õigustes. Ehk näiteks valides ennast juriidilise isiku esindajana, näeb kasutaja sellele juriidilisele isikule ja selle juriidilise isiku poolt saadetud sõnumeid, saadab sõnumeid selle juriidilise isiku nimel jne. Enda kui eraisiku sõnumeid kasutaja sel hetkel ei näe ja nende nägemiseks peab kasutaja uuesti sisse logima ning valima enda kui eraisiku rolli.

**Otsing**

Oleme realiseerinud otsingu mehhanismi ElasticSearch põhilise full-text indekseerimise otsingu mootori abiga. Selle tarbeks on loodud eraldi mikroteenus, otsingu mikroteenus, mis on lisatud tarnesse.

Kasutatud ElasticSearch-i versiooni näol on tegemist täiendatud forkiga, originaalsest versioonist, kuhu on lisatud täiendavat funktsionaalsust, s.h TLS kliendisertifikaatidega autentimine, mis on ühtlasi soovitud funktsionaalsus Hankija poolt. Originaalne ElasticSearch ei toeta kliendisertifikaadiga autentimist (ilma lisalitsentsi soetamiseta).

**Digiallkiri**

Vastavalt Hankija nõuele, postkastis, sõnumi koostamisel on kasutajal võimalik digitaalselt allkirjastada kogu sõnumit koos kõigi manustega. Digitaalse allkirjastamise tulemusena, koostatakse digitaalselt allkirjastatud konteiner ASiC-E (Associated Signature Container Extended) formaadis, mis koosneb sõnumi failist, koos kõigi sõnumi manustega. Kasutajal, peale edukat digitaalset allkirjastamist, ei ole enam võimalik sõnumi sisu või manuseid muuta: sõnumit kas saab saata või tervikuna katkestada.

**Digitaalseks allkirjastamiseks on kasutatud DigiDoc4J teeki.**

Tehnoloogia valik on tingitud ühelt poolt Hankija soovitusest ja teiselt poolt teegi üldomadustest. Võrreldes SiGa-ga, on DigiDoc4j oluliselt üldisema ja laiema kattega ja võimaldab tulevikus efektiivselt teenust laiendada.

Lahenduses eksisteerib veel üks osa, mis on seotud digiallkirjadega. Iga kord, kui digitaalselt signeeritud sõnumit kuvatakse kasutajale (k.a koheselt, peale digiallkirjastamist), siis selle signatuuri valiidsust kontrollitakse. Selle kontrolli jaoks oleks väga loogiline kasutada sama DigiDoc4j teegi võimalusi, kuid kuna Hankija nõuetes oli selgelt soovitatud kasutada SiVa teenust, siis selle tarbeks, eraldi mikroteenusena, on tõstetud SiVa teenus, mille abil kontrollitakse sõnumi digitaalse allkirja valiidsust. See on teostatud ainult sellel eesmärgil, et demonstreerida Hankijale meie võimeid kasutada SiVa teenust: reaalse ülesande raames, omades valikut, mis teenust kasutada - võtaksime signatuuri verifitseerimiseks kasutusele sama DigiDoc4j teegi, mis on kasutatud sõnumi signeerimiseks.

**File storage**

Kuna Hankija prooviülesande spetsifikatsioonis on selgelt nõutud failide üles laadimise, muutmise ja allalaadimise funktsionaalsus, siis see otseselt tingib failihoidla komponendi disainimise. Kuna meie hinnangul failide andmebaasis hoidmine on paljudes stsenaariumites väga väär taktika (konkreetselt ka siin juhuses, kuna faile ilmselgelt hakkab palju tekkima tulenevalt use-case'ist) ja põhjustatult mikroteenuste arhitektuurist, siis nende failide failisüsteemis hoidmine ei ole lihtsasti realiseeritav (mikroteenused on stateless ning ei hoia kliendi andmeid). Seetõttu on selle eesmärgi täitmiseks pildile toodud Hadoop HDFS klaster,  mis on äärmiselt vastupidav, sarnaste ülesannete täitmiseks mõeldud, distributeeritud ja on võimeline jooksma ka väga madala otsa riistvara peal. Hadoop selles valguses tundub ideaalseks valikuks ja see on paigaldatud klastri konfiguratsioonis 1+2.

## 3. Arhitektuurne disain

Rakendus on kavandatud kahes põhikomponendis:

- **Mikroteenuste põhine API kiht** - disainitud Java-s nelja selge funktsioonide ja vastutuse piiridega mikroteenuste komplektina.
- 
- **Web front-end** - disainitud Vue.js-is, on presentatsiooni kiht, mis realiseerib lõppkasutajate vooge ja sisaldab minimaalselt äriloogikat (ainult voogude loomise osas). Esikülg on välja töötatud Veera raamistiku nõuete alusel.

**Mikroteenuste põhine API kiht** pakub API teenuseid infovahetuseks:

- kasutajaliidesega, et võimaldada back-end tuge esitluskihis realiseeritavate äriprotsesside ja funktsioonide tarbeks

- administraatori juurdepääsu läbi API teenuste, andmebaasi andmete haldamiseks (sisestamine ja lugemine)
  *Tarnes on eeldefineeritud administraatori kasutaja, mis võimaldab andmete andmebaasi(st) automaatse laadimise mõlemas suunas.*
  *API teenused on selleks realiseeritud ja märgistatud allpool **administraatori ligipääs andmebaasile** spetsifkatsiooniga.*

- mikroteenuste omavaheliseks suhtluseks ja andmevahetuseks

Mikroteenus ise on ehitatud pidades silmas stateless arhitektuuri, kus iga instants on isoleeritud, teistest instantsidest sõltumatu olem, see tähendab, et mikroteenuse esinemisjuhtude arvu saab suurendada mistahes arvule.

Nginxi koormusjaotur on lisatud tarnesse, aktiveerib ja jaotab koormuse intstantside vahel automaatselt lihtsal round-robin koormuse jaotuse printsiibi alusel. Tulenevalt stateless arhitektuurist ei pea Nginx koormusjaotur koormuse jaotamisel jälgima mikroteenuste sessioonide püsivust.

Alljärgnevalt on välja toodud mikroteenuste komplekt koos vastavate funktsioonide, meetodite ja suhtlusosapooltega.

**Messages mikroteenus**

Võimaldab autentitud kasutajal töötada kogu sõnumite elukaarega, alates sõnumi mustandi loomisest kuni digitaalselt allkirjastatud sõnumi saatmiseni teisele kasutajale. Samuti lubab teenus autentitud kasutajal genereerida automaatselt kinnitusteateid, kui kasutaja on sõnumi esimest korda avanud.

|Nimi|Kirjeldus|Meetod|Suhtluspartnerid|
|--|--|--|--|
|/api/v1/messages|Uus sõnum|POST|Esitluskiht|
|/api/v1/messages|Hangi sõnumite nimekiri|GET|Esitluskiht|
|/api/v1/messages/{messageId}|Hangi sõnumi üksikasjad|GET|Esitluskiht|
|/api/v1/messages/search|Otsi sõnumit sisu järgi|GET|Esitluskiht|
|/api/v1/messages/{messageId}/sign|Hangi allkirjastatud sõnumi DigiDoc konteiner|GET|Esitluskiht|
|/api/v1/messages/{messageId}/attachments/{attachmentsId}|Hangi manus|GET|Esitluskiht|
|admin/data/mailbox_db/messages|Loo uus sõnum|POST|administraatori ligipääs andmebaasile|
|admin/data/mailbox_db/messages/{id}|Hangi sõnumi üksikasju|GET|administraatori ligipääs andmebaasile|
|admin/data/mailbox_db/messages|Hangi sõnumite nimekiri|GET|administraatori ligipääs andmebaasile|
|admin/data/mailbox_db/messages/files/upload|Lae manus üles failihoidlasse|POST|administraatori ligipääs andmebaasile|
|admin/data/mailbox_db/messages/files/{fileId}|Hangi manus failihoidlast|GET|administraatori ligipääs andmebaasile|
|/api/v1/messages/draft/settings|Hangi konfiguratsiooni seaded|GET|Esitluskiht|
|/api/v1/messages/draft|Hangi sõnumi mustand|GET|Esitluskiht|
|/api/v1/messages/draft|Salvesta sõnumi mustand|POST|Esitluskiht|
|/api/v1/messages/draft|Muuda sõnumi mustand|PATCH|Esitluskiht|
|/api/v1/messages/draft|Kustuta sõnumi mustand|DELETE|Esitluskiht|
|/api/v1/messages/draft/attachments/upload|Lae manus sõnumi mustandile|POST|Esitluskiht|
|/api/v1/messages/draft/attachments/{attachmentsId}|Asenda / Muuda manus sõnumi mustandis|PUT|Esitluskiht|
|/api/v1/messages/draft/attachments/{attachmentsId}|Lae alla manus sõnumi mustandilt|GET|Esitluskiht|
|/api/v1/messages/draft/attachments/{attachmentsId}|Kustuta manus sõnumi mustandilt|DELETE|Esitluskiht|
|/api/v1/messages/draft/sign/data|Hangi digiallkirja üksikasju|GET|Esitluskiht|
|/api/v1/messages/draft/sign|Allkirjasta lõplikult sõnum OSCP ajatempliga|POST|Esitluskiht|
|/api/v1/messages/draft/sign|Kustuta signeeritud sõnum|DELETE|Esitluskiht|
|/api/v1/messages/draft/sign|Hangi signeeritud sõnnum|GET|Esitluskiht|
|/api/v1/messages/draft/send|Salvesta ja saada sõnum|POST|Esitluskiht|

**User data mikroteenus**

Võimaldab kasutajal hankida kasutaja isiklikke andmeid, rolle ja hallata kasutaja eelistusi.
Mikroteenuses on realiseeritud ka privaatne API. See pole lõppkasutajate jaoks saadaval ja seda kasutatakse sõnumi saatja ja vastuvõtja isiklike andmete hankimiseks.
Hanke prooviülesande spetisifikatsioonis on selgelt nõutud X-Tee päringute koostamise ja kasutamise oskuse kontrollimiseks X-Tee päringute teostamise demonstreerimine. Antud mikroteenus ühtlasi teostab ka X-Tee päringuid, seda nii autentimisel kui ka sõnumi saatmisel, kui tuvastatakse saaja nimi isikukoodist.

|Nimi|Kirjeldus|Meetod|Suhtluspartnerid|
|--|--|--|--|
|/api/v1/users/me|Hangi autenditud kasutaja isiklikud andmed|GET|Esitluskiht|
|/api/v1/users/me/settings|Salvesta kasutaja eelistused|POST|Esitluskiht|
|/api/v1/users/me/settings|Hangi kasutaja eelistused|GET|Esitluskiht|
|/api/v1/users/me/roles|Saada roll, mis on kasutaja poolt valitud|POST|Esitluskiht|
|/api/v1/users/me/roles|Hangi rollide nimekiri autentitud kasutaja jaoks|GET|Esitluskiht|
|/private/api/users?id=1&id=2|Hangi sõnumi saaja ja saatja isiklike andmeid|GET|mikroteenuste vaheline suhtlus|
|/user_db/users|Loo uus kasutaja|POST|administraatori ligipääs andmebaasile|
|/user_db/users/{id}|Hangi kasutaja isiklikud andmed|GET|administraatori ligipääs andmebaasile|
|/user_db/users|Hangi kasutajate nimekiri|GET|administraatori ligipääs andmebaasile|

**Authentication mikroteenus** Võimaldab kasutajal autentida ja genereerida administraatori juurdepääsu jaoks juurdepääsuluba.

|Nimi|Kirjeldus|Meetod|Suhtluspartnerid|
|--|--|--|--|
|/api/v1/oauth2/authorization/tara|Autoriseeri kasutaja TARA kaudu|GET|Esitluskiht|
|/api/v1/oauth2/logout|Logi kasutaja välja|POST|Esitluskiht|
|/api/v1/oauth/token|Genereeri administraatori ligipääsu token (autendi administraatorit)|POST|administraatori ligipääs andmebaasile|

**Search mikroteenus** Võimaldab luua indekseid ja otsib sõnumeid nende sisu järgi.

|Nimi|Kirjeldus|Meetod|Suhtluspartnerid|
|--|--|--|--|
|/private/api/search/sources|Loo indeks|POST|mikroteenuste vaheline suhtlus|
|/private/api/search/sources|Sõnumite otsing indeksis|GET|mikroteenuste vaheline suhtlus|

**Postkasti Teenuse** rakendus tarbib tehnilisest aspektist kahte tüüpi lõpp-punkte:

- REST / JSON lõpp-punktid, mida toetab JSON-skeemipõhine framework - neid lõpp-punkte tarbitakse **Mikroteenuste põhisest API kihist** ja TARA autentimisteenusest
- SOAP-põhised lõpp-punktid, mida **Postkasti Teenuse** rakendus tarbib X-Teelt, kuna tegemist on X-Tee alustehnoloogiaga.

## 4. Kasutajate autentimine ja tuvalisus

Kasutajate autentimiseks on kasutuses TARA autentimisteenus. Kui kasutaja autentimine on algatatud, suunatakse kasutaja TARA autentimislehele ja pärast autentimist naaseb kasutaja autoriseerimisloaga esilehele tagasi, kus ligipääsu võimaldatakse. Autoriseerimisluba aktuaalsus kinnitatakse **Authentication mikroteenuse** lisapäringu kaudu TARA teenusele.

Administraatori (masinloetavate liideste kaudu) juurdepääs on tagatud Client Credentials loa kaudu. Seda kasutatakse andmebaasi administreerimiseks JSON protokollil põhinevate masinloetavate liideste kaudu (näiteks sõnumite mass laadimiseks või mass lugemiseks, või kasutajate loomiseks). **Client credentials ligipääs ei võimalda kasutajaliidese kaudu ligipääsu ja töötab vaid masinloetavatel liidestel** administreerimise eesmärgiga.

Autentimise token salvestatakse kasutaja poolel küpsistesse ning kõik päringud REST/JSON tarbimispunktidele varustatakse enkapsuleeritud autentidtud sessiooni tokeniga HTTP päisetes.

X-Tee SOAP lõpp-punktide jaoks tagab turvalisuse X-tee hajutatud arhitektuur, turvaserverid ja tehnoloogiad. **Postkasti Teenus** on X-Tee mõistes tarbija rakendus, mille eesmärk on luua õigete väärtustega nõuetekohase päringu ja edasise töötlemise ja turvalisuse X-Tee võrgus tagab juba X-Tee turvaserver.

**Kõigi komponentide vaheline suhtlus on turvatud SSL sertifikaatidega - s.h. ka sisemiste komponentide vaheline suhtlus. Antud tarne puhul kasutatakse self-signed sertifikaate, kuna tegu on proovitööga.**

**Kõikide rakenduskihi komponentide vaheline suhtlus kohustuslikus korras nõuab ka klientsertifikaadiga autentimist, kus kohustuslikus korras kontrollitakse klientsertifikaadi allkirjastamist usaldatud CA poolt.**

## 5. Persistent storage

**Postkasti Teenuse** rakendus kasutab nn. persistant storage-i jaoks (püsimälu tarbeks) PostgreSQL andmebaasi, mis on konfigureeritud UTF8-d kodeeringus ning Hadoop HDFS klastri üleslaetavate ja genereeritavate failide hoidmiseks.

REST-lõpppunktide kaudu on võimaldatud administraatori ligipääs (vt. mikroteenuste tarbepunktide kirjeldused ja käesoleva dokumendi autentimise sektsioon), PostgreSQL andmebaasist andmete lugemiseks ja andmete andmebaasi laadimiseks ning samuti ka Hadoop klastriga opereerimiseks samadel põhimõtetel.

Sisestamiseks ja lugemiseks otse andmebaasi on JSON liideste kaudud saadaval järgmised peamised äriolemid:

-  Kasutaja
-  Sõnum
- Manus ja selle üksikasjad

**Andmestruktuuride upgrade-kontroll on tagatud [Flyway](https://flywaydb.org) raamistiku kasutusega andmebaasi tasemel.**

## 6. Logimine

Logimine on kõikides teenustes realiseeritud läbi konfigureeritava logimise mehhanismi.

Vastavalt mikroteenuste ehitamise parimatele tavadele *ükski mikroteenuse konteiner ei kirjuta logi kettale*, mis oleks muidu väga jäme mikroteenuste ülesehitamise põhimõtete eiramine.

Selle asemel, kõik mikroteenused, ülesse ehitamisel, loevad oma logimise konfiguratsiooni konfiguratsiooni failist ja käituvad vastavalt sellele. Vaikekonfiguratsioonis suunatakse kõik logid RIA testkeskkonnas ülesse tõstetud logi serverisse striimina.

## 7. Kõrgkäideldavus ja koormuse jaotus

Kogu rakendus on loodud kõrgkäideldavuse ning dünaamilise skaleeritavuse printsiipide alusel.

- Kõik mikroteenuste instantsid on loodud stateless arhitektuuri alusel, mis võimaldab käivitada nii palju instantsi igast teenusest kui parasjagu vaja on (seda ka dünaamiliselt, kui kasutada mõnda dünaamilist skaleerimise keskkonda, nt Kubernetes või Openstack).
- Igal mikroteenuse instantsil on olemas heartbeat lehekülg, mida kasutab tarnega kaasasolev Nginx koormusejaotur instantsi staatuse ja tervise pärimiseks. Samu heartbeat lehekülgi on võimalik inkorporeerida ka automaatsete monitooringu süsteemidega (nagu näiteks Nagios või Zabbix).

Heatbeat leheküljed, igal mikroteenuse instantsil on kättesaadavad järgmise URL-i kaudu: ```https://{host}:{port}/api/v1/{mikroteenus}/actuator/health```.

Autentifikaatori mikroteenuse heartbeat lehekülg on kättesaadav: ```https://{host}:{port}/oauth/actuator/health```.

Iga heartbeat lehekülg, vastab JSON formaadis ühe väljaga: ```status```, kui välja väärtus on mistahes, kui ```UP``` tegu on probleemiga teenuses.

Näidised heartbeat lehekülgedest on kättesaadavad meie testkeskkonnast:

- [autentifikaatori heartbeat](https://10.1.19.35/oauth/actuator/health)
- [message teenuse heartbeat](https://10.1.19.35/api/v1/messages/actuator/health)
- [users teenuse heartbeat](https://10.1.19.35/api/v1/users/actuator/health)
- [search teenuse heartbeat](https://10.1.19.35/api/v1/search/actuator/health)

Andmebaas on loodud upgrade-control abstraktsiooni raamistikuga Flyway, mis võimaldab teostada green/blue skeemil paigaldusi, teenust maha võtmata.

Koos tarnega tarnitakse ka eelkonfigureeritud Nginx koormusjaotur: vaikimisi konfiguratsioonis projekti ehitades, ehitatakse igast mikroteenusest valmis kaks instantsi ja koormusjaotur jagab automaatselt koormust kahe õla vahel, kasutades primitiivset round-robin printsiipi. Stateless arhitektuur võimaldab koormuse jaotamisel sessiooni seisu mitte jälgida.

## 8. Kasutajaliidese automaattestimine

Kasutajaliides on kaetud minimaalse hulga automaattestidega.

**Automaattestide teostatus**

Automaattestid katavad järgmise funktsionaalsuse:

- Kasutaja sisselogimine (kasutaja 1)
- Kasutaja poolt teisele kasutajale (kasutaja 2) sõnumi saatmine
- Kasutaja sisselogimine (kasutaja 2)
- Kasutaja poolt sõnumi lugemine (kasutaja 2)
- Kasutaja poolt (kasutaja 1) automaatse lugemise kinnituse vastuvõtt.

Automaattestid on realiseeritud eraldi projektina, mida tarnitakse koos prooviülesandega samas arhiivis.

Automaattestide projekt koos testimisjuhendiga on saadaval ka [lähtekoodi repositaariumist](https://10.0.9.217/projects/RH6/repos/rh6/browse/ria_qa/)

Automaattestide projektiga on kaasas ka üksikasjalik käivitamise juhend, samuti on automaatteste võimalik käivitada ka RIA CI/CD keskkonnast (Jenkins), selleks on Jenkinsis loodud eraldi [töövoog](https://rh6-jenkins-01.dev.riaint.ee/job/Run%20qa%20tests/18/console).

## 9. Tehnoloogiline raamistik

Tehnoloogiate osas on **Postkasti Teenuse** rakendus üles ehitatud järgmise tehnoloogiate kogumi peale:

|Tehnoloogiline osa|Tehnoloogia|Versioon|
|--|--|--|
|Põhitehnoloogia|Java põhiraamistik|Java 11|
|Peamine raamistik|Spring boot|2.2.8|
|Security|spring-security-oauth2|2.3.8|
|Esitluskihi raamistik|vue.js|2.6.11|
||bootstrap-vue|2.15.0|
||vue-i18n|8.18.2|
||vue-router|3.3.2|
||vuex|3.4.0|
||veera-styles|latest version|
|HTTP klient|axios|0.19.2|
|Andmebaasi upgrade kontroll|flyway|6.0.8|
|ORM|hibernate|5.4.12|
|Logimine|log4j|1.2.17|
||logback|1.2.3|
|Unit testimine|testcontainers|1.13.0|
|Kasutajaliidese automaattestimine|Selenium||
||Selenoid||
||Python||
|Persistent storage|PostgreSQL||
|Cache|Redis||
|File storage|Hadoop||
||hadoop-common|3.2.1|
||hadoop-hdfs-client|3.2.1|
|Digital signature|digidoc4j|4.0.0|
|X-Tee|xtee-client-transport|4.2.11|
|X-Tee|xtee-typegen|4.2.11|
|X-Tee|JRoad|4.2.11|
||apache.commons|3.9|