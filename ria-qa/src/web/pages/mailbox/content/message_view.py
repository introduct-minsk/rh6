import allure
from datetime import datetime
from selene.api import s, by, have


class MessageView:
    _locator = '//*[@class="the-mailbox-root content"]'

    def __init__(self):
        self.container = s(by.xpath(self._locator))
        self.subject = s(by.xpath(f'{self._locator}//*[contains(@class, "subject")]'))
        self.details = s(by.xpath(f'{self._locator}//*[contains(@class, "details")]'))
        self.timestamp = s(by.xpath(f'{self._locator}//*[@class="timestamp"]'))
        self.body = s(by.xpath(f'{self._locator}//*[contains(@class, "body")]'))
        self.back_btn = s(by.xpath(f'{self._locator}//button[contains(@class, "c-btn--primary")]'))

    @allure.step("Check timestamp")
    def check_timestamp(self, date: datetime):
        self.timestamp.should(have.text(date.strftime('%d.%m.%Y')))
