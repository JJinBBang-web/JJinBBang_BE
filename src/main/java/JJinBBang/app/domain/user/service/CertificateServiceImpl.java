package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.*;
import JJinBBang.app.domain.user.repository.UsersRepository;
import JJinBBang.app.global.common.enums.VerificationStatus;
import JJinBBang.app.global.config.GoogleProperties;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final GoogleProperties googleProps;
    private final Drive drive;
    private final Sheets sheets;
    private final UsersRepository usersRepository;
    private final UsersService usersService;

    // 재학증명서 -> 구글 드라이브 업로드
    @Override
    public String uploadEnrollmentFileToDrive(MultipartFile file, String folderName) {
        try {
            String rootFolderId = googleProps.getDrive().getFolders().get("enrollment");
            String targetFolderId = findTargetFolderId(rootFolderId, folderName);
            return uploadFile(file, targetFolderId);
        } catch (IOException e) {
            throw new RuntimeException("구글 드라이브 타겟 폴더 탐색 실패");
        }
    }

    // 합격증명서 -> 구글 드라이브 업로드
    @Override
    public String uploadAdmissionFileToDrive(MultipartFile file, String folderName) {
        try {
            String rootFolderId = googleProps.getDrive().getFolders().get("admission");
            String targetFolderId = findTargetFolderId(rootFolderId, folderName);
            return uploadFile(file, targetFolderId);
        } catch (IOException e) {
            throw new RuntimeException("구글 드라이브 타겟 폴더 탐색 실패");
        }
    }

    // 재학증명서 업로드 -> 스프레드시트 추가
    @Override
    public void appendEnrollmentFileToSheets(int userId, int universityId, String fileName, String fileLink) {
        appendSheets("enrollment", userId, universityId, fileName, fileLink);
    }

    // 합격증명서 업로드 -> 스프레드시트 추가
    @Override
    public void appendAdmissionFileToSheets(int userId, int universityId, String fileName, String fileLink) {
        appendSheets("admission", userId, universityId, fileName, fileLink);
    }



    /**
     * 업로드 파일 검증
     */
    private void verifyFileType(MultipartFile file, String type) {
        // 업로드 대상 파일 존재 확인
        if (file == null || file.isEmpty()) {
            throw CertificateBadRequestException.FileUploadException();
        }

        // 파일 타입 검사
        if (type == null || !(type.toLowerCase().startsWith("image/") || type.equalsIgnoreCase("application/pdf"))) {
            throw CertificateBadRequestException.FileUploadException();
        }
    }

    /**
     * 폴더 이름으로 root에 포함된 폴더 찾기
     */
    private String findTargetFolderId(String rootFolderId, String folderName) throws IOException {
        FileList result = drive.files().list()
                .setQ(String.format(
                        "'%s' in parents and mimeType = 'application/vnd.google-apps.folder' and name = '%s' and trashed = false",
                        rootFolderId, folderName
                ))
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("하위 폴더 '" + folderName + "'를 찾을 수 없습니다.");
        }

        return files.get(0).getId(); // 첫 번째 매칭된 폴더 ID
    }

    /**
     * google drive 호출 후 파일 업로드 -> 파일
     */
    private File setGoogleDrive(
            MultipartFile file,
            String targetFolderId,
            String type
    ) throws IOException {

        File metadata = new File()
                .setName(file.getOriginalFilename())
                .setParents(Collections.singletonList(targetFolderId));

        InputStreamContent content = new InputStreamContent(
                type,
                file.getInputStream()
        );

        // google drive api 호출 -> 파일 업로드, 파일 ID 반환
        File uploaded = drive.files()
                .create(metadata, content)
                .setFields("id")
                .execute();

        // 링크 공유 권한 설정
        drive.permissions()
                .create(uploaded.getId(), new Permission().setType("anyone").setRole("reader"))
                .execute();

        return uploaded;
    }


    /**
     * Google Drive 파일 업로드
     */
    private String uploadFile(
            MultipartFile file,
            String targetFolderId
    ) {
        String contentType = file.getContentType();

        // 증명서 파일 검증
        verifyFileType(file, contentType);

        try {
            File uploaded = setGoogleDrive(file, targetFolderId, contentType);
            return String.format(
                    googleProps.getDrive().getUrlTemplate(),
                    uploaded.getId()
            );

        } catch (GoogleJsonResponseException e) {
            switch (e.getStatusCode()) {
                case 400:
                    throw CertificateBadRequestException.DriveAPIException();
                case 401:
                case 403:
                    throw UserAuthException.InvalidToken();
                default:
                    throw CertificateProcessException.DriveProcessException();
            }
        } catch (IOException e) {
            throw CertificateProcessException.DriveProcessException();
        }
    }

    private void appendSheets(
            String type,
            int userId,
            int universityId,
            String fileName,
            String fileLink
    ) {
        String spreadsheetId = googleProps.getSpreadsheet().getId(); // 스프레드시트
        String sheetName = googleProps.getSpreadsheet().getSheets().get(type); // 스프레드시트의 탭
        String range = String.format(googleProps.getSpreadsheet().getRangeTemplate(), sheetName); // 시트 범위 포맷팅
        String hyperlinkFormula = String.format("=HYPERLINK(\"%s\",\"%s\")", fileLink, fileName); // url 포뮬러

        List<Object> row = List.of(
                userId,
                universityId,
                hyperlinkFormula,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        try {
            // 시트 api 호출
            sheets.spreadsheets()
                    .values()
                    .append(spreadsheetId, range, new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (GoogleJsonResponseException e) {
            switch (e.getStatusCode()) {
                case 400:
                    throw CertificateBadRequestException.SheetsRequestException();
                case 401:
                case 403:
                    throw UserAuthException.InvalidToken();
                default:
                    throw CertificateProcessException.SheetsProcessException();
            }
        } catch (IOException e) {
            throw CertificateProcessException.SheetsProcessException();
        }
    }

    private String findOrCreateFolder(String parentId, String folderName) throws IOException {
        try {
            String query = String.format(
                    "mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents",
                    folderName, parentId
            );

            FileList result = drive.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id)")
                    .execute();

            if (!result.getFiles().isEmpty()) {
                return result.getFiles().get(0).getId();
            }

            File folderMeta = new File()
                    .setName(folderName)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(Collections.singletonList(parentId));

            return drive.files().create(folderMeta)
                    .setFields("id")
                    .execute()
                    .getId();
        } catch (IOException e) {
            log.error("[Drive 폴더 생성/조회 중 IO 오류]", e);
            throw e;
        }
    }

    /**
     * 사용자 ID와 인증 상태 입력 -> 사용자 인증 상태 변경
     */
    @Override
    @Transactional
    public void updateVerificationStatusByCertificate(Long userId, String status) {
        if (!VerificationStatus.isValid(status)) {
            throw VerificatoinStatusException.InvalidVerificationStatusException();
        }
        Users user = usersService.findByUserId(userId);
        user.updateVerificationStatus(VerificationStatus.valueOf(status));
        usersRepository.save(user);
    }
}