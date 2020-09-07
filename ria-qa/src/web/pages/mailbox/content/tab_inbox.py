from selene.api import s, by

from src.web.pages.mailbox.content.base_tab import BaseMessageTab


class InboxTab(BaseMessageTab):

    def __init__(self):
        super().__init__()
        self.container = s(by.xpath('(//*[@class="message-list-root"])[1]'))
        self.tab = s(by.xpath('(//*[@class="nav nav-tabs"]//li)[1]'))
