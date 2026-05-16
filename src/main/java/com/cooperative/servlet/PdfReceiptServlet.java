package com.cooperative.servlet;

import com.cooperative.dao.ReservationDAO;
import com.cooperative.model.ReservationReceiptData;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

@WebServlet("/receipt")
public class PdfReceiptServlet extends HttpServlet {
    private static final Font TITLE = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font LABEL = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font VALUE = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idReserv = req.getParameter("id");
        try {
            ReservationReceiptData data = reservationDAO.findReceiptData(idReserv);
            if (data == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Reservation introuvable");
                return;
            }

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=recu-" + idReserv + ".pdf");

            Document doc = new Document();
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();

            Paragraph title = new Paragraph("Recu N\u00b0" + data.getIdReserv(), TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(18f);
            doc.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.4f, 2f});
            table.setSpacingBefore(6f);
            table.setSpacingAfter(12f);

            addRow(table, "Date de reservation :", data.getDateReserv());
            addRow(table, "Date du voyage :", data.getDateVoyage());
            addRow(table, "Nom du client :", data.getNomClient());
            addRow(table, "Contact :", formatContact(data.getNumTel()));
            addRow(table, "Voiture :", displayVoiture(data.getIdVoiture()));
            addRow(table, "Type de voiture :", data.getTypeVoiture());
            addRow(table, "Place :", String.valueOf(data.getPlace()));
            addRow(table, "Frais :", formatAr(data.getFrais()));
            addRow(table, "Paiement :", data.getPaiement());
            addRow(table, "Montant avance :", formatAr(data.getMontantAvance()));
            addRow(table, "Reste a payer :", formatAr(data.getReste()));

            doc.add(table);

            Paragraph footer = new Paragraph("Cooperative - Merci pour votre confiance", VALUE);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(24f);
            doc.add(footer);
            doc.close();
        } catch (DocumentException e) {
            throw new ServletException(e);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPaddingBottom(8f);
        PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "" : value, VALUE));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPaddingBottom(8f);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String displayVoiture(String idVoiture) {
        if (idVoiture == null || idVoiture.isBlank()) {
            return "";
        }
        String id = idVoiture.trim();
        if (id.startsWith("RES-")) {
            return "N\u00b0" + id.substring(4);
        }
        return "N\u00b0" + id;
    }

    private String formatContact(String numTel) {
        if (numTel == null) {
            return "";
        }
        String digits = numTel.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return digits.substring(0, 3) + " " + digits.substring(3, 5) + " " + digits.substring(5, 8) + " " + digits.substring(8);
        }
        return numTel.trim();
    }

    private String formatAr(int value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        return nf.format(value) + " Ar";
    }
}
