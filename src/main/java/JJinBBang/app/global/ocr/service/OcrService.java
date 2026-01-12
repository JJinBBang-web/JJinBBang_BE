package JJinBBang.app.global.ocr.service;

import JJinBBang.app.global.ocr.dto.response.OcrResult;

public interface OcrService {
    OcrResult extractTextFromGoogleDrive(String imageUrl, String fileName);
}
