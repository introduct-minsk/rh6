import selene.config
import warnings

warnings.filterwarnings('ignore', message='Unverified HTTPS request')
selene.config.timeout = 30
