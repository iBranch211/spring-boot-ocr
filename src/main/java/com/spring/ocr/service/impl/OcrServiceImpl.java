package com.spring.ocr.service.impl;

import com.spring.ocr.model.response.ImageTextDto;
import com.spring.ocr.service.ImageProcessService;
import com.spring.ocr.service.OcrService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OcrServiceImpl implements OcrService {

    final Tesseract tesseract;
    final ImageProcessService imageProcessService;

    @Override
    public ImageTextDto extractTextFromImage(MultipartFile file) throws IOException {
        try {

            BufferedImage bufferedImage = imageProcessService.preprocessImage(file.getBytes());
            String text = tesseract.doOCR(bufferedImage);
            return ImageTextDto.builder()
                    .fileName(file.getOriginalFilename())
                    .text(text)
                    .build();

        } catch (TesseractException e) {
            log.error("Tesseract error: " + e.getMessage());
            return ImageTextDto.builder()
                    .fileName(file.getOriginalFilename())
                    .text("")
                    .build();
        }
    }

}
