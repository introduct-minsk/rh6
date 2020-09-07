import allure
import pytest
from py._xmlgen import html
from datetime import datetime
from core.project_config import ProjectConfig


@pytest.mark.optionalhook
def pytest_html_results_table_header(cells):
    """
    modify the columns of test report by implementing custom hooks for the header and rows.
    Adds a sortable Timestamp column, and removes the links column:
    :param cells: report table cells
    :return: modified results table
    """
    cells.insert(3, html.th('Timestamp', class_='sortable time', col='time'))
    cells.pop()


@pytest.mark.optionalhook
def pytest_html_results_table_row(report, cells):
    """
    :param report: html report
    :param cells: report table cells
    :return: adds datetime utc time to the third column
    """
    cells.insert(3, html.td(datetime.now(), class_='col-time'))
    cells.pop()


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """
    :param item:
    :param call: does action when code is executed
    :return: modified html report table
    """
    outcome = yield
    report = outcome.get_result()
    report.timestamp = str(item.function.__doc__)


def pytest_addoption(parser):
    parser.addoption("--env", action="store", default='DEFAULT', help=f"Possible environment values: []")


@allure.title("setup:: define env")
@pytest.fixture(scope="session")
def environment(request):
    option = request.config.getoption("--env")
    if not option:
        raise EnvironmentError("Specified environment is invalid. Use --help for more information")
    return option.upper()


@allure.title("setup:: define env config")
@pytest.fixture(scope="session")
def project_config(environment):
    return ProjectConfig(environment)
