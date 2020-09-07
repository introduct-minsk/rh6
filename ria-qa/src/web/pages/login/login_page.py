from selene.api import s, by

from src.web.pages.login.content.header import Header
from src.web.pages.login.content.tab_mobile_id import MobileIdTab
from src.web.pages.login.content.tab_smart_id import SmartIdTab


class LogInPage:
    _container_locator = '//*[contains(@class, "c-layout--full")]'

    def __init__(self):
        self.container = s(by.xpath(self._container_locator))
        self.header = Header()
        self.mobile_id_tab = MobileIdTab()
        self.smart_id_tab = SmartIdTab()
