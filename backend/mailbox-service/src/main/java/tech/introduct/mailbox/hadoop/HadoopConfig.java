package tech.introduct.mailbox.hadoop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;

@Configuration
@EnableConfigurationProperties(HadoopProperties.class)
@RequiredArgsConstructor
@Slf4j
public class HadoopConfig {
    private final HadoopProperties properties;

    @Bean
    public org.apache.hadoop.conf.Configuration hadoopConfiguration() {
        var configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, properties.getUri());
        configuration.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
        configuration.set("fs.file.impl", LocalFileSystem.class.getName());
        System.setProperty("HADOOP_USER_NAME", properties.getUser());
        System.setProperty("hadoop.home.dir", properties.getHomeDir());
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser(properties.getUser()));
        return configuration;
    }

    @Bean
    public FileSystem hadoopFileSystem(org.apache.hadoop.conf.Configuration conf) throws IOException {
        log.info("hadoopFileSystem initialization url {}", properties.getUri());
        var fileSystem = FileSystem.get(URI.create(properties.getUri()), conf);
        var newFolderPath = new Path(properties.getPath());
        log.info("hadoopFileSystem folderPath exist {}", fileSystem.exists(newFolderPath));
        if (!fileSystem.exists(newFolderPath)) {
            fileSystem.mkdirs(newFolderPath);
            log.info("Hadoop path {} created.", properties.getPath());
        }
        return fileSystem;
    }
}
