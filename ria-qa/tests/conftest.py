import os
import pytest
import allure
from json import loads, load
from selene import factory
from selene.helpers import take_screenshot
from selene.common.none_object import NoneObject
from core.project import project


@pytest.mark.hookwrapper
def pytest_runtest_makereport(item, call):
    pytest_html = item.config.pluginmanager.getplugin('html')
    outcome = yield
    report = outcome.get_result()
    extra = getattr(report, 'extra', [])
    if report.when in ('call', 'setup'):
        xfail = hasattr(report, 'wasxfail')
        if ((report.passed or report.skipped) and xfail) or (report.failed and not xfail):
            # only add additional html on failure
            try:
                if type(factory.get_shared_driver()) is not NoneObject:
                    screen = take_screenshot(webdriver=factory.get_shared_driver(),
                                             filename=item.function.__name__)
                    extra.append(pytest_html.extras.image(screen))
                    allure.attach.file(screen, item.function.__name__, allure.attachment_type.PNG)
            except Exception as e:
                print(f"Could not create a screenshot:\n{e}")
            if xfail:
                extra.append(pytest_html.extras.html(
                    '<div style="color: orange">XFailed reason: {}</div><br />'.format(
                        report.wasxfail)))
        report.extra = extra


with open(os.path.join(project.path_to_config_folder, "browsers.json")) as f:
    browser_cfg = load(f)


def pytest_addoption(parser):
    parser.addoption("--browser", action="store", default="chrome",
                     help=f"Possible browser values: chrome, firefox")
    parser.addoption("--bv", action="store",
                     default=browser_cfg.get("chrome").get("default") or "78.0",
                     help=f"Browser versions")
    parser.addoption("--vnc", action="store", default="True",
                     help=f"VNC (see browser screen) enable option. Default is on. Correct values: [True, False]")
    parser.addoption("--video", action="store", default="False",
                     help=f"Video record enable option. Default is off. Correct values: [True, False]")
    parser.addoption("--docker", action="store", default="True",
                     help=f"Run tests in selenide docker containers. Correct values: [True, False]")
    parser.addoption("--selenoid_host", action="store", default="localhost",
                     help=f"Define selenoid host or IP.")
    parser.addoption("--web_host", action="store", default=None,
                     help=f"Define application web host or IP.")


@pytest.fixture(scope="session")
def browser_name(request):
    option = request.config.getoption("--browser")
    if not option or option.lower() not in ["chrome", "firefox"]:
        raise ValueError("Specified browser is invalid. Use --help for more information")
    return option.lower()


@pytest.fixture(scope="session")
def browser_version(request):
    option = request.config.getoption("--bv")
    return option.lower()


@pytest.fixture(scope="session")
def vnc(request):
    option = request.config.getoption("--vnc")
    if option.lower() not in ["true", "false"]:
        raise ValueError("Specified vnc is invalid. Use --help for more information")
    return loads(option.lower())


@pytest.fixture(scope="session")
def video(request):
    option = request.config.getoption("--video")
    if option.lower() not in ["true", "false"]:
        raise ValueError("Specified video is invalid. Use --help for more information")
    return loads(option.lower())


@pytest.fixture(scope="session")
def docker(request):
    option = request.config.getoption("--docker")
    if option.lower() not in ["true", "false"]:
        raise ValueError("Specified docker option is invalid. Use --help for more information")
    return loads(option.lower())


@pytest.fixture(scope="session")
def selenoid_host(request):
    option = request.config.getoption("--selenoid_host")
    return option


@pytest.fixture(scope="session")
def web_host(request):
    option = request.config.getoption("--web_host")
    if option and (not option.startswith("http://") and not option.startswith("https://")):
        raise AttributeError(
            "Command line option --web_host should start from  http:// or https://")
    return option
