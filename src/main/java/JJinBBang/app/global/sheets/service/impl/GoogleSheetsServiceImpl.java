package JJinBBang.app.global.sheets.service.impl;

import JJinBBang.app.global.sheets.dto.UnregisterReasonDto;
import JJinBBang.app.global.sheets.dto.UserOpinionDto;
import JJinBBang.app.global.sheets.properties.GoogleProperties;
import JJinBBang.app.global.sheets.service.GoogleSheetsService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private final Sheets googleSheets;
    private final GoogleProperties googleProperties;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 시트 데이터 입력
    private AppendValuesResponse appendRowToGoogleSheets(
            GoogleProperties.Spreadsheet sheet,
            String sheetName,
            List<Object> data
    ) throws IOException {
        String sheetsId = sheet.getId();
        String name = sheet.getSheets().get(sheetName);
        String range = String.format(sheet.getRangeTemplate(), name);

        ValueRange row = new ValueRange().setValues(List.of(data));

        return googleSheets.spreadsheets().values()
                .append(sheetsId, range, row)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }


    // 탈퇴 사유
    @Override
    public void appendUnregisterReason(
            UnregisterReasonDto unregisterReasonDto
    ) throws IOException {
        var sheet = googleProperties.getSpreadsheetUnregister();

        List<Object> row = List.of(
                unregisterReasonDto.userId(),
                unregisterReasonDto.option(),
                unregisterReasonDto.unregisterReason(),
                unregisterReasonDto.registeredAt().format(DATE_TIME_FORMATTER),
                unregisterReasonDto.unregisteredAt().format(DATE_TIME_FORMATTER)
        );

        appendRowToGoogleSheets(sheet, "unregister-reason", row);
    }


    // 문의 내용
    @Override
    public void appendUserOpinion(
            UserOpinionDto userOpinionDto
    ) throws IOException {
        var sheet = googleProperties.getSpreadsheetOpinion();

        List<Object> row = List.of(
                userOpinionDto.userId(),
                userOpinionDto.opinion(),
                userOpinionDto.timestamp().format(DATE_TIME_FORMATTER)
        );

        appendRowToGoogleSheets(sheet, "user-opinion", row);
    }
}
