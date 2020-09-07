import allure
from selene.api import s, by

from src.web.pages.login.content.base_sign_in_tab import BaseSignInTab


class MobileIdTab(BaseSignInTab):
    _container_locator = '//*[@data-tab="mobile-id"]'

    def __init__(self):
        super().__init__()
        self.container = s(by.xpath(self._container_locator))
        self.tab = s(by.xpath('//a[@data-tab="mobile-id"]'))
        self.personal_code_input = s(by.xpath(
            f'{self._container_locator}//input[@id="mid-personal-code"]'))
        self.phone_input = s(by.xpath(f'{self._container_locator}//input[@id="mid-phone-number"]'))
        self.continue_btn = s(by.xpath(
            '//*[@id="mobileIdForm"]//button[contains(@class, "c-btn c-btn--primary")]'))

    @allure.step("Submit login form with mobile ID")
    def login(self, personal_code: str, phone: str):
        self.fill_form(personal_code=personal_code, phone=phone)
        self.continue_btn.click()

    @allure.step("Fill in login form with mobile ID")
    def fill_form(self, personal_code: str, phone: str):
        self.personal_code_input.set_value(personal_code)
        self.phone_input.set_value(phone)
