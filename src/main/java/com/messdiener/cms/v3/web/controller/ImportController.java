package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.worship.Liturgie;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.shared.enums.LiturgieType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ImportController {

    private final LiturgieService liturgieService;

    @GetMapping("/import")
    public RedirectView importPage() throws IOException, SQLException {
        System.out.println("Import page");
        List<String[]> data = new ArrayList<>();

        Path filePath = Path.of("." + File.separator + "cms_vx" + File.separator + "import" +  File.separator + "importFile.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                data.add(values);
            }
        }

        System.out.println(data.size());


        for(String[] values : data){

            System.out.println(Arrays.toString(values));

            String date = values[0];
            String time = values[1];
            String tenantName = values[2];
            String type = values[3];

            CMSDate date1 = CMSDate.convert(date + " " + time, DateUtils.DateType.GERMAN_WITH_TIME);
            Liturgie liturgie = new Liturgie(UUID.randomUUID(), 0, UUID.fromString(tenantName), LiturgieType.valueOf(type), date1, true);
            liturgieService.save(liturgie);
        }

        return new RedirectView("/");
    }

}
