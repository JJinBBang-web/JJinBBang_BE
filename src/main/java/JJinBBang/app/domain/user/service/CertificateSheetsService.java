package JJinBBang.app.domain.user.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class CertificateSheetsService {
    private final Sheets sheets;
    private final String spreadsheetId;

    public CertificateSheetsService(Sheets sheets, String spreadsheetId) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
    }

    public void appendCertificateSheet(
            String sheetName,
            int univerityId,
            String studentNumber,
            String fileName,
            String shareLink
    ) throws IOException {
        String formula = String.format(
                "=HYPERLINK(\"%s\",\"%s\")",
                shareLink,
                fileName
        );

        List<Object> row = List.<Object>of(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                univerityId,
                studentNumber,
                formula
        );

        var body = new ValueRange().setValues(List.of(row));

        String sheetRange = String.format("'%s'!A:D", sheetName);

        sheets.spreadsheets()
                .values()
                .append(spreadsheetId, sheetRange, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}
