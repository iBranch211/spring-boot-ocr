package com.spring.ocr.util;

import lombok.experimental.UtilityClass;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@UtilityClass
public class OcrUtil {

    /**
     * Convert Byte Array to Mat
     *
     * @param original
     *            Byte Array to be converted
     * @return
     */
    public BufferedImage byteArrayToMat(byte[] original) {
        ByteArrayInputStream bais = new ByteArrayInputStream(original);
        BufferedImage bufImage = null;
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
           e.printStackTrace();
        }
        return bufImage;
    }

    /**
     * OCR preprocessing
     * @return
     */
    public Mat preprocess(Mat mat){
        //Grayscale
        mat=OpencvUtil.gray(mat);

        Imgcodecs.imwrite("test/gray.png", mat);

        //gaussianBlur
        mat=OpencvUtil.gaussianBlur(mat);

        Imgcodecs.imwrite("test/gaussian-blur.png", mat);


        //Remove Noise
        mat=OpencvUtil.navieRemoveNoise(mat,1);

        Imgcodecs.imwrite("test/noiseless.png", mat);


        return mat;
    }

}
