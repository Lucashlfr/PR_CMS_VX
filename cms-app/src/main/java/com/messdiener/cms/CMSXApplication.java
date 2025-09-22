// cms-app/src/main/java/com/messdiener/cms/CMSXApplication.java
package com.messdiener.cms;

import com.messdiener.cms.documents.app.export.PdfGenerator;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.messdiener") // scannt alle Module unter com.messdiener...
public class CMSXApplication {

    public static void main(String[] args) {
        SpringApplication.run(CMSXApplication.class, args);
    }

    /**
     * Optional: nur falls du beim Start ein PDF erzeugen willst.
     * Wenn nicht gewünscht, diesen Bean einfach entfernen.
     */
    @Bean
    ApplicationRunner generatePdfOnStartup(PdfGenerator pdfGenerator) {
        return args -> pdfGenerator.createPdf("output.pdf", "Mein Dokumenttitel");
    }
}
