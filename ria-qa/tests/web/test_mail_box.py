import allure
import pytest
import src.web.app as web_app
from datetime import datetime
from faker import Faker

from constants.constants import Language
from test_data.users.users import Users
from selene.api import be, have

time_fmt = '%d-%m-%Y %H:%M'
date = datetime.now()
subject = f"Auto test {date.strftime(time_fmt)}"
message_body = Faker('en_US').name()


@pytest.fixture(params=[Users.user_with_mobile_id], ids=["Sender: user with Mobile ID"])
def sender(request):
    yield request.param


@pytest.fixture(params=[Users.user_with_smart_id], ids=["Receiver: user with Smart ID"])
def receiver(request):
    yield request.param


@allure.feature("Mail page")
class TestMailPage:

    @allure.story("User is able to send message")
    @pytest.mark.usefixtures("open_home_page")
    def test_user_is_able_to_send_message(self, sender, receiver):
        web_app.login_page.mobile_id_tab.go_to_tab()
        web_app.login_page.mobile_id_tab.login(
            personal_code=sender.personal_code,
            phone=sender.mobile_phone
        )
        web_app.mailbox_page.wait_page_has_loaded()
        web_app.mailbox_page.header.lang_dropdown.set_lang(Language.en)
        web_app.mailbox_page.new_message_tab.go_to_tab()
        web_app.mailbox_page.new_message_tab.submit_form(
            to=Users.user_with_smart_id.personal_code,
            subject=f"{subject} from {sender.personal_code} to {receiver.personal_code}",
            text=message_body
        )
        web_app.mailbox_page.outbox_tab.wait_content_has_loaded()

    @allure.story("User is able to see sent message in outbox")
    @pytest.mark.usefixtures("open_home_page")
    def test_user_is_able_to_see_sent_message_in_outbox(self, sender, receiver):
        msg_subject = f"{subject} from {sender.personal_code} to {receiver.personal_code}"
        web_app.login_page.mobile_id_tab.go_to_tab()
        web_app.login_page.mobile_id_tab.login(
            personal_code=sender.personal_code,
            phone=sender.mobile_phone
        )
        web_app.mailbox_page.wait_page_has_loaded()
        web_app.mailbox_page.header.lang_dropdown.set_lang(Language.en)
        web_app.mailbox_page.outbox_tab.go_to_tab()
        message = web_app.mailbox_page.outbox_tab.get_message_by_text(msg_subject)
        message.title.should(have.text(msg_subject))
        message_view = message.see_message()
        message_view.subject.should(have.text(msg_subject))
        message_view.details.should(have.text("To: OSKAR RIATEST (EE10101010005)"))
        message_view.check_timestamp(date)
        message_view.body.should(have.text(message_body))

    @allure.story("User is able to receive message")
    @pytest.mark.usefixtures("open_home_page")
    def test_user_is_able_to_receive_sent_message(self, sender, receiver):
        msg_subject = f"{subject} from {sender.personal_code} to {receiver.personal_code}"
        web_app.login_page.smart_id_tab.go_to_tab()
        web_app.login_page.smart_id_tab.login(
            personal_code=receiver.personal_code,
        )
        web_app.role_page.wait_page_has_loaded()
        web_app.role_page.select_top_role()
        web_app.mailbox_page.wait_page_has_loaded()
        web_app.mailbox_page.header.lang_dropdown.set_lang(Language.en)
        web_app.mailbox_page.inbox_tab.container.should(be.visible)
        message = web_app.mailbox_page.inbox_tab.get_message_by_text(msg_subject)
        message.title.should(have.text(msg_subject))
        message.is_unread(True)
        message_view = message.see_message()
        message_view.subject.should(have.text(msg_subject))
        message_view.details.should(have.text("From: MARY ÄNN O'CONNEŽ-ŠUSLIK"))
        message_view.check_timestamp(date)
        message_view.body.should(have.text(message_body))
        message_view.back_btn.click()
        message.is_unread(False)

    @allure.story("User is able to receive 'Read your message' email")
    @pytest.mark.usefixtures("open_home_page")
    def test_user_is_able_to_receive_read_your_message_email(self, sender, receiver):
        read_msg_subject = f'The user EE{receiver.personal_code} has read your message'
        web_app.login_page.mobile_id_tab.go_to_tab()
        web_app.login_page.mobile_id_tab.login(
            personal_code=sender.personal_code,
            phone=sender.mobile_phone
        )
        web_app.mailbox_page.wait_page_has_loaded()
        web_app.mailbox_page.header.lang_dropdown.set_lang(Language.en)
        web_app.mailbox_page.inbox_tab.container.should(be.visible)
        message = web_app.mailbox_page.inbox_tab.get_message_by_text(read_msg_subject)
        message.title.should(have.text(read_msg_subject))
        message.is_unread(True)
        message_view = message.see_message()
        message_view.subject.should(have.text(read_msg_subject))
        message_view.details.should(have.text("From: OSKAR RIATEST"))
        message_view.details.should(have.text(f"{date.strftime('%d.%m.%Y')}"))
        message_view.check_timestamp(date)
        message_view.body.should(
            have.text("The user OSKAR RIATEST has read your message"))
        message_view.back_btn.click()
        message.is_unread(False)
