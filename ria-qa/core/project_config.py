import os
from configparser import ConfigParser, ExtendedInterpolation
from core.project import project


class ProjectConfig:
    def __init__(self, env: str = "DEFAULT"):
        config = ConfigParser(interpolation=ExtendedInterpolation())
        config.read(os.path.join(project.path_to_config_folder, "env.cfg"))
        self.web_host = config.get(env, "web_host")
