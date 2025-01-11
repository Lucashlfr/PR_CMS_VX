package com.messdiener.cms.v3.app.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Utils {

    public static ResponseEntity<?> download(File file) throws FileNotFoundException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");


        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    public static Optional<File> createFile(List<String[]> export, String name) {
        String seperator = ";";

        File file = new File(name + ".csv");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(file)) {

            StringBuilder sb = new StringBuilder();
            for (String[] strings : export) {
                for (int i = 0; i < strings.length; i++) {
                    sb.append(strings[i]);
                    if (i < (strings.length - 1)) {
                        sb.append(seperator);
                    }
                }
                sb.append("\n");
            }

            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.of(file);
    }


    public static void downloadFile(File file, HttpServletResponse response) throws IOException {
        if (file.exists()) {

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);

            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            FileCopyUtils.copy(inputStream, response.getOutputStream());

            file.delete();
        }
    }

    public static double roundToTwoDecimalPlaces(double number) {
        DecimalFormat df = new DecimalFormat("#,##");
        return Double.parseDouble(df.format(number));
    }

    public static List<List<String>> importFromCSV(MultipartFile file) throws IOException, IllegalAccessException {

        if (file.getSize() > 1048576) {
            throw new IllegalAccessException();
        }



        List<List<String>> records = new ArrayList<>();

        File outputFile = File.createTempFile("import", "csv");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(file.getBytes());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                records.add(Arrays.asList(values));
            }
        }
        outputFile.delete();

        return records;

    }
}
