package org.inventory.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.inventory.model.Sale;
import org.inventory.model.SaleItem;

import java.io.IOException;
import java.util.List;

public class PDFGenerator {

    public void generateInvoice(Sale sale, List<SaleItem> items, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("INVOICE");
                contentStream.endText();

                // Sale Details
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Sale ID: " + sale.getId()); // ID might be null if not refreshed from DB
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date: " + sale.getSaleDate());
                contentStream.endText();

                // Items Header
                int y = 650;
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Item");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText("Qty");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Price");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Total");
                contentStream.endText();

                y -= 20;
                contentStream.moveTo(50, y + 15);
                contentStream.lineTo(500, y + 15);
                contentStream.stroke();

                // Items
                for (SaleItem item : items) {
                    y -= 20;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("Product " + item.getProductId()); // Ideally Product Name
                    contentStream.newLineAtOffset(200, 0);
                    contentStream.showText(String.valueOf(item.getQuantity()));
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(item.getUnitPrice().toString());
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(item.getSubtotal().toString());
                    contentStream.endText();
                }

                // Total
                y -= 40;
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(350, y);
                contentStream.showText("Total: " + sale.getTotalAmount());
                contentStream.endText();
            }

            document.save(filePath);
        }
    }
}
