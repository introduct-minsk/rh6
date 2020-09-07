import allure
from selene.api import s, by

from constants.constants import Language
from helpers.utils import sleep_and_click_element


class Header:
    _container_locator = '//*[contains(@class, "the-header-root")]'

    def __init__(self):
        self.lang_dropdown = _LangDropdown(header_locator=self._container_locator)


class _LangDropdown:

    def __init__(self, header_locator: str):
        self._locator = f'({header_locator}//*[contains(@class, "menu-dropdown-root")])[1]'
        self.button = s(by.xpath(f'{self._locator}//button'))
        self.current_lang = s(by.xpath(f'{self._locator}//button//*[@class="value"]'))
        self.en_lang = s(by.xpath(
            '(//ul//a[contains(@class, "dropdown-item")][contains(text(), "ENG")])[1]'))
        self.est_lang = s(by.xpath(
            '(//ul//a[contains(@class, "dropdown-item")][contains(text(), "EST")])[1]'))

    @allure.step("Set language")
    def set_lang(self, lang: str):
        if lang.lower() != self.current_lang.text.lower():
            sleep_and_click_element(self.button, after_timeout=1)
            if lang == Language.en:
                sleep_and_click_element(self.en_lang)
            else:
                sleep_and_click_element(self.est_lang)
