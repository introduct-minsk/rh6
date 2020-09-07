package tech.introduct.mailbox.service.impl;

import lombok.Data;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tech.introduct.mailbox.dto.sign.SigningData;

import java.util.Collection;

@Component
public class FileSigner {
    private final DigestAlgorithm algorithm;
    private final Configuration configuration;

    public FileSigner(Digidoc4jProperties properties) {
        this.algorithm = properties.getAlgorithm();
        this.configuration = new Configuration(properties.getMode());
    }

    public void generateDataToSign(SigningData signingData, Collection<DataFile> dataFile) {
        signingData.setContainer(createContainer(dataFile));
        signingData.setDataToSign(createDataToSign(signingData));
    }

    public void signContainer(SigningData signingData) {
        Signature signature = signingData.getDataToSign().finalize(signingData.getSignature());
        signingData.getContainer().addSignature(signature);
    }

    private Container createContainer(Collection<DataFile> dataFiles) {
        var builder = BDocContainerBuilder
                .aContainer()
                .withConfiguration(configuration);
        dataFiles.forEach(builder::withDataFile);
        return builder.build();
    }

    private DataToSign createDataToSign(SigningData signingData) {
        return SignatureBuilder
                .aSignature(signingData.getContainer())
                .withSigningCertificate(signingData.getCertificate())
                .withSignatureDigestAlgorithm(algorithm)
                .withSignatureProfile(SignatureProfile.LT_TM)
                .buildDataToSign();
    }

    public String getFileFormat() {
        return "asice";
    }

    public MediaType getMimeType() {
        return MediaType.parseMediaType("application/vnd.etsi.asic-e+zip");
    }

    @Data
    @ConfigurationProperties(prefix = "digidoc4j")
    @org.springframework.context.annotation.Configuration
    public static class Digidoc4jProperties {
        private DigestAlgorithm algorithm = DigestAlgorithm.SHA256;
        private Configuration.Mode mode = Configuration.Mode.PROD;

    }
}
