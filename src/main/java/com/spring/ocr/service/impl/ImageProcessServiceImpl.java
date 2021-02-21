package com.spring.ocr.service.impl;

import com.spring.ocr.service.ImageProcessService;
import com.spring.ocr.util.OcrUtil;
import com.spring.ocr.util.OpencvUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageProcessServiceImpl implements ImageProcessService {

    @Override
    public BufferedImage preprocessImage(byte[] image) {

        Mat img = OpencvUtil.byteArrayToMat(image);

        Mat preprocessMat = OcrUtil.preprocess(img);

        return OpencvUtil.matToBufImg(preprocessMat,".jpg");
    }
}
