


## Postkasti Teenus API (administraatori ligipääs)

API eesmärk on võimaldada tugimeeskonnal hallata postkasti sõnumeid (masslugemine, masslaadimine), manuseid (masslugemine, masslaadimine) ja kasutajakontosid. 
Autentimine on nõutud, et saada juurdepääs postkasti teenuse administreerimise API-le. 

## Administraatori ligipääs
Administraatori kontod on kirjeldatud andmebaasis.
Uue administraatori konto lisamine käib andmebaasi kaudu.
Vaiketarnes, rakendust tarnitakse ühe vaikeadministraatori kontoga andmebaasis.

Autentimiseks palun kasutage:

`client_id: admin-client`
`client_secret: mailboxpassword`

## Autentimine
Kõik administreerimise API liidesed nõuavad eelnevat administraatori autentimist ja valiidse sessiooni tokeni olemasolu. Tokeni saab eduka autentimise tulemusel (autentimisprotokoll on OAuth2).
Autentimine toimub eraldi mikroteenuse poole pöördumisel, eduka autentimise tulemusel väljastatakse administraatorile kehtiv sessiooni token, mida tuleb hilisemate administraatori API teenuste pöördumisel määrata päringu päisetes HTTP Basic skeemil (näited edasi):

URL: https://[host]:[port]/oauth/token
Test keskkonna näide: [https://10.1.19.35/oauth/token](https://10.1.19.35/oauth/token)


**Päring**

See päring võimaldab administraatoril ennast autentida ning omandada sessiooni token edasiste administreerimistegevuste teostamiseks. Autentimispäring sisaldab kasutajatunnust ja parooli: client_id ja client_secret ja mõnda täiendavat omadust.
_Administraatori konto peab olema andmebaasis eeldefineeritud (tarne teostatakse vaikimisi ühe administaatori kontoga)._

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|client_id|Jah|string|Administraatori kasutaja juurdepääsu ID|
|client_secret|Jah|string|Administraatori kasutaja juurdepääsu parool|
|grant_type|Jah|string|Väärtus peab alati olema "client_credentials"|
|scope|Ei|string|Ankur edasise arenduse võimaldamiseks: antud väli on sisuliselt autoriseerimise (mitte segamini ajada autentimisega) määraja, mis määrab autorisatsiooni ulatust. Praeguses versioonis on ainult üks võimalus - "Any" ja see annab täieliku juurdepääsu kõigile selles dokumendis kirjeldatud administreerimise API-dele, kuid hiljem on võimalik lisada täiendavaid limiteerijaid - piirates administraatori sessiooni ulatust.|


**Vastus**

Kui autentimise päring on edukas, vastab autentimisliides juurdepääsuloaga.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|access_token|Jah|string|Juurdepääsu token, mis on väljastatud autentimisserveri poolt|
|token_type|Jah|string|Juurdepääsu tokeni tüüp, hetkel alati “bearer” |
|expires_in|Jah|integer|Aja kestus sekundites, mille vältel juurdepäsu token kehtib.|
|scope|Jah|string|Ulatus näitab andmetele juurdepääsu taset (vt. päringu sama välja kirjeldust).|


**Vead**

Võimalikud vead:

- invalid_request – Päringul puudub parameeter, nii et server ei saa päringuga jätkata. Seda võib esineda ka siis, kui päring sisaldab toetamata parameetrit või korduvat parameetrit.
- invalid_client – Kliendi autentimine nurjus, näiteks kui päring sisaldab kehtetut kliendi ID-d või parooli. Sellisel juhul HTTP vastuse kood on seatud väärtusele 401.
- invalid_grant – Välja grant_type väärtus on vale (peab alati olema "client_credentials").
- invalid_scope – Päringus sisalduv ulatuse väärtus on vale (hetkeversioonis peab alati olema "Any").
- unauthorized_client – Sellel kliendil ei ole volitust taotletud ligipääsu kasutada.
- unsupported_grant_type – Välja grant_type väärtus on vale (peab alati olema "client_credentials").


**Näidispäring:**

`curl -X POST https://admin-client:mailboxpassword@10.1.19.35/oauth/token -dgrant_type=client_credentials -dscope=any -k`


**Näidisvastus:**

`{"access_token":"5405ac0b-7057-413f-8120-9b87e24860de","token_type":"bearer","expires_in":86399,"scope":"any"}`


## Message


**1. Hankida sõnumite loend**

See API võimaldab saada sõnumite loendi. Selle API-ga on saadaval leheküljed ja sorteerimine.

URL: https://[host]:[port]/admin/data/mailbox_db/messages
Method: GET

**Päring**

Päringuks on andmestruktuur, mis sisaldab eeltingimusi sõnumiloendi valimiseks:

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|page|Ei|integer|Leheküljenumber, vaikimisi on see ´0´|
|size|Ei|integer|Kirjete arv lehel, vaikimisi on see 10|
|sort|Ei|string|Sorteerimise omaduse nime saab pakkuda siin|
|direction|Ei|string|Sorteerimise suund: 'ASC' - suurenevaks või 'DESC' - kahanevaks sorteerimiseks|


**Vastus**

Eduka päringu korral vastab süsteem andmestruktuuriga, mis sisaldab sõnumite loetelu.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|content|Jah|complex|Sõnumi üksikasjade olem|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu sõnumi ID|
|type|Jah|string|Sõnumi tüüp. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|Sõnumi teema/pealkiri|
|createdOn|Jah|string|Sõnumi loomise kuupäev ja kellaaeg. Kinnitussõnumi korral tähistab see kuupäev ja kellaaeg sõnumi avamise ajatemplit|
|unread|Jah|boolean|Märge, näitamaks, kas sõnum on kasutaja poolt loetud või lugemata. Vaikeväärtus on 'true'|
|related|Ei|string|Seotud sõnumi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|body|Ei|complex|Sõnumi sisu olem|
|id|Jah|string|Sõnumi sisu kordumatu ID|
|text|Jah|string|Sõnumi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud sõnumi DigiDoc konteineri fail.|
|id|Jah|string|Faili kordumatu ID UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|Sõnumi manuse failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|
|totalPages|Jah|integer|Lehekülgede koguarv|
|totalElements|Jah|integer|Kirjete koguarv vastavalt päringtingimustele|
|size|Jah|integer|Kirjete arv lehel|
|number|Jah|integer|Leheküljenumber|


**Vead**

Võimalikud vead:

- invalid_token – esitatud juurdepäsuluba (token) on kehtetu.


**Näidispäring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/messages?page=1&size=3&sort=createdOn&direction=ASC" -H "accept: application/json" -H "Authorization: Bearer f4a02f3c-f1e8-4fcf-988e-b4f52e8f41b1"

**Näidisvastus:**

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


**2. Hankida sõnumi üksikasjad**

See API võimaldab saada sõnumi üksikasju sõnumi ID abil.

URL: https://[host]:[port]/admin/data/mailbox_db/messages/{id}
Method: GET

**Päring**

Täpsustatud sõnumi üksikasjade saamiseks tuleb päringes esitada kordumatu sõnumi ID.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduse poolt loodud unikaalne sõnumi ID|


**Vastus**

Eduka päringu korral vastab süsteem andmestruktuuriga, mis sisaldab konkreetseid sõnumi üksikasju.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu sõnumi ID|
|type|Jah|string|Sõnumi tüüp. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|Sõnumi teema/pealkiri|
|createdOn|Jah|string|Sõnumi loomise kuupäev ja kellaaeg. Kinnitussõnumi korral tähistab see kuupäev ja kellaaeg sõnumi avamise ajatemplit|
|unread|Jah|boolean|Märge, näitamaks, kas sõnum on kasutaja poolt loetud või lugemata. Vaikeväärtus on 'true'|
|related|Ei|string|Seotud sõnumi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|body|Ei|complex|Sõnumi sisu olem|
|id|Jah|string|Sõnumi sisu kordumatu ID|
|text|Jah|string|Sõnumi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud DigiDoc konteineri fail|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|Sõnumi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

Võimalikud vead:

- invalid_token – esitatud juurdepäsuluba (token) on kehtetu.


**Näidispäring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/messages/d58fe565-8560-43b8-be31-2041eaaa58c3" -H "accept: application/json" -H "Authorization: Bearer f4a02f3c-f1e8-4fcf-988e-b4f52e8f41b1"


**Näidisvastus:**

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


**3. Lisa sõnum**

See API võimaldab salvestada uue sõnumi andmebaasi.

URL: https://[host]:[port]/admin/data/mailbox_db/messages
Method: POST

**Päring**

Päringuks on andmestruktuur, mis sisaldab andmebaasi lisatava sõnumi üksikasju.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|type|Jah|string|Sõnumi tüüp. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|Sõnumi teema/pealkiri|
|createdOn|Jah|string|Sõnumi loomise kuupäev ja kellaaeg. Kinnitussõnumi korral tähistab see kuupäev ja kellaaeg sõnumi avamise ajatemplit|
|unread|Ei|boolean|Märge, näitamaks, kas sõnum on kasutaja poolt loetud või lugemata. Vaikeväärtus on 'true'|
|related|Ei|string|Seotud sõnumi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|body|Ei|complex|Sõnumi sisu olem|
|text|Ei|string|Sõnumi sisu|
|attachments|Ei|complex|Sõnumi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|

**Vastus**

Eduka päringu korral vastab süsteem andmestruktuuriga, mis sisaldab uusi sõnumi üksikasju.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|id|Jah|string|**Postkasti Teenus** rakenduses loodud kordumatu sõnumi ID|
|type|Jah|string|Sõnumi tüüp. LOV: 'SIMPLE', 'NOTIFICATION'|
|subject|Ei|string|Sõnumi teema/pealkiri|
|createdOn|Jah|string|Sõnumi loomise kuupäev ja kellaaeg. Kinnitussõnumi korral tähistab see kuupäev ja kellaaeg sõnumi avamise ajatemplit|
|unread|Jah|boolean|Märge, näitamaks, kas sõnum on kasutaja poolt loetud või lugemata. Vaikeväärtus on 'true'|
|related|Ei|string|Seotud sõnumi ID kinnitusteavituse korral|
|sender|Jah|string|Saatja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|senderUserId|Jah|string|Saatja kordumatu isiku ID. (Tarast)|
|receiver|Jah|string|Saaja registrikood (juriidilise isiku puhul) või isikukood (eraisiku puhul). (Tarast või AAR-ist)|
|body|Ei|complex|Sõnumi sisu olem|
|id|Jah|string|Sõnumi sisu kordumatu ID|
|text|Jah|string|Sõnumi sisu teksti kujul|
|sign|Ei|complex|Allkirjastatud DigiDoc konteineri fail|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|
|attachments|Ei|complex|Sõnumi manuste failid|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

Võimalikud vead:

- DataIntegrityViolationException – kui mõni kohustuslik väli päringus puudub
- invalid_token – esitatud juurdepäsuluba (token) on kehtetu


**Näidispäring:**

curl -X POST "https://10.1.19.35/admin/data/mailbox_db/messages" -H "accept: application/json" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118" -H "Content-Type: application/json" -d "{\"type\":\"SIMPLE\",\"subject\":\"Lorem ipsum dolor1\",\"createdOn\":\"2020-06-17T11:31:25.219185+03:00\",\"unread\":true,\"sender\":\"EE39407120044\",\"senderUserId\":\"7120022\",\"receiver\":\"EE39407120055\",\"body\":{\"text\":\"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque rutrum consequat massa.2\"},\"attachments\":[{\"id\":\"8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1\"}]}"


**Näidisvastus:**

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


**4. Faili üleslaadimine**

Antud API võimaldab failide ülesse laadimise Hadoop HDFS hoidlasse ja ülesse laetud failide üksikasjade hankimise. 

URL: https://[host]:[port]/admin/data/mailbox_db/files/upload
Method: POST

**Päring**

MIME Multipart formaadis faili andmed, mida kasutatakse faili ülesse laadimisel:

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|file|Jah|string|MIME Multipart formaadis faili andmed|


**Vastus**

Eduka päringu korral vastab süsteem andmestruktuuriga, mis sisaldab Hadoop hoidlasse ülesse laetud faili detaile.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|
|name|Jah|string|Faili nimi|
|type|Jah|string|Faili MIME tüüp|
|externalId|Jah|string|Faili ID Hadoopis|


**Vead**

Võimalikud vead:

- DataIntegrityViolationException – kui mõni kohustuslik väli päringus puudub
- invalid_token – esitatud juurdepäsuluba (token) on kehtetu


**Näidispäring:**

curl -X POST "https://10.1.19.35/admin/data/mailbox_db/files/upload" -H "accept: application/json" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118" -H "Content-Type: multipart/form-data" -F "file=@reply1.xml;type=text/xml"


**Näidisvastus:**

{
  "id": "8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1",
  "name": "reply1.xml",
  "type": "text/xml",
  "externalId": "/mailbox/a1fe5d76-4c18-4787-8948-8348aa30c8c6.xml"
}



**5. Hangi fail**

Antud API võimaldab faili allalaadimise selle UUID järgi.

URL: https://[host]:[port]/admin/data/mailbox_db/files/{fileId}
Method: GET

**Päring**

Unikaalne faili identifikaator peab olema pakutud faili allalaadimiseks:

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|
|id|Jah|string|Faili kordumatu ID, UUID formaadis|


**Vastus**

Eduka päringu korral vastab süsteem Hadoop hoidlasse üles laetud failiga, nii nagu tegu oleks HTTP GET päringuga faili otsesel allalaadimisel.

|Nimi|Kohustuslik|Tüüp|Kirjeldus
|--|--|--|--|


**Vead**

Võimalikud vead:

- invalid_token – esitatud juurdepäsuluba (token) on kehtetu


**Näidispäring:**

curl -X GET "https://10.1.19.35/admin/data/mailbox_db/files/8a01cd3a-f73f-4b2f-85bd-20749cfc1ba1" -H "accept: */*" -H "Authorization: Bearer 421bbb7a-8a53-45c6-b8f2-8daebbeac118"


**Näidisvastus:**

Vastuspäringu päised:
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

Vastuspäringu keha: soovitud faili sisu