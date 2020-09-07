


## Postkasti Teenus API (administraatori ligip��s)

API eesm�rk on v�imaldada tugimeeskonnal hallata postkasti s�numeid (masslugemine, masslaadimine), manuseid (masslugemine, masslaadimine) ja kasutajakontosid. 
Autentimine on n�utud, et saada juurdep��s postkasti teenuse administreerimise API-le. 

## Administraatori ligip��s
Administraatori kontod on kirjeldatud andmebaasis.
Uue administraatori konto lisamine k�ib andmebaasi kaudu.
Vaiketarnes, rakendust tarnitakse �he vaikeadministraatori kontoga andmebaasis.

Autentimiseks palun kasutage:

`client_id: admin-client`
`client_secret: mailboxpassword`

## Autentimine
K�ik administreerimise API liidesed n�uavad eelnevat administraatori autentimist ja valiidse sessiooni tokeni olemasolu. Tokeni saab eduka autentimise tulemusel (autentimisprotokoll on OAuth2).
Autentimine toimub eraldi mikroteenuse poole p��rdumisel, eduka autentimise tulemusel v�ljastatakse administraatorile kehtiv sessiooni token, mida tuleb hilisemate administraatori API teenuste p��rdumisel m��rata p�ringu p�isetes HTTP Basic skeemil (n�ited edasi):

