from time import sleep
from selene.api import browser, be

from selene.elements import SeleneElement


def sleep_and_click_element(element: SeleneElement, before_timeout: float = 1.0, after_timeout: float = 0.0):
    sleep(before_timeout)
    scroll_into_view(element)
    element.should(be.clickable).click()
    sleep(after_timeout)


def scroll_into_view(element: SeleneElement):
    browser.execute_script("arguments[0].scrollIntoView(false);", element.get_actual_webelement())
