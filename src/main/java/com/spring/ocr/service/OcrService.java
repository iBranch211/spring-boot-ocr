package com.spring.ocr.service;

import com.spring.ocr.model.response.ImageTextDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OcrService {

    ImageTextDto extractTextFromImage(MultipartFile file) throws IOException;
}
