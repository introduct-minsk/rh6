from selene.api import s, by

from src.web.pages.mailbox.content.tab_inbox import InboxTab


class OutboxTab(InboxTab):
    def __init__(self):
        super().__init__()
        self.container = s(by.xpath('(//*[@class="message-list-root"])[2]'))
        self.tab = s(by.xpath('(//*[@class="nav nav-tabs"]//li)[2]'))
