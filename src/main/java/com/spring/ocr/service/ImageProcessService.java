package com.spring.ocr.service;

import java.awt.image.BufferedImage;

public interface ImageProcessService {

    BufferedImage preprocessImage(byte[] image);


}
