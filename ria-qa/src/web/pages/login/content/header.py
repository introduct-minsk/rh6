from selene.api import s, by


class Header:

    def __init__(self):
        self.en_lang = s(by.xpath('//nav//a[@lang="en"]'))
        self.ru_lang = s(by.xpath('//nav//a[@lang="ru"]'))
