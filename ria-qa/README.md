# 1. Selenium automaattestide käivitamise juhend
Kasutajaliides on kaetud minimaalse hulga automaattestidega.
Automaattestide teostatus.
Automaattestid katavad järgmise funktsionaalsuse:
- Kasutaja sisselogimine (kasutaja 1)
- Kasutaja poolt teisele kasutajale (kasutaja 2) sõnumi saatmine
- Kasutaja sisselogimine (kasutaja 2)
- Kasutaja poolt sõnumi lugemine (kasutaja 2)
- Kasutaja poolt (kasutaja 1) automaatse lugemise kinnituse vastuvõtt

Automaattestid on realiseeritud eraldi projektina, mida tarnitakse koos prooviülesandega.
Automaattestide projekt koos testimisjuhendiga on saadaval ka [lähtekoodi repositaariumist](https://10.0.9.217/projects/RH6/repos/rh6/ria-qa)

# 2. Paigaldus (ilma Docker-ita)
1. Paigaldage Python versioon 3.7 või kõrgem. [Laadige alla](https://www.python.org/downloads/) ja tehke see käivitatavaks. Selle tulemusena peaks terminalis käsk "python --version" töötama ja tagastama python3 versiooni, nt. ** Python 3.7.4 **
2. Paigaldage [poetry](https://python-poetry.org/docs/#installation) pakett . Andke terminalis käsk "poetry". Kontrollige paigaldust ja käivitage terminalis käsk ```poetry --version```. Selle tulemusena peaks käsk tagastama Poetry versiooni, nt. **Poetry version 1.0.5**
3. Paigaldage allure [juhendi järgi](https://docs.qameta.io/allure/#_installing_a_commandline). Selle tulemusena peaks terminalis käsk `allure --version` töötama  ja tagastama allure versiooni.
4. Paigaldage projekti sõltuvused, selleks minge terminalis kausta ** ria-qa ** ja käivitage installimiskäsk ```poetry install```. See peaks tekitama virtualenvi, mida tähistab umbes sarnane väljund käsust: **Creating virtualenv ria-qa-DCOggpqd-py3.7 in /Users/user/Library/Caches/pypoetry/virtualenvs**. Jätke see rida meelde, reas määratud kausta läheb Teil tarvis järgmises sammus.
5. Aktiveerige loodud virtualenv, käivitage: `source {path_to_venv_folder}/bin/activate`. Lisainfo virtualenvi käivitamise/desaktiveerimise kohta, vastavalt operatsioonisüsteemile, on leitav [siit](https://uoa-eresearch.github.io/eresearch-cookbook/recipe/2014/11/26/python-virtual-env/).

# 3. UI automaatestide seadistus Dockeris
1. Minge kausta "ria-qa" ja käivitage käsk terminalis "docker-compose up -d". Dockeri konteinerid koos **selenoid**, **selenoid-ui**, **ria-qa_selenoid-chrome-81_1** peaksid olema sellega loodud.
2. Kontrollige, et selenoid-ui on kättesaadav brauserist [http://localhost:8082/](http://localhost:8082/)
3. Kontrollige selenoidi staatust [http://localhost:8082/status](http://localhost:8082/status)

# 4. Testide käivitamine
1. Testide käivitamiseks andke terminalis käsk ```pytest tests/web/test_mail_box.py --selenoid_host=localhost --web_host={application_web_host}```

# 5. Tulemuste raporteerimine
1. Looge allure raport ja avage see brauseris. Selleks käivitage terminalis: `allure generate {path_to_repository_folder}/public/allure-report --clean && allure open` olles `ria-qa` kaustas.

# 6. Kõik sammud koos Dockeri keskkonnas käivitamiseks
1. Olles `ria-qa` kaustas, andke käsk `docker-compose up -d`, et käivitada konteinerid selenoidi ja brauseriga
2. Andke käsk `docker build -t ria_qa . --force-rm`, et ehitada Dockeri kujund Python 3.7-ga ja kõikide paketi sõltuvustega, mida sellega paigaldatakse automaatselt ria-qa hoidla Dockerfile´ist.
3. Andke käsk `docker run --name ria_qa_container ria_qa poetry run pytest --selenoid_host={selenoid_host} --web_host={application_web_host}` testide käivitamiseks, kus {selenoid_host} peab olema masina IP aadress, kus jookseb Selenoid (vt. eelmised sammud) ja mis on kättesaadav ria_qa_container Dokkeri konteinerist. Juhul, kui selleks on sama masin (nagu meie näidetes) ja sama Dokkeri instants, siis väärtust võib seada "host.docker.internal"-iks või masina IP aadressiks.
4. Allure tuleks paigaldada arvutisse ja see peaks olema käivitatav, kui on soov saada graafilist aruandlust testide jooksutamisest. Allure´i tulemused tuleks koguda ria_qa_container-ist (public/allure-report). 
5. Tulemuste vaatamiseks brauserist, käituge nii nagu on kirjeldatud **5. Tulemuste raporteerimine** sektsioonis.
