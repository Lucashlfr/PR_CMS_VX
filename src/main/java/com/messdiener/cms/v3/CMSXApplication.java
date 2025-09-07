package com.messdiener.cms.v3;

import com.messdiener.cms.v3.app.export.PdfGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CMSXApplication {
    public static void main(String[] args) throws Exception {
        new PdfGenerator().createPdf("output.pdf", "Mein Dokumenttitel");
        SpringApplication.run(CMSXApplication.class, args);

    }
}

