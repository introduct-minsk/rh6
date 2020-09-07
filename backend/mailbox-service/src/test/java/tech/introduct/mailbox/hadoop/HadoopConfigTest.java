package tech.introduct.mailbox.hadoop;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class HadoopConfigTest {

    @Test
    void hadoopFileSystem_createPackage() throws IOException {
        var properties = new HadoopProperties();
        properties.setUri("file:///");
        properties.setPath("target/" + RandomStringUtils.randomAlphabetic(10));
        var hadoopConfig = new HadoopConfig(properties);
        hadoopConfig.hadoopFileSystem(new org.apache.hadoop.conf.Configuration());
    }
}
