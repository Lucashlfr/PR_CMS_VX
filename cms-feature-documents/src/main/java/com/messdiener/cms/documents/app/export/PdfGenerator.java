package com.messdiener.cms.documents.app.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;

@Component
public class PdfGenerator {

    public void createPdf(String filename, String title) throws Exception {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        writer.setPageEvent(new PdfHeaderFooter(title));
        document.open();

        // Beispielinhalt
        for (int i = 0; i < 3; i++) {
            document.add(new Paragraph("Inhalt des Dokuments - Seite " + (i + 1)));
            document.newPage();
        }

        document.close();
    }

    public static class PdfHeaderFooter extends PdfPageEventHelper {

        private final String title;
        private final Font whiteFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        private final Font grayItalicFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, new BaseColor(100, 100, 100));
        private final BaseColor darkRed = new BaseColor(161, 33, 28);
        private PdfTemplate total;

        public PdfHeaderFooter(String title) {
            this.title = title;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(50, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle pageSize = document.getPageSize();

            float tableWidth = pageSize.getWidth() - document.leftMargin() - document.rightMargin();
            float headerY = pageSize.getTop() - 10;

            try {
                // === HEADER-TABELLE ===
                PdfPTable headerTable = new PdfPTable(3);
                headerTable.setTotalWidth(tableWidth);
                headerTable.setWidths(new float[]{2, 5, 2});
                headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                headerTable.getDefaultCell().setBackgroundColor(darkRed);

                PdfPCell left = new PdfPCell(new Phrase(CMSDate.current().convertTo(DateUtils.DateType.GERMAN_WITH_TIME), whiteFont));
                left.setBorder(Rectangle.NO_BORDER);
                left.setHorizontalAlignment(Element.ALIGN_LEFT);
                left.setVerticalAlignment(Element.ALIGN_MIDDLE);
                left.setBackgroundColor(darkRed);
                left.setPadding(5);

                PdfPCell center = new PdfPCell(new Phrase(title, whiteFont));
                center.setBorder(Rectangle.NO_BORDER);
                center.setHorizontalAlignment(Element.ALIGN_CENTER);
                center.setVerticalAlignment(Element.ALIGN_MIDDLE);
                center.setBackgroundColor(darkRed);
                center.setPadding(5);

                PdfPCell right = new PdfPCell(new Phrase("Seite " + writer.getPageNumber(), whiteFont));
                right.setBorder(Rectangle.NO_BORDER);
                right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                right.setVerticalAlignment(Element.ALIGN_MIDDLE);
                right.setBackgroundColor(darkRed);
                right.setPadding(5);

                headerTable.addCell(left);
                headerTable.addCell(center);
                headerTable.addCell(right);

                // Header-Tabelle schreiben
                headerTable.writeSelectedRows(0, -1, document.leftMargin(), headerY, cb);


                // === FUßZEILE ===
                ColumnText.showTextAligned(cb,
                        Element.ALIGN_CENTER,
                        new Phrase("CMS Messdiener der Pfarrei Bellheim.", grayItalicFont),
                        pageSize.getWidth() / 2,
                        document.bottom() - 10,
                        0
                );

            } catch (DocumentException e) {
                throw new ExceptionConverter(e);
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            try {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                total.beginText();
                total.setFontAndSize(bf, 10);
                total.setTextMatrix(0, 0);
                total.showText(String.valueOf(writer.getPageNumber() - 1));
                total.endText();
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

}
