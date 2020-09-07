import allure
from selene.api import s, by

from src.web.pages.login.content.base_sign_in_tab import BaseSignInTab


class SmartIdTab(BaseSignInTab):
    _container_locator = '//*[@data-tab="smart-id"]'

    def __init__(self):
        super().__init__()
        self.container = s(by.xpath(self._container_locator))
        self.tab = s(by.xpath('//a[@data-tab="smart-id"]'))
        self.personal_code_input = s(by.xpath(
            f'{self._container_locator}//input[@id="sid-personal-code"]'))
        self.continue_btn = s(by.xpath(
            '//*[@id="smartIdForm"]//button[contains(@class, "c-btn c-btn--primary")]'))

    @allure.step("Submit login form with smart ID")
    def login(self, personal_code: str):
        self.fill_form(personal_code=personal_code)
        self.continue_btn.click()

    @allure.step("Fill in login form with smart ID")
    def fill_form(self, personal_code: str):
        self.personal_code_input.set_value(personal_code)
