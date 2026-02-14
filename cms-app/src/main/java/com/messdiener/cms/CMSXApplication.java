// cms-app/src/main/java/com/messdiener/cms/CMSXApplication.java
package com.messdiener.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.messdiener") // scannt alle Module unter com.messdiener...
public class CMSXApplication {

    public static void main(String[] args) {
        SpringApplication.run(CMSXApplication.class, args);
    }

}
