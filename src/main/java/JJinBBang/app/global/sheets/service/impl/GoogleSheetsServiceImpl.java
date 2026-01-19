package JJinBBang.app.global.sheets.service.impl;

import JJinBBang.app.domain.common.dto.request.ReviewEventRequest;
import JJinBBang.app.global.sheets.GoogleInternalException;
import JJinBBang.app.global.sheets.dto.UnregisterReasonDto;
import JJinBBang.app.global.sheets.dto.UserOpinionDto;
import JJinBBang.app.global.sheets.enums.OpinionType;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private final Sheets googleSheets;
    private final GoogleProperties googleProperties;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 시트 데이터 입력
    private AppendValuesResponse appendRowToGoogleSheets(
            GoogleProperties.SheetConfig sheetConfig,
            String sheetKey,
            List<Object> data
    ) throws IOException {
        String sheetsId = sheetConfig.getId();
        String name = sheetConfig.getSheets().get(sheetKey);
        String range = String.format(sheetConfig.getRangeTemplate(), name);

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
        var sheet = googleProperties.getSpreadsheet().getUnregister();

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
            UserOpinionDto userOpinionDto,
            OpinionType opinionType
    ) throws IOException {
        Objects.requireNonNull(userOpinionDto, "userOpinionDto is null");

        var sheet = googleProperties.getSpreadsheet().getOpinion();
        String sheetKey = resolveOpinionSheetKey(opinionType);

        List<Object> row = List.of(
                userOpinionDto.userId(),
                userOpinionDto.targetId(),
                userOpinionDto.opinion(),
                userOpinionDto.timestamp().format(DATE_TIME_FORMATTER)
        );

        appendRowToGoogleSheets(sheet, sheetKey, row);
    }

    // 문의 타입에 따라 시트 선택
    private String resolveOpinionSheetKey(OpinionType opinionType) {
        if (opinionType == null) throw new IllegalArgumentException("opinionType is null");

        return switch (opinionType) {
            case BUILDING_REPORT -> "user-building-opinion";
            case REVIEW_REPORT ->  "user-review-opinion";
        };
    }

    // 리뷰 저장 (지거국 확장 이벤트)
    @Override
    public void appendReviewFromEvent(ReviewEventRequest req) {
        try {
            var sheet = googleProperties.getSpreadsheet().getReviewEvent();

            String posKeywords = String.join(", ", req.review().positiveKeywords());
            String negKeywords = String.join(", ", req.review().negativeKeywords());
            String imageLinks = req.review().images() != null ? String.join("\n", req.review().images()) : "";

            List<Object> row = List.of(
                    req.phoneNumber(),
                    req.review().university(),
                    req.review().contractType(),
                    req.review().deposit(),
                    req.review().monthlyRent(),
                    req.review().administrationCost(),
                    posKeywords,
                    negKeywords,
                    imageLinks,
                    req.review().content(),
                    req.hasAgreedToMarketing(),
                    req.hasAgreedToPrivacy(),
                    java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER)
            );

            appendRowToGoogleSheets(sheet, "review-event", row);
        } catch (IOException e) {
            log.error("리뷰이벤트 후기를 저장에 실패했습니다.: {}", e.getMessage());
            throw GoogleInternalException.apiError();
        }
    }
}
