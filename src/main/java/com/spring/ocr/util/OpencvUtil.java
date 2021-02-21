package com.spring.ocr.util;

import lombok.experimental.UtilityClass;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class OpencvUtil {

    static final int BLACK = 0;
    static final int WHITE = 255;

    /**
     * Grayscale
     * @return
     */
    public Mat gray(Mat mat){
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY,1);
        return gray;
    }

    /**
     * Binarization
     * @return
     */
    public Mat binary(Mat mat){
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(mat, binary, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 25, 10);
        return binary;
    }

    /**
     * Blur processing
     * @param mat
     * @return
     */
    public Mat blur(Mat mat) {
        Mat blur = new Mat();
        Imgproc.blur(mat,blur,new Size(5,5));
        return blur;
    }

    /**
     * Gaussian Blur processing
     * @param mat
     * @return
     */
    public Mat gaussianBlur(Mat mat) {
        Mat gaussianBlur = new Mat();
        Imgproc.GaussianBlur(mat,gaussianBlur, new Size(3, 3),0);
        return gaussianBlur;
    }

    /**
     * Sobel edge detection
     * @param mat
     * @return
     */
    public Mat sobel(Mat mat) {
        Mat sobel = new Mat();
        Imgproc.Sobel(mat, sobel, -1, 1, 0);
        return sobel;
    }

    /**
     * Otsu threshold
     * @param mat
     * @return
     */
    public Mat otsu(Mat mat) {
        Mat otsu = new Mat();
        Imgproc.threshold(mat, otsu, 0, 255,  3);
        return otsu;
    }

    /**
     *Dilate
     * @param mat
     * @return
     */
    public Mat dilate(Mat mat,int size){
        Mat dilate=new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));

        Imgproc.dilate(mat, dilate, element, new Point(-1, -1), 1);
        return dilate;
    }

    /**
     * Erode
     * @param mat
     * @return
     */
    public Mat erode(Mat mat,int size){
        Mat erode=new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size,size));

        Imgproc.erode(mat, erode, element, new Point(-1, -1), 1);
        return erode;
    }

    /**
     * Edge detection
     * @param mat
     * @return
     */
    public Mat carry(Mat mat){
        Mat dst=new Mat();
        Imgproc.GaussianBlur(mat, dst, new Size(3,3), 0);

        Imgproc.Canny(mat, dst, 50, 150);
        return dst;
    }

    /**
     * Contour detection
     * @param mat
     * @return
     */
    public List<MatOfPoint> findContours(Mat mat){
        List<MatOfPoint> contours=new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }


    /**
     * Clear small area outline
     * @param mat
     * @param size
     * @return
     */
    public Mat drawContours(Mat mat,int size){
        List<MatOfPoint> cardContours=OpencvUtil.findContours(mat);
        for (int i = 0; i < cardContours.size(); i++)
        {
            double area=OpencvUtil.area(cardContours.get(i));
            if(area<size){
                Imgproc.drawContours(mat, cardContours, i, new Scalar( 0, 0, 0),-1 );
            }
        }
        return mat;
    }

    /**
     * Cut ID area
     * @param mat
     */
    public Mat houghLinesP(Mat begin,Mat mat){
        //Grayscale
        mat=OpencvUtil.gray(mat);
        //Binarization
        mat=OpencvUtil.binary(mat);
        //Erode
        mat=OpencvUtil.erode(mat,5);
        //Edge detection
        mat=OpencvUtil.carry(mat);
        //Noise reduction
        mat=OpencvUtil.navieRemoveNoise(mat,1);
        //Dilate
        mat=OpencvUtil.dilate(mat,3);
        //Contour detection, clear small contour parts
        List<MatOfPoint> contours=OpencvUtil.findContours(mat);
        for(int i=0;i<contours.size();i++){
            double area=OpencvUtil.area(contours.get(i));
            if(area<5000){
                Imgproc.drawContours(mat, contours, i, new Scalar( 0, 0, 0), -1);
            }
        }
        Mat storage = new Mat();
        Imgproc.HoughLinesP(mat, storage, 1, Math.PI / 180, 10, 0, 10);
        double[] maxLine = new double[]{0,0,0,0};
        //Get the longest line
        for (int x = 0; x < storage.rows(); x++)
        {
            double[] vec = storage.get(x, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            double newLength = Math.sqrt(Math.abs((x1 - x2)* (x1 - x2)+(y1 - y2)* (y1 - y2)));
            double oldLength = Math.sqrt(Math.abs((maxLine[0] - maxLine[2])* (maxLine[0] - maxLine[2])+(maxLine[1] - maxLine[3])* (maxLine[1] - maxLine[3])));
            if(newLength>oldLength){
                maxLine = vec;
            }
        }
        //Calculate the angle of the longest line
        double angle = getAngle(maxLine[0],maxLine[1],maxLine[2],maxLine[3]);
        //Rotation angle
        mat = rotate3( mat,angle);
        begin = rotate3( begin,angle);

        Imgproc.HoughLinesP(mat, storage, 1, Math.PI / 180, 10, 10, 10);
        List<double[]> lines=new ArrayList<>();
        //Draw a line on the mat
        for (int x = 0; x < storage.rows(); x++)
        {
            double[] vec = storage.get(x, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            //Get a straight line approximately parallel to the edge of the image x
            if(Math.abs(start.y-end.y)<5){
                if(Math.abs(x2-x1)>20){
                    lines.add(vec);
                }
            }
            //Get a straight line approximately parallel to the y edge of the image
            if(Math.abs(start.x-end.x)<5){
                if(Math.abs(y2-y1)>20){
                    lines.add(vec);
                }
            }
        }
        //Get the largest and smallest X, Y coordinates
        double maxX=0.0,minX=10000,minY=10000,maxY=0.0;
        for(int i=0;i<lines.size();i++){
            double[] vec = lines.get(i);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            maxX=maxX>x1?maxX:x1;
            maxX=maxX>x2?maxX:x2;
            minX=minX>x1?x1:minX;
            minX=minX>x2?x2:minX;
            maxY=maxY>y1?maxY:y1;
            maxY=maxY>y2?maxY:y2;
            minY=minY>y1?y1:minY;
            minY=minY>y2?y2:minY;
        }
        if(maxX<mat.cols()&&minX>0&&maxY<mat.rows()&&minY>0){
            List<Point> list=new ArrayList<>();
            Point point1=new Point(minX+10,minY+10);
            Point point2=new Point(minX+10,maxY-10);
            Point point3=new Point(maxX-10,minY+10);
            Point point4=new Point(maxX-10,maxY-10);
            list.add(point1);
            list.add(point2);
            list.add(point3);
            list.add(point4);
            mat=shear(begin,list);
        }else{
            mat=begin;
        }
        return mat;
    }


    /**
     * Calculated angle
     * @param px1
     * @param py1
     * @param px2
     * @param py2
     * @return
     */
    public double getAngle(double px1, double py1, double px2, double py2) {
        //The x and y values of two points
        double x = px2-px1;
        double y = py2-py1;
        double hypotenuse = Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
        //The length of the hypotenuse
        double cos = x/hypotenuse;
        double radian = Math.acos(cos);
        //Find the radians
        double angle = 180/(Math.PI/radian);
        //Calculate the angle in radians
        if (y<0) {
            angle = -angle;
        } else if ((y == 0) && (x<0)) {
            angle = 180;
        }
        while (angle<0){
            angle = angle +90;
        }
        return angle;
    }

    /**
     * Cumulative probability hough transform straight line detection
     * @param mat
     */
    public Mat houghLines(Mat mat){
        Mat storage = new Mat();
        Imgproc.HoughLines(mat, storage, 1, Math.PI / 180, 50, 0, 0, 0, 1);
        for (int x = 0; x < storage.rows(); x++) {
            double[] vec = storage.get(x, 0);

            double rho = vec[0];
            double theta = vec[1];

            Point pt1 = new Point();
            Point pt2 = new Point();

            double a = Math.cos(theta);
            double b = Math.sin(theta);

            double x0 = a * rho;
            double y0 = b * rho;

            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));

            if (theta >= 0)
            {
                Imgproc.line(mat, pt1, pt2, new Scalar(255), 3);
            }
        }
        return mat;
    }

    /**
     * Capture template pictures based on four-point coordinates
     * @param mat
     * @param pointList
     * @return
     */
    public Mat shear(Mat mat,List<Point> pointList){
        int x=minX(pointList);
        int y=minY(pointList);
        int xl=xLength(pointList)>mat.cols()-x?mat.cols()-x:xLength(pointList);
        int yl=yLength(pointList)>mat.rows()-y?mat.rows()-y:yLength(pointList);
        Rect re=new Rect(x,y,xl,yl);
        return new Mat(mat,re);
    }

    /**
     * Picture rotation
     * @param splitImage
     * @param angle
     * @return
     */
    public Mat rotate3(Mat splitImage, double angle){
        double thera = angle * Math.PI / 180;
        double a = Math.sin(thera);
        double b = Math.cos(thera);

        int wsrc = splitImage.width();
        int hsrc = splitImage.height();

        int wdst = (int) (hsrc * Math.abs(a) + wsrc * Math.abs(b));
        int hdst = (int) (wsrc * Math.abs(a) + hsrc * Math.abs(b));
        Mat imgDst = new Mat(hdst, wdst, splitImage.type());

        Point pt = new Point(splitImage.cols() / 2, splitImage.rows() / 2);

        Mat affineTrans = Imgproc.getRotationMatrix2D(pt, angle, 1.0);

        affineTrans.put(0, 2, affineTrans.get(0, 2)[0] + (wdst - wsrc) / 2);
        affineTrans.put(1, 2, affineTrans.get(1, 2)[0] + (hdst - hsrc) / 2);

        Imgproc.warpAffine(splitImage, imgDst, affineTrans, imgDst.size(),
                Imgproc.INTER_CUBIC | Imgproc.WARP_FILL_OUTLIERS);
        return imgDst;
    }

    /**
     * Image histogram processing
     * @param mat
     * @return
     */
    public Mat equalizeHist(Mat mat){
        Mat dst = new Mat();
        List<Mat> mv = new ArrayList<>();
        Core.split(mat, mv);
        for (int i = 0; i < mat.channels(); i++)
        {
            Imgproc.equalizeHist(mv.get(i), mv.get(i));
        }
        Core.merge(mv, dst);
        return dst;
    }

    /**
     * 8 neighborhood noise reduction is a bit like 9-square grid noise reduction; that is, if the center of the 9-square grid is surrounded by different colors, it will be assimilated
     * @param pNum default value is 1
     */
    public Mat navieRemoveNoise(Mat mat,int pNum) {
        int i, j, m, n, nValue, nCount;
        int nWidth = mat.cols();
        int nHeight = mat.rows();

        for (j = 1; j < nHeight - 1; ++j) {
            for (i = 1; i < nWidth - 1; ++i) {
                nValue =  (int)mat.get(j, i)[0];
                if (nValue == 0) {
                    nCount = 0;

                    // Compare the 9-square grid with (j ,i) as the center, if the surroundings are white, assimilate
                    for (m = j - 1; m <= j + 1; ++m) {
                        for (n = i - 1; n <= i + 1; ++n) {
                            if ((int)mat.get(m, n)[0] == 0) {
                                nCount++;
                            }
                        }
                    }
                    if (nCount <= pNum) {
                        // The number of surrounding black points is less than the threshold pNum, set the point to white
                        mat.put(j, i, WHITE);
                    }
                } else {
                    nCount = 0;
                    for (m = j - 1; m <= j + 1; ++m) {
                        for (n = i - 1; n <= i + 1; ++n) {
                            if ((int)mat.get(m, n)[0] == 0) {
                                nCount++;
                            }
                        }
                    }
                    if (nCount >= 7) {
                        // The number of black points around is greater than or equal to 7, set the point to black; that is, the surroundings are all black
                        mat.put(j, i, BLACK);
                    }
                }
            }
        }
        return mat;
    }

    /**
     * Connected domain noise reduction
     * @param pArea The default value is 1
     */
    public Mat contoursRemoveNoise(Mat mat,double pArea) {
        //mat=floodFill(mat,mat.new Point(mat.cols()/2,mat.rows()/2),new Color(225,0,0));
        int i, j, color = 1;
        int nWidth =  mat.cols(), nHeight = mat.rows();

        for (i = 0; i < nWidth; ++i) {
            for (j = 0; j < nHeight; ++j) {
                if ((int) mat.get(j, i)[0] == BLACK) {
                    //Fill each black dot in the link area with a different color
                    Imgproc.floodFill(mat, new Mat(), new Point(i, j), new Scalar(color));
                    color++;
                }
            }
        }

        //Count the number of different color points
        int[] ColorCount = new int[255];

        //Remove noise
        for (i = 0; i < nWidth; ++i) {
            for (j = 0; j < nHeight; ++j) {
                if ((int) mat.get(j, i)[0] != 255) {
                    ColorCount[(int) mat.get(j, i)[0] - 1]++;
                }
            }
        }

        for (i = 0; i < nWidth; ++i) {
            for (j = 0; j < nHeight; ++j) {
                if (ColorCount[(int) mat.get(j, i)[0] - 1] <= pArea) {
                    mat.put(j, i, WHITE);
                }
            }
        }

        for (i = 0; i < nWidth; ++i) {
            for (j = 0; j < nHeight; ++j) {
                if ((int) mat.get(j, i)[0] < WHITE) {
                    mat.put(j, i, BLACK);
                }
            }
        }
        return mat;
    }

    /**
     * Get the vertex coordinates of the contour
     * @param contour
     * @return
     */
    public List<Point> getPointList(MatOfPoint contour){
        MatOfPoint2f mat2f=new MatOfPoint2f();
        contour.convertTo(mat2f,CvType.CV_32FC1);
        RotatedRect rect=Imgproc.minAreaRect(mat2f);
        Mat points=new Mat();
        Imgproc.boxPoints(rect,points);
        return getPoints(points.dump());
    }

    /**
     * Get the area of the contour
     * @param contour
     * @return
     */
    public double area (MatOfPoint contour){
        MatOfPoint2f mat2f=new MatOfPoint2f();
        contour.convertTo(mat2f,CvType.CV_32FC1);
        RotatedRect rect=Imgproc.minAreaRect(mat2f);
        return rect.boundingRect().area();
    }

    /**
     * Get a collection of point coordinates
     * @param str
     * @return
     */
    public List<Point> getPoints(String str){
        List<Point> points=new ArrayList<>();
        str=str.replace("[","").replace("]","");
        String[] pointStr=str.split(";");
        for(int i=0;i<pointStr.length;i++){
            double x=Double.parseDouble(pointStr[i].split(",")[0]);
            double y=Double.parseDouble(pointStr[i].split(",")[1]);
            Point po=new Point(x,y);
            points.add(po);
        }
        return points;
    }

    /**
     * Get the smallest X coordinate
     * @param points
     * @return
     */
    public int minX(List<Point> points){
        Collections.sort(points, new XComparator(false));
        return (int)(points.get(0).x>0?points.get(0).x:-points.get(0).x);
    }

    /**
     * Get the smallest Y coordinate
     * @param points
     * @return
     */
    public int minY(List<Point> points){
        Collections.sort(points, new YComparator(false));
        return (int)(points.get(0).y>0?points.get(0).y:-points.get(0).y);
    }


    /**
     * Get the longest X coordinate distance
     * @param points
     * @return
     */
    public int xLength(List<Point> points){
        Collections.sort(points, new XComparator(false));
        return (int)(points.get(3).x-points.get(0).x);
    }

    /**
     * Get the longest Y coordinate distance
     * @param points
     * @return
     */
    public  int yLength(List<Point> points){
        Collections.sort(points, new YComparator(false));
        return (int)(points.get(3).y-points.get(0).y);
    }

    /**
     * Convert BufferedImage to Mat
     *
     * @param matrix
     *            Mat to be converted
     * @param fileExtension
     *            The format is ".jpg", ".png", etc
     * @return
     */
    public BufferedImage matToBufImg (Mat matrix, String fileExtension) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

    /**
     * Convert BufferedImage to Mat
     *
     * @param original
     *            The BufferedImage to be converted
     * @param imgType
     *            The type of bufferedImage such as BufferedImage.TYPE_3BYTE_BGR
     * @param matType
     *            Converted to mat type such as CvType.CV_8UC3
     */
    public Mat bufImgToMat (BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }
        if (original.getType() != imgType) {
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        DataBufferByte dbi =(DataBufferByte)original.getRaster().getDataBuffer();
        byte[] pixels = dbi.getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }

    /**
     * Convert Byte Array to Mat
     *
     * @param original
     *            Byte Array to be converted
     * @return
     */
    public Mat byteArrayToMat (byte[] original) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(original), Imgcodecs.IMREAD_UNCHANGED);
        return mat;
    }


}
