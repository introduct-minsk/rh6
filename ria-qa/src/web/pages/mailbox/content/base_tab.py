import allure
from selene.api import s, by, be
from selene.elements import SeleneElement

from src.web.pages.mailbox.content.message_row import MessageRow


class BaseTab:
    _container_locator = '//*[@class="the-mailbox-root content"]'

    def __init__(self):
        self.container = s(by.xpath(self._container_locator))
        self.tab: SeleneElement = NotImplementedError

    @allure.step("Go to tab")
    def go_to_tab(self):
        self.tab.click()
        self.container.should(be.visible)


class BaseMessageTab(BaseTab):

    @staticmethod
    @allure.step("Find message by text")
    def get_message_by_text(text: str):
        return MessageRow(text=text)

    @staticmethod
    @allure.step("Find message by index")
    def get_message_by_index(index: int):
        return MessageRow(index=index)

    @allure.step("Tab content has loaded")
    def wait_content_has_loaded(self):
        self.container.should(be.visible)
