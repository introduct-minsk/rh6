from dataclasses import dataclass


@dataclass
class User:
    personal_code: str
    mobile_phone: str = None
    smart_id: str = None
