
# Known issues ehk teadlikud kitsaskohad.

Antud dokumendis on esile tõstetud kõik arendusmeeskonna poolt tuvastatud kitsaskohad ja ebakõlad, mis proovitöös esinevad ja mille parandamine ei olnud objektiivsetel põhjustel arendusmeeskonna võimetes. Iga kitsaskoha ja ebakõla puhul on välja toodud ka mitigeerimise taktika, mis enamasti piirdub upstream-i ehk tootjapoolse paranduspaki paigaldusega, kui see ilmub. (Antud dokumendis kitsaskoha kirjeldamine tähendab seda, et projekti kirjutamise ajal paranduspakki tootjapoolel ei eksisteerinud). Lisaks on väljatoodud mõned "false-positive'id" ehk valehäired.

Kõik kitsaskohad ja ebakõlad on jaotatud sisuliste aspektide järgi peatükkidesse.

## OWASP TOP-10 vastavus

### FRONT-END sõltuvuste nõrkused

|Komponent|Viide|Kirjeldus|Põhjus|Mitigeerimise taktika|
|--|--|--|--|--|
|yargs-parser:10.1.0|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_front/113/dependency-check-findings/)|CVE-2020-7608|Pakett on kasutuses üksnes ja rangelt arendusproofilis (tegu on Vue.JS käsurea paketiga, mida kasutatakse konsoolist arendaja poolt) ning seda ei lisata tootmisprofiili. Turvanõrkus tuleneb paketis sisalduva teadva turvanõrkuse tõttu. Pakett on uuendatud viimase versioonini. Uut paketti versiooni tänaseks päevaks ei ole olemas. Paketist loobumine on raskendatud, sest arenduses on seda raske asendada.|Tootja on nõrkusest teadlik. Ootame uut versiooni. Tuleb jälgida, et pakett ei sattuks tootmisprofiili.|
|yargs-parser:10.1.0|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_front/113/dependency-check-findings/)|CWE-400|Pakett on kasutuses üksnes ja rangelt arendusproofilis (tegu on Vue.JS käsurea pakettiga, mida kasutatakse konsoolist arendaja poolt) ning seda ei lisata tootmisprofiili. Turvanõrkus tuleneb paketis sisalduva teadva turvanõrkuse tõttu. Pakett on uuendatud viimase versioonini. Uut paketi versiooni tänaseks päevaks ei ole olemas. Paketist loobumine on raskendatud, sest arenduses on seda raske asendada.|Tootja on nõrkusest teadlik. Ootame uut versiooni. Tuleb jälgida, et pakett ei sattuks tootmisprofiili.|
|yargs-parser:13.1.2|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_front/113/dependency-check-findings/)|NPM-1500|Pakett on kasutuses üksnes arendusprofiilis (tegu on Vue.JS käsurea paketiga, mida kasutatakse konsoolist arendaja poolt) ning seda ei lisata tootmisprofiili. Turvanõrkus tuleneb paketis sisalduva ning teadva turvanõrkuse tõttu. Pakett on uuendatud viimase versioonini. Uut paketi versiooni tänaseks päevaks ei ole olemas. Paketist loobumine on raskendatud, sest arenduses on seda raske asendada.|Tootjale on meie poolt tõstetud [vea raport](https://github.com/vuejs/vue-cli/issues/5573). Ootame uut versiooni. Tuleb jälgida, et pakett ei sattuks tootmisprofiili.|

### BACK-END sõltuvuste nõrkused
*(mitmed CVE'd, ühtegi medium-high pole)*

|Komponent|Viide|Kirjeldus|Põhjus|Mitigeerimise taktika|
|--|--|--|--|--|
|htrace-core4-4.1.0-incubating|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Parandatud uues versioonis, ei saa kasutusele võtta, kuna ei tarbi antud paketti otse vaid teise paketi sõltuvusena (kettsõltuvus). Loobuda ei saa. (baaspakett)|Ootame "emapaketi" uuendust. "Emapakett" on viimasel versioonil, kuid kasutab veel vana sõltuvust. Tootja teavitatud.|
|log4j-1.2.17|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Turvanõrkused pakettis. Parandust tootja poolt pole. Loobumine väga raskendatud. (baaspakett)|Ootame tootja poolset parandust.|
|log4j-core-2.12.1|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Parandatud uues versioonis, ei saa kasutusele võtta, kuna ei tarbi antud paketti otse vaid teise paketti sõltuvusena (kettsõltuvus). Loobumine väga raskendatud. (baaspakett)|Ootame "emapaketi" uuendust. "Emapakett" on viimasel versioonil, kuid kasutab veel vana sõltuvust. Tootja teavitatud.|
|spring-cloud-security-2.2.2|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Turvanõrkused paketis. Parandust tootja poolt pole. Loobuda ei saa. (baaspakett)|Ootame tootjapoolset parandust.|
|spring-security-core-5.2.5.RELEASE|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Turvanõrkused paketis. Parandust tootja poolt pole. Loobuda ei saa. (baaspakett)|Ootame tootjapoolset parandust.|
|spring-security-oauth2-core-5.2.5.RELEASE|[OWASP Dependency Check raport](https://rh6-jenkins-01.dev.riaint.ee/job/rh6_backend/lastFailedBuild/dependency-check-findings/)|Mitmed avalikud CVE-d.|Turvanõrkused paketis. Parandust tootja poolt pole. Loobuda ei saa. (baaspakett)|Ootame tootjapoolset parandust.|


### BACK-END turvanõrkused

|Nõrkus|Viide|Kirjeldus|Põhjus|Mitigeerimise taktika|
|--|--|--|--|--|
|Private IP Disclosure|[OWASP ZAP raport](OWASP_ZAP_report.html)|False-positive|Lahendus jookseb RIA sisevõrgus asuvates serverites, seega loogiline, et näha on sisemisi IP aadresse.|Ei ole tarvis.|


### FRONT-END turvanõrkused
|Nõrkus|Viide|Kirjeldus|Põhjus|Mitigeerimise taktika|
|--|--|--|--|--|
|Cookie No HttpOnly Flag|[OWASP ZAP raport](OWASP_ZAP_report.html)|False-positive|Tegu on täiesti oodatud, ja tehnoloogia poolt ette nähtud käitumisega. [Tsitaat Wikipediast: "*The CSRF token cookie must not have httpOnly flag, as it is intended to be read by the JavaScript by design.*"](https://en.wikipedia.org/wiki/Cross-site_request_forgery#Cookie-to-header_token). Selle muutmine oleks ilmselge tehnoloogia eiramine.|Ei ole tarvis.|
|Cross-Domain Misconfiguration|[OWASP ZAP raport](OWASP_ZAP_report.html)|semi-false positive|Tegu on õige märkusega, kuid märkus tuleneb Swagger-UI pakettist, ja osaliselt selle loomust. API kirjeldused Swagger-UI's ongi mõeldud olema avalikud, ning lubamas integratsioone kolmandatelt osapoolteelt. Selleks on nad ka loodud. Tõsiasi ka see, et production set-up'is siiski oleks mõistlik seada CORS-i vaatest piiranguid, nagu seda on soovitatud Swaggeri [ametlikus dokumentatsioonis](https://swagger.io/docs/open-source-tools/swagger-ui/usage/cors/). Põhjus miks seda ei ole praegu tehtud, peitub selles, et me ei tea, mis kujul ja mis masinatest/domeenidest hakkab lugupeetud Hankija API'si testima (kuid proovülesanne spetsifikatsioonis oli selgelt märgitud, et testitakse), seega valides kahe valiku vahel: kas riskida antud nõrkuse olemsoluga, vs. riskida sellega, et API-ga automaatseks integreerimiseks tuleb lugupeetud Hankijal teha täiendavaid lisaliigutusi, oleme teadlikult valinud esimese.|Ei ole tarvis lisaliigutusi integreerimise faasis. Kui integratsioonid on paigas - tuleb seada kas ligipääsu või CORS piirangud (või mõlemat) ning skaneerida rakendus uuesti. |