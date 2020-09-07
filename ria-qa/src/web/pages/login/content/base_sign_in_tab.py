from selene.elements import SeleneElement


class BaseSignInTab:

    def __init__(self):
        self.tab: SeleneElement = NotImplementedError

    def go_to_tab(self):
        self.tab.click()
