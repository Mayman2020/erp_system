package com.erp.system.admin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.backup")
public class BackupProperties {
    private String pgDumpPath = "pg_dump";
    private String host = "localhost";
    private String port = "5432";
    private String db = "postgres";
    private String user = "postgres";
    private String password = "";
    private String dir = "./backups";
}
