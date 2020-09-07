package tech.introduct.mailbox.dto.sign;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.spi.DSSUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Data
@Slf4j
public class SigningData implements Serializable {
    @NonNull
    private final String certInHex;
    private Container container;
    private DataToSign dataToSign;
    private String signatureInHex;

    public X509Certificate getCertificate() {
        try {
            byte[] certificateBytes = DatatypeConverter.parseHexBinary(certInHex);
            try (InputStream inStream = new ByteArrayInputStream(certificateBytes)) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                return (X509Certificate) cf.generateCertificate(inStream);
            }
        } catch (Exception e) {
            throw new ErrorInfo("wrong_certificate").badRequest();
        }
    }

    public String getDataToSignInHex() {
        log.debug("getDataToSignInHex {}", Base64.getEncoder().encodeToString(dataToSign.getDataToSign()));
        return DatatypeConverter.printHexBinary(DSSUtils.digest(DigestAlgorithm.SHA256, dataToSign.getDataToSign()));
    }

    public byte[] getSignature() {
        return DatatypeConverter.parseHexBinary(signatureInHex);
    }
}
