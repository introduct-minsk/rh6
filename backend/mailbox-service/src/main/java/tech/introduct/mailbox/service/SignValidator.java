package tech.introduct.mailbox.service;

import tech.introduct.mailbox.dto.sign.SignatureDto;

public interface SignValidator {

    SignatureDto validate(String filename, byte[] bytes);
}
