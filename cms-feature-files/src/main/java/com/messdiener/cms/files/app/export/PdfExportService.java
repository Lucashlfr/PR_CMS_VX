package com.messdiener.cms.files.app.export;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.messdiener.cms.shared.ui.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    public byte[] exportWorkflowAsPdf(String workflowTitle, List<Component> components, Map<String, String> values) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titel
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            document.add(new Paragraph("Workflow: " + workflowTitle, titleFont));
            document.add(new Paragraph(" "));

            // Komponententabelle
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);
            for (Component component : components) {
                String label = component.getLabel();
                String name = component.getName();
                String value = values.getOrDefault(name, "(nicht ausgefüllt)");

                Paragraph line = new Paragraph();
                line.add(new Chunk(label + ": ", labelFont));
                line.add(new Chunk(value, valueFont));
                document.add(line);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF-Erstellung fehlgeschlagen", e);
        }
    }
}
