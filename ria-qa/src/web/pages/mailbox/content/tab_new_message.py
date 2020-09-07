import allure
from selene.api import s, by, be
from src.web.pages.mailbox.content.base_tab import BaseTab


class NewMessageTab(BaseTab):
    def __init__(self):
        super().__init__()
        self._container_locator = '//*[@class="the-mailbox-root content"]//*[@class="container"]'
        self.tab = s(by.xpath('(//*[@class="nav nav-tabs"]//li)[3]'))
        self.to_input = s(by.xpath(f'({self._container_locator}//input)[1]'))
        self.subject_input = s(by.xpath(f'({self._container_locator}//input)[2]'))
        self.text_area = s(by.xpath(f'{self._container_locator}//textarea'))
        self.attach_files_btn = s(by.xpath(
            f'{self._container_locator}//i[contains(@class, "material-icons-outlined")]'
            f'/ancestor::button'))
        self.sign_with_id_card_btn = s(by.xpath(
            f'{self._container_locator}//*[contains(@class, "sign-icon")]/ancestor::button'))
        self.send_message_btn = s(by.xpath(
            f'{self._container_locator}//button[@variant="primary"][2]'))

    @allure.step("Submit new message form")
    def submit_form(self, to: str, subject: str, text: str):
        self.fill_form(to=to, subject=subject, text=text)
        self.send_message_btn.click()

    @allure.step("Fill in new message form with receiver data, subject, text")
    def fill_form(self, to: str, subject: str, text: str):
        self.container.should(be.visible)
        self.to_input.set_value(to)
        self.subject_input.set_value(subject)
        self.text_area.set_value(text)
