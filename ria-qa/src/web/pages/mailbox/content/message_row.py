import allure
from selene.api import s, by, have, be

from src.web.pages.mailbox.content.message_view import MessageView


class MessageRow:
    _locator = '//*[@class="message"]'

    def __init__(self, text: str = "", index: int = None):
        self._locator_by_text = f'//*[contains(text(), "{text}")]/ancestor::*[@class="message"]'
        self._locator_by_index = f'({self._locator})[{index}]'
        self._locator = self._locator_by_text if text else self._locator_by_index
        self.container = s(by.xpath(self._locator))
        self.title = s(by.xpath(f'{self._locator}//*[contains(@class, "title")]'))
        self.details = s(by.xpath(f'{self._locator}//*[@class="properties"]'))
        self.unread_indicator = s(by.xpath(
            f'{self._locator}//*[contains(@class, "unread-indicator")]'))
        self.see_message_link = s(by.xpath(f'({self._locator}//*[@class="actions"]//a)[1]'))

    @allure.step("Open message")
    def see_message(self):
        self.see_message_link.click()
        view = MessageView()
        view.container.should(be.visible)
        return view

    @allure.step("Check message read/unread state")
    def is_unread(self, state: bool):
        if state:
            self.title.should(have.css_class('unread'))
            self.unread_indicator.should(have.css_class('active'))
        else:
            self.title.should_not(have.css_class('unread'))
            self.unread_indicator.should_not(have.css_class('active'))