URL: https://[host]:[port]/oauth/token
Test keskkonna n�ide: [https://10.1.19.35/oauth/token](https://10.1.19.35/oauth/token)


**P�ring**

See p�ring v�imaldab administraatoril ennast autentida ning omandada sessiooni token edasiste administreerimistegevuste teostamiseks. Autentimisp�ring sisaldab kasutajatunnust ja parooli: client_id ja client_secret ja m�nda t�iendavat omadust.
_Administraatori konto peab olema andmebaasis eeldefineeritud (tarne teostatakse vaikimisi �he administaatori kontoga)._

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|client_id|Jah|string|Administraatori kasutaja juurdep��su ID|
|client_secret|Jah|string|Administraatori kasutaja juurdep��su parool|
|grant_type|Jah|string|V��rtus peab alati olema "client_credentials"|
|scope|Ei|string|Ankur edasise arenduse v�imaldamiseks: antud v�li on sisuliselt autoriseerimise (mitte segamini ajada autentimisega) m��raja, mis m��rab autorisatsiooni ulatust. Praeguses versioonis on ainult �ks v�imalus - "Any" ja see annab t�ieliku juurdep��su k�igile selles dokumendis kirjeldatud administreerimise API-dele, kuid hiljem on v�imalik lisada t�iendavaid limiteerijaid - piirates administraatori sessiooni ulatust.|


**Vastus**

Kui autentimise p�ring on edukas, vastab autentimisliides juurdep��suloaga.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|access_token|Jah|string|Juurdep��su token, mis on v�ljastatud autentimisserveri poolt|
|token_type|Jah|string|Juurdep��su tokeni t��p, hetkel alati �bearer� |
|expires_in|Jah|integer|Aja kestus sekundites, mille v�ltel juurdep�su token kehtib.|
|scope|Jah|string|Ulatus n�itab andmetele juurdep��su taset (vt. p�ringu sama v�lja kirjeldust).|


**Vead**

V�imalikud vead:

- invalid_request � P�ringul puudub parameeter, nii et server ei saa p�ringuga j�tkata. Seda v�ib esineda ka siis, kui p�ring sisaldab toetamata parameetrit v�i korduvat parameetrit.
- invalid_client � Kliendi autentimine nurjus, n�iteks kui p�ring sisaldab kehtetut kliendi ID-d v�i parooli. Sellisel juhul HTTP vastuse kood on seatud v��rtusele 401.
- invalid_grant � V�lja grant_type v��rtus on vale (peab alati olema "client_credentials").
- invalid_scope � P�ringus sisalduv ulatuse v��rtus on vale (hetkeversioonis peab alati olema "Any").
- unauthorized_client � Sellel kliendil ei ole volitust taotletud ligip��su kasutada.
- unsupported_grant_type � V�lja grant_type v��rtus on vale (peab alati olema "client_credentials").


**N�idisp�ring:**

`curl -X POST https://admin-client:mailboxpassword@10.1.19.35/oauth/token -dgrant_type=client_credentials -dscope=any -k`


**N�idisvastus:**

`{"access_token":"5405ac0b-7057-413f-8120-9b87e24860de","token_type":"bearer","expires_in":86399,"scope":"any"}`


## Message


**1. Hankida s�numite loend**

See API v�imaldab saada s�numite loendi. Selle API-ga on saadaval lehek�ljed ja sorteerimine.

URL: https://[host]:[port]/admin/data/mailbox_db/messages
Method: GET

**P�ring**

P�ringuks on andmestruktuur, mis sisaldab eeltingimusi s�numiloendi valimiseks:

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|page|Ei|integer|Lehek�ljenumber, vaikimisi on see �0�|
|size|Ei|integer|Kirjete arv lehel, vaikimisi on see 10|
|sort|Ei|string|Sorteerimise omaduse nime saab pakkuda siin|
|direction|Ei|string|Sorteerimise suund: 'ASC' - suurenevaks v�i 'DESC' - kahanevaks sorteerimiseks|


**Vastus**

Eduka p�ringu korral vastab s�steem andmestruktuuriga, mis sisaldab s�numite loetelu.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|content|Jah|complex|S�numi �ksikasjade olem|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu s�numi ID|
|type|Jah|string|S�numi t��p. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|S�numi teema/pealkiri|
|createdOn|Jah|string|S�numi loomise kuup�ev ja kellaaeg. Kinnituss�numi korral t�histab see kuup�ev ja kellaaeg s�numi avamise ajatemplit|
|unread|Jah|boolean|M�rge, n�itamaks, kas s�num on kasutaja poolt loetud v�i lugemata. Vaikev��rtus on 'true'|
|related|Ei|string|Seotud s�numi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|body|Ei|complex|S�numi sisu olem|
|id|Jah|string|S�numi sisu kordumatu ID|
|text|Jah|string|S�numi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud s�numi DigiDoc konteineri fail.|
|id|Jah|string|Faili kordumatu ID UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|S�numi manuse failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|
|totalPages|Jah|integer|Lehek�lgede koguarv|
|totalElements|Jah|integer|Kirjete koguarv vastavalt p�ringtingimustele|
|size|Jah|integer|Kirjete arv lehel|
|number|Jah|integer|Lehek�ljenumber|


**Vead**

V�imalikud vead:

- invalid_token � esitatud juurdep�suluba (token) on kehtetu.


**N�idisp�ring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/messages?page=1&size=3&sort=createdOn&direction=ASC" -H "accept: application/json" -H "Authorization: Bearer f4a02f3c-f1e8-4fcf-988e-b4f52e8f41b1"

**N�idisvastus:**

{
  "content": [
    {
      "id": "7a267603-f0c3-45aa-9ba1-a7987b904d5c",
      "type": "SIMPLE",
      "subject": "Hemwati Nandan Bahuguna Garhwal University",
      "createdOn": "2019-06-11T00:00:00Z",
      "unread": false,
      "related": null,
      "senderUserId": "EE51001091072",
      "sender": "EE51001091072",
      "receiver": "EE60001019906",
      "body": null,
      "sign": null,
      "attachments": []
    },
    {
      "id": "13b705d3-7865-4993-b36f-0e032ad67230",
      "type": "SIMPLE",
      "subject": "I-Shou University",
      "createdOn": "2019-06-11T00:00:00Z",
      "unread": false,
      "related": null,
      "senderUserId": "EE60001019906",
      "sender": "EE60001019906",
      "receiver": "EE51001091072",
      "body": null,
      "sign": null,
      "attachments": []
    },
    {
      "id": "8332c2be-0132-4ab5-8f46-7d1d0016475a",
      "type": "SIMPLE",
      "subject": "Grinnell College",
      "createdOn": "2019-06-11T00:00:00Z",
      "unread": true,
      "related": null,
      "senderUserId": "EE60001019906",
      "sender": "EE60001019906",
      "receiver": "EE51001091072",
      "body": null,
      "sign": null,
      "attachments": []
    }
  ],
  "totalPages": 380,
  "totalElements": 1140,
  "size": 3,
  "number": 1
}


**2. Hankida s�numi �ksikasjad**

See API v�imaldab saada s�numi �ksikasju s�numi ID abil.

URL: https://[host]:[port]/admin/data/mailbox_db/messages/{id}
Method: GET

**P�ring**

T�psustatud s�numi �ksikasjade saamiseks tuleb p�ringes esitada kordumatu s�numi ID.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduse poolt loodud unikaalne s�numi ID|


**Vastus**

Eduka p�ringu korral vastab s�steem andmestruktuuriga, mis sisaldab konkreetseid s�numi �ksikasju.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu s�numi ID|
|type|Jah|string|S�numi t��p. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|S�numi teema/pealkiri|
|createdOn|Jah|string|S�numi loomise kuup�ev ja kellaaeg. Kinnituss�numi korral t�histab see kuup�ev ja kellaaeg s�numi avamise ajatemplit|
|unread|Jah|boolean|M�rge, n�itamaks, kas s�num on kasutaja poolt loetud v�i lugemata. Vaikev��rtus on 'true'|
|related|Ei|string|Seotud s�numi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|body|Ei|complex|S�numi sisu olem|
|id|Jah|string|S�numi sisu kordumatu ID|
|text|Jah|string|S�numi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud DigiDoc konteineri fail|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|S�numi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

V�imalikud vead:

- invalid_token � esitatud juurdep�suluba (token) on kehtetu.


**N�idisp�ring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/messages/d58fe565-8560-43b8-be31-2041eaaa58c3" -H "accept: application/json" -H "Authorization: Bearer f4a02f3c-f1e8-4fcf-988e-b4f52e8f41b1"


**N�idisvastus:**

{
  "id": "d58fe565-8560-43b8-be31-2041eaaa58c3",
  "type": "SIMPLE",
  "subject": "asdfasdfsd",
  "createdOn": "2020-06-11T12:39:45.976267Z",
  "unread": true,
  "related": null,
  "senderUserId": "EE60001019906",
  "sender": "EE60001019906",
  "receiver": "000000000",
  "body": {
    "id": "dd042d06-6849-4c4d-819e-04ceac1ee2aa",
    "text": "asdfasdfasdfasdf"
  },
  "sign": null,
  "attachments": []
}


**3. Lisa s�num**

See API v�imaldab salvestada uue s�numi andmebaasi.

URL: https://[host]:[port]/admin/data/mailbox_db/messages
Method: POST

**P�ring**

P�ringuks on andmestruktuur, mis sisaldab andmebaasi lisatava s�numi �ksikasju.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|type|Jah|string|S�numi t��p. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|S�numi teema/pealkiri|
|createdOn|Jah|string|S�numi loomise kuup�ev ja kellaaeg. Kinnituss�numi korral t�histab see kuup�ev ja kellaaeg s�numi avamise ajatemplit|
|unread|Ei|boolean|M�rge, n�itamaks, kas s�num on kasutaja poolt loetud v�i lugemata. Vaikev��rtus on 'true'|
|related|Ei|string|Seotud s�numi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|body|Ei|complex|S�numi sisu olem|
|text|Ei|string|S�numi sisu|
|attachments|Ei|complex|S�numi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|

**Vastus**

Eduka p�ringu korral vastab s�steem andmestruktuuriga, mis sisaldab uusi s�numi �ksikasju.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu s�numi ID|
|type|Jah|string|S�numi t��p. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|S�numi teema/pealkiri|
|createdOn|Jah|string|S�numi loomise kuup�ev ja kellaaeg. Kinnituss�numi korral t�histab see kuup�ev ja kellaaeg s�numi avamise ajatemplit|
|unread|Jah|boolean|M�rge, n�itamaks, kas s�num on kasutaja poolt loetud v�i lugemata. Vaikev��rtus on 'true'|
|related|Ei|string|Seotud s�numi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) v�i isikukood (eraisiku puhul). (Tarast v�i AAR-ist)|
|body|Ei|complex|S�numi sisu olem|
|id|Jah|string|S�numi sisu kordumatu ID|
|text|Jah|string|S�numi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud DigiDoc konteineri fail|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|S�numi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

