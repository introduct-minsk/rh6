import pytest
import allure
from time import time

from requests import get
from selene.api import browser, be
from selenium import webdriver
from core.project import project
from src.web import app
from core.project_config import ProjectConfig
from logging import ERROR
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.firefox.options import Options as FOptions
from selenium.webdriver.firefox.firefox_profile import FirefoxProfile
from webdriver_manager.chrome import ChromeDriverManager
from webdriver_manager.firefox import GeckoDriverManager
from selenium.webdriver.remote.remote_connection import LOGGER


@pytest.fixture(scope="session")
def app_web_host(environment, web_host):
    if web_host:
        return web_host
    return ProjectConfig(environment).web_host


@allure.title("setup: web driver")
@pytest.fixture()
def web_driver(docker, selenoid_host, browser_name, browser_version, vnc, video):
    if docker:
        driver = get_selenoid_web_driver(selenoid_host, browser_name, browser_version, vnc, video)
    elif browser_name == "firefox":
        driver = get_firefox_web_driver()
    else:
        driver = get_chrome_web_driver(is_headless=False)
    browser.set_driver(driver)
    yield browser.driver()
    browser.quit()


def get_selenoid_web_driver(selenoid_host, browser_name, browser_version, vnc, video):
    capabilities = {
        "browserName": browser_name, "version": browser_version, "enableVNC": vnc,
        "enableVideo": video, "videoName": f"selenoid_video_{time()}.mp4",
        "acceptInsecureCerts": True}
    selenoid_status = get(url=f"http://{selenoid_host}:4444/status")
    project.logger.debug(f"Selenoid status: {selenoid_status.text}")
    driver = webdriver.Remote(
        command_executor=f"http://{selenoid_host}:4444/wd/hub",
        desired_capabilities=capabilities)
    driver.set_window_size(width="1440", height="900")
    return driver


def get_chrome_web_driver(is_headless: bool = True):
    options = Options()
    options.set_capability('goog:loggingPrefs', {'performance': 'ALL'})
    options.set_capability('acceptInsecureCerts', True)
    options.add_argument("--disable-infobars")
    options.add_argument("--disable-notifications")
    options.add_argument("--allow-running-insecure-content")
    options.add_argument("--ignore-certificate-errors")
    options.add_argument("--unsafely-treat-insecure-origin-as-secure")
    options.add_argument("--allow-insecure-localhost")
    options.add_experimental_option("useAutomationExtension", False)
    options.add_experimental_option(
        "prefs", {
            "credentials_enable_service": False,
            "profile.password_manager_enabled": False
        }
    )
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.headless = is_headless
    LOGGER.setLevel(ERROR)
    driver = webdriver.Chrome(
        executable_path=ChromeDriverManager().install(),
        options=options
    )
    return driver


def get_firefox_web_driver():
    options = FOptions()
    options.set_capability('goog:loggingPrefs', {'performance': 'ALL'})
    options.add_argument("--disable-infobars")
    options.add_argument("--disable-notifications")
    profile = FirefoxProfile()
    profile.accept_untrusted_certs = True
    LOGGER.setLevel(ERROR)
    driver = webdriver.Firefox(
        executable_path=GeckoDriverManager().install(),
        options=options,
        firefox_profile=profile
    )
    return driver


@pytest.fixture()
def open_home_page(web_driver, app_web_host):
    browser.open_url(app_web_host)
    app.login_page.container.should(be.visible)
    app.login_page.header.en_lang.click()
