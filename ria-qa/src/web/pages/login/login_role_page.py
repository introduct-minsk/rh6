import allure
from selene.api import s, by, be


class LogInRolePage:
    _container_locator = '//*[contains(@class, "card-body")]'

    def __init__(self):
        self.container = s(by.xpath(self._container_locator))
        self.log_out_link = s(by.xpath(
            f'{self._container_locator}//*[contains(@class, "logout")]//a[@href="/logout"]'))
        self.role_1 = self._get_role_by_index(1)
        self.role_2 = self._get_role_by_index(2)

    def select_top_role(self):
        self.select_role(index=1)

    def select_role(self, index: int):
        role = self._get_role_by_index(index)
        role.should(be.visible)
        role.click()
        role.should_not(be.visible)

    @staticmethod
    def _get_role_by_index(index: int):
        return s(by.xpath(f'(//*[contains(@class, "list-group")]//button)[{index}]'))

    @allure.step("Wait select role screen is loaded")
    def wait_page_has_loaded(self):
        self.container.should(be.visible)