V�imalikud vead:

- DataIntegrityViolationException � kui m�ni kohustuslik v�li p�ringus puudub
- invalid_token � esitatud juurdep�suluba (token) on kehtetu


**N�idisp�ring:**

curl -X POST "https://10.1.19.35/admin/data/mailbox_db/messages" -H "accept: application/json" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118" -H "Content-Type: application/json" -d "{\"type\":\"SIMPLE\",\"subject\":\"Lorem ipsum dolor1\",\"createdOn\":\"2020-06-17T11:31:25.219185+03:00\",\"unread\":true,\"sender\":\"EE39407120044\",\"senderUserId\":\"7120022\",\"receiver\":\"EE39407120055\",\"body\":{\"text\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque rutrum consequat massa.2\"},\"attachments\":[{\"id\":\"8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1\"}]}"


**N�idisvastus:**

{
  "id": "951ba4d6-3d1c-4f1f-9530-39b5df3a2f08",
  "type": "SIMPLE",
  "subject": "Lorem ipsum dolor1",
  "createdOn": "2020-06-17T08:31:25.219185Z",
  "unread": true,
  "related": null,
  "senderUserId": "7120022",
  "sender": "EE39407120044",
  "receiver": "EE39407120055",
  "body": {
    "id": "d642e613-69e5-4463-9ef1-44b16bc3a013",
    "text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque rutrum consequat massa.2"
  },
  "sign": null,
  "attachments": [
    {
      "id": "8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1",
      "name": null,
      "type": null,
      "externalId": null
    }
  ]
}


**4. Faili �leslaadimine**

Antud API v�imaldab failide �lesse laadimise Hadoop HDFS hoidlasse ja �lesse laetud failide �ksikasjade hankimise. 

URL: https://[host]:[port]/admin/data/mailbox_db/files/upload
Method: POST

**P�ring**

MIME Multipart formaadis faili andmed, mida kasutatakse faili �lesse laadimisel:

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|file|Jah|string|MIME Multipart formaadis faili andmed|


**Vastus**

Eduka p�ringu korral vastab s�steem andmestruktuuriga, mis sisaldab Hadoop hoidlasse �lesse laetud faili detaile.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME t��p|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

V�imalikud vead:

- DataIntegrityViolationException � kui m�ni kohustuslik v�li p�ringus puudub
- invalid_token � esitatud juurdep�suluba (token) on kehtetu


**N�idisp�ring:**

curl -X POST "https://10.1.19.35/admin/data/mailbox_db/files/upload" -H "accept: application/json" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118" -H "Content-Type: multipart/form-data" -F "file=@reply1.xml;type=text/xml"


**N�idisvastus:**

{
  "id": "8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1",
  "name": "reply1.xml",
  "type": "text/xml",
  "externalId": "/mailbox/a1fe5d76-4c18-4787-8948-8348aa30c8c6.xml"
}



**5. Hangi fail**

Antud API v�imaldab faili allalaadimise selle UUID j�rgi.

URL: https://[host]:[port]/admin/data/mailbox_db/files/{fileId}
Method: GET

**P�ring**

Unikaalne faili identifikaator peab olema pakutud faili allalaadimiseks:

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|


**Vastus**

Eduka p�ringu korral vastab s�steem Hadoop hoidlasse �les laetud failiga, nii nagu tegu oleks HTTP GET p�ringuga faili otsesel allalaadimisel.

|Nimi|Kohustuslik|T��p|Kirjeldus
|--|--|--|--|


**Vead**

V�imalikud vead:

- invalid_token � esitatud juurdep�suluba (token) on kehtetu


**N�idisp�ring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/files/8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1" -H "accept: */*" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118"


**N�idisvastus:**

Vastusp�ringu p�ised:
cache-control: no-cacheno-storemax-age=0must-revalidate
connection: keep-alive
content-disposition: attachment; filename="reply1.xml"
content-encoding: gzip
content-type: text/xml
date: Wed17 Jun 2020 14:50:31 GMT
expires: 0
pragma: no-cache
server: nginx/1.14.0 (Ubuntu)
strict-transport-security: max-age=31536000 ; includeSubDomains
transfer-encoding: chunked
vary: OriginAccess-Control-Request-MethodAccess-Control-Request-Headers
x-content-type-options: nosniff
x-frame-options: DENY
x-xss-protection: 1; mode=block

Vastusp�ringu keha: soovitud faili sisu