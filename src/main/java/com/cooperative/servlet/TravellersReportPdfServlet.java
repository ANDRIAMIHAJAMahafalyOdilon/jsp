package com.cooperative.servlet;

import com.cooperative.dao.ReservationDAO;
import com.cooperative.model.ReservationView;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@WebServlet("/travellers-report")
public class TravellersReportPdfServlet extends HttpServlet {
    private final ReservationDAO reservationDAO = new ReservationDAO();

    private static final BaseColor NAVY = new BaseColor(13, 27, 42);
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 247, 250);
    private static final BaseColor BORDER = new BaseColor(226, 232, 240);
    private static final BaseColor SUCCESS = new BaseColor(22, 163, 74);
    private static final BaseColor WARNING = new BaseColor(245, 158, 11);
    private static final BaseColor DANGER = new BaseColor(220, 38, 38);

    private static final Font H1 = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, NAVY);
    private static final Font H2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, NAVY);
    private static final Font TXT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font TXT_DARK = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
    private static final Font TABLE_HEAD = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
    private static final Font MONEY = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);
    private static final Font BADGE = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idVoit = safe(req.getParameter("idvoit"));
        String paiement = safe(req.getParameter("paiementFilter"));
        String keyword = safe(req.getParameter("keyword"));
        String dateFromRaw = safe(req.getParameter("dateFrom"));
        String dateToRaw = safe(req.getParameter("dateTo"));
        String sort = safe(req.getParameter("sort"));

        try {
            LocalDate dateFrom = parseOptionalDate(dateFromRaw);
            LocalDate dateTo = parseOptionalDate(dateToRaw);
            List<ReservationView> rows = reservationDAO.findForList(idVoit, paiement, keyword, dateFrom, dateTo, sort);

            LocalDateTime generatedAt = LocalDateTime.now();
            String reportNo = "RPT-" + generatedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

            LocalDate periodStart = dateFrom;
            LocalDate periodEnd = dateTo;
            if ((periodStart == null || periodEnd == null) && !rows.isEmpty()) {
                LocalDate min = rows.stream().map(ReservationView::getDateVoyage).filter(d -> d != null).min(Comparator.naturalOrder()).orElse(null);
                LocalDate max = rows.stream().map(ReservationView::getDateVoyage).filter(d -> d != null).max(Comparator.naturalOrder()).orElse(null);
                if (periodStart == null) periodStart = min;
                if (periodEnd == null) periodEnd = max;
            }

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=liste-voyageurs-" + reportNo + ".pdf");
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
            resp.setHeader("X-Report-Theme", "premium-v2");

            Document doc = new Document(PageSize.A4, 36, 36, 36, 54);
            PdfWriter writer = PdfWriter.getInstance(doc, resp.getOutputStream());
            writer.setPageEvent(new PremiumPageEvent(reportNo));
            doc.open();

            addHeader(doc, writer, reportNo);
            addTitle(doc);
            addStatsCards(doc, rows.size(), periodStart, periodEnd);
            addTravellersTable(doc, rows);
            addFooter(doc, generatedAt);

            doc.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDate parseOptionalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw);
    }

    private String formatAr(int value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);
        return nf.format(value) + " Ar";
    }

    private void addHeader(Document doc, PdfWriter writer, String reportNo) throws DocumentException, IOException {
        // Watermark (discreet)
        PdfContentByte cb = writer.getDirectContentUnder();
        cb.saveState();
        cb.setColorFill(new BaseColor(0, 0, 0, 20));
        cb.beginText();
        cb.setFontAndSize(FontFactoryHelper.base(), 54);
        cb.showTextAligned(Element.ALIGN_CENTER, "AGENCE VOYAGES", 297.5f, 420, 25);
        cb.endText();
        cb.restoreState();

        PdfPTable header = new PdfPTable(new float[]{1.3f, 2.2f, 1.8f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(8f);

        // “Logo” vector-like: small dark square + A
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        PdfPTable logoBox = new PdfPTable(1);
        logoBox.setWidthPercentage(100);
        PdfPCell logo = new PdfPCell(new Phrase("A", new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE)));
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setFixedHeight(44f);
        logo.setBackgroundColor(NAVY);
        logo.setBorder(Rectangle.NO_BORDER);
        logoBox.addCell(logo);
        logoCell.addElement(logoBox);
        header.addCell(logoCell);

        PdfPCell agencyCell = new PdfPCell();
        agencyCell.setBorder(Rectangle.NO_BORDER);
        agencyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph agency = new Paragraph();
        agency.add(new Chunk("AGENCE VOYAGES\n", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, NAVY)));
        agency.add(new Chunk("Le voyage commence ici", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY)));
        agencyCell.addElement(agency);
        header.addCell(agencyCell);

        PdfPCell contactCell = new PdfPCell();
        contactCell.setBorder(Rectangle.NO_BORDER);
        contactCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        contactCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph contact = new Paragraph();
        contact.setAlignment(Element.ALIGN_RIGHT);
        contact.add(new Chunk("Téléphone: +261 34 00 000 00\n", TXT));
        contact.add(new Chunk("Email: contact@agencevoyages.mg\n", TXT));
        contact.add(new Chunk("Adresse: Antananarivo, Madagascar\n", TXT));
        contact.add(new Chunk("Rapport: " + reportNo, SMALL));
        contactCell.addElement(contact);
        header.addCell(contactCell);

        doc.add(header);

        // QR code (bonus)
        BarcodeQRCode qr = new BarcodeQRCode("Rapport " + reportNo, 120, 120, null);
        Image qrImg = qr.getImage();
        qrImg.scaleAbsolute(54f, 54f);
        qrImg.setAbsolutePosition(doc.right() - 54f, doc.top() - 54f);
        writer.getDirectContent().addImage(qrImg);
    }

    private void addTitle(Document doc) throws DocumentException {
        Paragraph title = new Paragraph("LISTE DES VOYAGEURS", H1);
        title.setSpacingBefore(6f);
        title.setSpacingAfter(4f);
        doc.add(title);

        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell line = new PdfPCell(new Phrase(" "));
        line.setFixedHeight(2.5f);
        line.setBorder(Rectangle.NO_BORDER);
        line.setBackgroundColor(NAVY);
        rule.addCell(line);
        rule.setSpacingAfter(10f);
        doc.add(rule);
    }

    private void addStatsCards(Document doc, int total, LocalDate from, LocalDate to) throws DocumentException {
        PdfPTable cards = new PdfPTable(new float[]{1f, 1f, 1f});
        cards.setWidthPercentage(100);
        cards.setSpacingAfter(12f);

        cards.addCell(cardCell("📌  Total réservations", String.valueOf(total)));
        cards.addCell(cardCell("📅  Période couverte", periodLabel(from, to)));
        cards.addCell(cardCell("💱  Devise", "Ariary (Ar)"));

        doc.add(cards);
    }

    private PdfPCell cardCell(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(BORDER);
        c.setBorderWidth(1f);
        c.setPadding(10f);
        c.setBackgroundColor(BaseColor.WHITE);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, NAVY)));
        p.add(new Chunk(value, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK)));
        c.addElement(p);
        return c;
    }

    private String periodLabel(LocalDate from, LocalDate to) {
        if (from == null && to == null) return "Toutes dates";
        if (from != null && to == null) return "Depuis " + from;
        if (from == null) return "Jusqu’au " + to;
        return from + " → " + to;
    }

    private void addTravellersTable(Document doc, List<ReservationView> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.15f, 1.6f, 1.2f, 1.55f, 1.2f, 1.1f, 1.0f});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        addHead(table, "Réservation");
        addHead(table, "Client");
        addHead(table, "Contact");
        addHead(table, "Voiture");
        addHead(table, "Date voyage");
        addHead(table, "Paiement");
        addHead(table, "Reste");

        for (int i = 0; i < rows.size(); i++) {
            ReservationView r = rows.get(i);
            BaseColor bg = (i % 2 == 0) ? BaseColor.WHITE : LIGHT_GRAY;

            table.addCell(bodyCell(r.getIdReserv(), bg, Element.ALIGN_LEFT));
            table.addCell(bodyCell(r.getNomClient(), bg, Element.ALIGN_LEFT));
            table.addCell(bodyCell(r.getNumTel(), bg, Element.ALIGN_LEFT));
            table.addCell(bodyCell(r.getIdVoit() + " (" + r.getTypeVoiture() + ")", bg, Element.ALIGN_LEFT));
            table.addCell(bodyCell(String.valueOf(r.getDateVoyage()), bg, Element.ALIGN_LEFT));
            table.addCell(paiementBadgeCell(r.getPaiement(), bg));
            table.addCell(moneyCell(formatAr(r.getReste()), bg));
        }

        doc.add(table);
    }

    private void addHead(PdfPTable t, String label) {
        PdfPCell c = new PdfPCell(new Phrase(label, TABLE_HEAD));
        c.setBackgroundColor(NAVY);
        c.setBorderColor(NAVY);
        c.setPadding(8f);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(c);
    }

    private PdfPCell bodyCell(String txt, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt == null ? "" : txt, TXT_DARK));
        c.setBackgroundColor(bg);
        c.setBorderColor(BORDER);
        c.setPadding(7f);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell moneyCell(String txt, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt == null ? "" : txt, MONEY));
        c.setBackgroundColor(bg);
        c.setBorderColor(BORDER);
        c.setPadding(7f);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell paiementBadgeCell(String paiement, BaseColor bg) {
        String p = paiement == null ? "" : paiement.trim();
        BaseColor color;
        String label;

        if ("Tout payé".equalsIgnoreCase(p) || "Tout paye".equalsIgnoreCase(p)) {
            color = SUCCESS;
            label = "Tout payé";
        } else if ("Avec avance".equalsIgnoreCase(p)) {
            color = WARNING;
            label = "Partiel";
        } else {
            color = DANGER;
            label = "Non payé";
        }

        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(bg);
        outer.setBorderColor(BORDER);
        outer.setPadding(6f);
        outer.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell badge = new PdfPCell(new Phrase(label, BADGE));
        badge.setBackgroundColor(color);
        badge.setBorder(Rectangle.NO_BORDER);
        badge.setPadding(5f);
        badge.setHorizontalAlignment(Element.ALIGN_CENTER);
        badge.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPTable wrap = new PdfPTable(1);
        wrap.setWidthPercentage(100);
        wrap.addCell(badge);
        outer.addElement(wrap);
        return outer;
    }

    private void addFooter(Document doc, LocalDateTime generatedAt) throws DocumentException {
        doc.add(Chunk.NEWLINE);

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell hr = new PdfPCell(new Phrase(" "));
        hr.setFixedHeight(1f);
        hr.setBorder(Rectangle.NO_BORDER);
        hr.setBackgroundColor(BORDER);
        line.addCell(hr);
        line.setSpacingBefore(8f);
        line.setSpacingAfter(8f);
        doc.add(line);

        PdfPTable footer = new PdfPTable(new float[]{2f, 1.2f});
        footer.setWidthPercentage(100);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        Paragraph thanks = new Paragraph();
        thanks.add(new Chunk("🔒 Merci de voyager avec nous !\n", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, NAVY)));
        thanks.add(new Chunk("Votre satisfaction est notre priorité.", TXT));
        left.addElement(thanks);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph gen = new Paragraph();
        gen.setAlignment(Element.ALIGN_RIGHT);
        gen.add(new Chunk("Généré le: " + generatedAt.toLocalDate() + "\n", TXT));
        gen.add(new Chunk("Heure: " + generatedAt.toLocalTime().withNano(0) + "\n", TXT));
        gen.add(new Chunk("Document généré automatiquement", SMALL));
        right.addElement(gen);

        footer.addCell(left);
        footer.addCell(right);
        doc.add(footer);
    }

    private static class PremiumPageEvent extends PdfPageEventHelper {
        private final String reportNo;
        private PdfTemplate totalTpl;
        private final Font pageFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);

        private PremiumPageEvent(String reportNo) {
            this.reportNo = reportNo;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalTpl = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            String text = "Page " + writer.getPageNumber() + " / ";
            PdfContentByte cb = writer.getDirectContent();
            float x = document.right();
            float y = document.bottom() - 18;
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(text, pageFont), x, y, 0);
            cb.addTemplate(totalTpl, x + 2, y - 6);

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(reportNo, pageFont), document.left(), y, 0);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(totalTpl, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1), pageFont),
                    0, 6, 0);
        }
    }

    private static class FontFactoryHelper {
        static com.itextpdf.text.pdf.BaseFont base() throws DocumentException, IOException {
            try {
                return com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.WINANSI, com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new DocumentException(e);
            }
        }
    }
}
