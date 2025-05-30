package com.messdiener.cms.v3.app.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.table.CMSCell;
import com.messdiener.cms.v3.app.entities.table.CMSRow;
import com.messdiener.cms.v3.utils.other.Pair;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class FileCreator {

    private static final Font contentFont = FontFactory.getFont("ARIAL", 10);
    private final Document document;

    private final ArrayList<Pair<String, String>> table;
    private String folder;
    private String fileName;
    private final ArrayList<String> paragraphs;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String subject;
    private String greeting;
    private String documentOwner;
    private String rank;
    private String sender1;
    private String sender2;
    private String sender3;
    private String sender4;
    private String signature;
    private String imageUrl;
    private String abbreviation;
    private String workflow;
    private String manager;
    private String approval;
    private List<CMSRow> rows;

    public FileCreator() {
        this.document = new Document();
        this.folder = "." + File.separator + "cms_vx" + File.separator;
        this.fileName = "new_document.pdf";
        this.address1 = "";
        this.address2 = "";
        this.address3 = "";
        this.address4 = "";
        this.table = new ArrayList<>();
        this.paragraphs = new ArrayList<>();
        this.subject = "";
        this.greeting = "";
        this.documentOwner = "Lucas Helfer";
        this.rank = "Obermessdiener der Pfarrei";
        this.sender1 = "Lucas Helfer";
        this.sender2 = "Hintere Straße 1";
        this.sender3 = "76756 Bellheim";
        this.sender4 = "lucas@messdiener.com";
        this.signature = "";
        this.imageUrl = "https://i.ibb.co/zWJfCnHd/Logo-mit-Schrift-HL-Hildegard-Bellheim.png";
        this.abbreviation = "-";
        this.workflow = "-";
        this.manager = "-";
        this.approval = "Keine (F0)";
        this.rows = new ArrayList<>();
    }

    public FileCreator setFolder(String... folder) {

        StringBuilder builder = new StringBuilder("./cms_vx");
        builder.append(File.separator);

        for (String s : folder) {
            builder.append(s);
            builder.append(File.separator);
        }

        File dir = new File(builder.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.folder = builder.toString();
        return this;
    }

    public FileCreator setFileName(String fileName) {
        String uuid = UUID.randomUUID().toString();

        this.fileName = CMSDate.current().getEnglishDate() + "_" + uuid.split("-")[0] + "_" + fileName;
        return this;
    }

    public FileCreator setFileName(UUID uuid) {
        this.fileName = uuid.toString() + ".pdf";
        return this;
    }

    public FileCreator setReceiver(Person person, String tenantName) {
        this.address1 = person.getName();
        this.address2 = tenantName;
        this.address3 = person.getStreet() + " " + person.getHouseNumber();
        this.address4 = person.getPostalCode() + " " + person.getCity();
        return this;
    }

    public FileCreator setReceiver(String address1, String address2, String address3, String address4) {
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.address4 = address4;
        return this;
    }

    public FileCreator setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public FileCreator addAttribute(String attribute, String value) {
        this.table.add(new Pair<>(attribute, value));
        return this;
    }

    public FileCreator addText(String text) {
        this.paragraphs.add(text);
        return this;
    }

    public FileCreator setGreeting(String greeting) {
        this.greeting = "Hallo " + greeting + ",";
        return this;
    }


    public FileCreator setSender(Person sender, String tenantName) throws SQLException {
        this.documentOwner = sender.getName();
        this.rank = sender.getRank().getName() + " | " + tenantName;

        this.sender1 = sender.getName();
        this.sender2 = sender.getStreet() + " " + sender.getHouseNumber();
        this.sender3 = sender.getPostalCode() + " " + sender.getCity();
        this.sender4 = sender.getEmail();

        return this;
    }

    public FileCreator setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public FileCreator setImageUrlKn() {
        this.imageUrl = "https://messdiener-knittelsheim.de/wp-content/uploads/2025/04/Logo_mit_Schrift_Knittelsheim.png";
        return this;
    }

    public FileCreator setInfoline(String abbreviation, String workflow, String manager, String approval) {
        this.abbreviation = abbreviation;
        this.workflow = workflow;
        this.manager = manager;
        this.approval = approval;
        return this;
    }

    public FileCreator addRow(CMSRow row) {
        this.rows.add(row);
        return this;
    }

    private PdfPCell getCell(String text, int fontSize, boolean b) {
        Font font = FontFactory.getFont("ARIAL", fontSize);
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(text, font));
        if(b){
            pdfPCell.setBorder(PdfPCell.BOX);
        }else {
            pdfPCell.setBorder(PdfPCell.NO_BORDER);
        }
        return pdfPCell;
    }



    private PdfPCell getBoldCell(String text, int fontSize, boolean b) {
        Font boldFont = FontFactory.getFont("ARIAL", fontSize, Font.BOLD);
        PdfPCell pdfPCell = new PdfPCell(new Phrase(text, boldFont));
        if(b){
            pdfPCell.setBorder(PdfPCell.BOX);
        }else {
            pdfPCell.setBorder(PdfPCell.NO_BORDER);
        }
        return pdfPCell;
    }

    private PdfPCell getUnderlinedCell(String text, int fontSize) {
        Font underlinedFont = FontFactory.getFont("ARIAL", fontSize, Font.UNDERLINE);
        PdfPCell pdfPCell = new PdfPCell(new Phrase(text, underlinedFont));
        pdfPCell.setBorder(PdfPCell.NO_BORDER);
        return pdfPCell;
    }

    private void addParagraph(String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, contentFont);
        paragraph.setLeading(0f, 1f);
        document.add(paragraph);
    }

    private void addNewLine() throws DocumentException {
        Paragraph paragraph = new Paragraph("", contentFont);
        paragraph.add(Chunk.NEWLINE);
        paragraph.setLeading(0f, 1f);
        document.add(paragraph);
    }


    public Optional<File> createPdf() throws IOException, DocumentException {

        PdfWriter.getInstance(document, new FileOutputStream(folder + fileName));
        document.open();

        Image image = Image.getInstance(new URL(imageUrl));
        image.scalePercent(25);
        image.setAlignment(Element.ALIGN_CENTER);
        document.add(image);

        PdfPTable tableA = new PdfPTable(2);
        tableA.setWidthPercentage(100);
        tableA.setWidths(new float[]{3, 2});

        PdfPCell headerCell = getUnderlinedCell("Messdiener Pfarrei Bellheim – Hintere Straße 1 | 76756 Bellheim", 8);
        headerCell.setColspan(2);
        tableA.addCell(headerCell);

        tableA.addCell(getCell(address1 + "\n" + address2 + "\n" + address3 + "\n" + address4, 10, false));
        PdfPCell cmsCell = new PdfPCell();
        cmsCell.setBorder(PdfPCell.NO_BORDER);
        Paragraph cmsContent = new Paragraph();
        cmsContent.setLeading(0f, 1f);
        cmsContent.add(new Chunk("Central Management System\n", FontFactory.getFont("ARIAL", 10, Font.BOLD)));
        cmsContent.add(new Chunk(sender1 + "\n" + sender2 + "\n" + sender3 + "\n\nE-Mail: \n" + sender4, FontFactory.getFont("ARIAL", 10)));

        cmsCell.addElement(cmsContent);
        tableA.addCell(cmsCell);
        document.add(tableA);

        addNewLine();
        addNewLine();
        PdfPTable tableC = new PdfPTable(5);
        tableC.setWidthPercentage(100);
        tableC.setWidths(new float[]{2, 2,2,2,2});

        tableC.addCell(getBoldCell("Kürzel", 8, false));
        tableC.addCell(getBoldCell("Workflow", 8, false));
        tableC.addCell(getBoldCell("Obermessdiener*in", 8, false));
        tableC.addCell(getBoldCell("Freigabe", 8, false));
        tableC.addCell(getBoldCell("Datum", 8, false));

        tableC.addCell(getCell(abbreviation, 8, false));
        tableC.addCell(getCell(workflow, 8, false));
        tableC.addCell(getCell(manager, 8, false));
        tableC.addCell(getCell(approval, 8, false));
        tableC.addCell(getCell(CMSDate.current().getGermanDate(), 8, false));

        document.add(tableC);

        /*
        Paragraph infoParagraph = new Paragraph("– Erstellt über Central Management System –", FontFactory.getFont("ARIAL", 10, Font.BOLD, BaseColor.RED));
        infoParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(infoParagraph);
         */
        addNewLine();
        addNewLine();

        Font titleFont = FontFactory.getFont("ARIAL", 10, Font.BOLD);
        Paragraph title = new Paragraph(subject, titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        document.add(title);

        addNewLine();
        addParagraph(greeting);
        addNewLine();

        for (String paragraph : paragraphs) {
            addParagraph(paragraph);
            addNewLine();
        }

        if (!table.isEmpty()) {

            PdfPTable tableF = new PdfPTable(2);
            tableF.setWidthPercentage(100);
            tableF.setWidths(new float[]{1, 1});
            tableF.addCell(getBoldCell("Feld", 10, true));
            tableF.addCell(getBoldCell("Wert", 10, true));

            for (Pair<String, String> attribute : table) {
                tableF.addCell(getCell(attribute.getFirst(), 10, true));
                tableF.addCell(getCell(attribute.getSecond(), 10, true));
            }

            document.add(tableF);
            addNewLine();
        }

        // **Neue Tabelle aus CMSRow-Liste einfügen**
        if (rows != null && !rows.isEmpty()) {
            // Anzahl der Spalten anhand der ersten Zeile ermitteln
            int numCols = rows.getFirst().getCells().size();
            PdfPTable dataTable = new PdfPTable(numCols);
            dataTable.setWidthPercentage(100);

            // Zeilen hinzufügen
            for (CMSRow row : rows) {
                if (row.check()) { // nur gültige Zeilen
                    for (CMSCell cell : row.getCells()) {
                        dataTable.addCell(getCell(cell.getContent(), 10, false));
                    }
                }
            }

            document.add(dataTable);
            addNewLine();
        }

        addParagraph("Mit freundlichen Grüßen,");
        addNewLine();

        if (signature != null && !signature.isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(signature.split(",")[1]);
            Image signature = Image.getInstance(decodedBytes);
            signature.scaleToFit(524, 100);
            signature.setAlignment(Element.ALIGN_LEFT);
            document.add(signature);
            addNewLine();
        }

        Paragraph docOwner = new Paragraph(documentOwner, titleFont);
        docOwner.setAlignment(Element.ALIGN_LEFT);
        document.add(docOwner);
        addParagraph(rank);

        document.close();
        System.out.println("PDF generated successfully.");
        return Optional.of(new File(folder + fileName));
    }
}
