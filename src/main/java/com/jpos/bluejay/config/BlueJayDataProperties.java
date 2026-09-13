package com.jpos.bluejay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bluejay.datasource")
public class BlueJayDataProperties {
    private String url = "jdbc:sqlite:data/db/bluejay.db";
    private String driverClassName = "org.sqlite.JDBC";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
}
