import allure
from selene.api import s, by, have, browser, be

from src.web.pages.mailbox.content.header import Header
from src.web.pages.mailbox.content.tab_inbox import InboxTab
from src.web.pages.mailbox.content.tab_new_message import NewMessageTab
from src.web.pages.mailbox.content.tab_outbox import OutboxTab


class MailBoxPage:
    _container_locator = '//*[contains(@class, "home-page-root")]'

    def __init__(self):
        self.container = s(by.xpath(self._container_locator))
        self.header = Header()
        self.inbox_tab = InboxTab()
        self.outbox_tab = OutboxTab()
        self.new_message_tab = NewMessageTab()

    @allure.step("Wait mail box content is loaded")
    def wait_page_has_loaded(self):
        browser.wait_to(have.url_containing('/mailbox/'), timeout=90)
        self.container.should(be.visible)
