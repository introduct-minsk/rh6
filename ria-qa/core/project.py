#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os.path

from core.logger import Logger


class Project:
    """
    Defines config settings for common library objects, e.g. own logger etc
    """
    CRITICAL = 50
    ERROR = 40
    WARNING = 30
    INFO = 20
    DEBUG = 10

    def __init__(self):
        self.logger = Logger(
            path_to_logging_json=self.path_to_logging_json,
            path_to_logs_folder=self.path_to_logs_folder
        )

    @property
    def path_to_repository(self):
        """
        :return:  path to account core repository
        """
        return os.path.dirname(os.path.dirname(__file__))

    @property
    def path_to_config_folder(self):
        """
        :return:  path to account core config folder
        """
        return os.path.join(self.path_to_repository, 'config')

    @property
    def path_to_logging_json(self):
        """
        :return:  path to logging json config file for core
        """
        return os.path.join(self.path_to_config_folder, 'logging.json')

    @property
    def path_to_logs_folder(self):
        """
        :return:  path to logs folder for core
        """
        return os.path.join(self.path_to_repository, "logs")

    @property
    def path_to_test_data_folder(self):
        """
        :return:  path to logs folder for core
        """
        return os.path.join(self.path_to_repository, "test_data")


project = Project()
