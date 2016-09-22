package com.ljy.tools;

import com.google.common.base.Charsets;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class QRCodeTools {

    public static final String QRCODE_DEFAULT_CHARSET = "UTF-8";

    public static final int QRCODE_DEFAULT_HEIGHT = 150;

    public static final int QRCODE_DEFAULT_WIDTH = 150;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * Create qrcode with default settings
     *
     * @param data
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCode(String data) {
        return createQRCode(data, QRCODE_DEFAULT_WIDTH, QRCODE_DEFAULT_HEIGHT);
    }

    /**
     * Create qrcode with default charset
     *
     * @param data
     * @param width
     * @param height
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCode(String data, int width, int height) {
        return createQRCode(data, QRCODE_DEFAULT_CHARSET, width, height);
    }

    /**
     * Create qrcode with specified charset
     *
     * @param data
     * @param charset
     * @param width
     * @param height
     * @return
     * @author stefli
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BufferedImage createQRCode(String data, String charset, int width, int height) {
        Map hint = new HashMap();
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.CHARACTER_SET, charset);

        return createQRCode(data, charset, hint, width, height);
    }

    /**
     * Create qrcode with specified hint
     *
     * @param data
     * @param charset
     * @param hint
     * @param width
     * @param height
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCode(String data, String charset, Map<EncodeHintType, ?> hint, int width,
                                             int height) {
        BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE,
                    width, height, hint);
            return toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /**
     * Create qrcode with default settings and logo
     *
     * @param data
     * @param logoFile
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCodeWithLogo(String data, File logoFile) {
        return createQRCodeWithLogo(data, QRCODE_DEFAULT_WIDTH, QRCODE_DEFAULT_HEIGHT, logoFile);
    }

    /**
     * Create qrcode with default charset and logo
     *
     * @param data
     * @param width
     * @param height
     * @param logoFile
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCodeWithLogo(String data, int width, int height, File logoFile) {
        return createQRCodeWithLogo(data, QRCODE_DEFAULT_CHARSET, width, height, logoFile);
    }

    /**
     * Create qrcode with specified charset and logo
     *
     * @param data
     * @param charset
     * @param width
     * @param height
     * @param logoFile
     * @return
     * @author stefli
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BufferedImage createQRCodeWithLogo(String data, String charset, int width, int height, File logoFile) {
        Map hint = new HashMap();
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.CHARACTER_SET, charset);

        return createQRCodeWithLogo(data, charset, hint, width, height, logoFile, 0);
    }

    /**
     * 生成带logo的二维码图片
     *
     * @param data 扫描二维码之后获得的数据，通常为一个url；
     * @param charset 字符编码 默认UTF-8
     * @param hint  二维码图片的属性设置
     * @param width 生成图片的寬度
     * @param height    生成图片的高度
     * @param logoFile  需要画到二维码图片中间的logo
     * @param logoRatio logo占整个图片的大小比例,默认为5，即高宽都为整个图片的1/5
     * @return
     * @author stefli
     */
    public static BufferedImage createQRCodeWithLogo(String data, String charset, Map<EncodeHintType, ?> hint,
                                                     int width, int height, File logoFile, int logoRatio) {
        try {
            logoRatio = logoRatio > 0 ? logoRatio : 5;

            BufferedImage qrcode = createQRCode(data, charset, hint, width, height);
            BufferedImage logo = ImageIO.read(logoFile);

            BufferedImage combined = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();
            g.drawImage(qrcode, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(zoomImage(logo, width / logoRatio,height / logoRatio), Math.round(width / 2 - width / logoRatio / 2), Math.round(height / 2 - height / logoRatio / 2), null);

            return combined;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Return base64 for image
     *
     * @param image
     * @return
     * @author stefli
     */
    public static String getImageBase64String(BufferedImage image) {
        String result = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStream b64 = new Base64OutputStream(os);
            ImageIO.write(image, "png", b64);
            result = os.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Decode the base64Image data to image
     *
     * @param base64ImageString
     * @param file
     * @author stefli
     */
    public static void convertBase64StringToImage(String base64ImageString, File file) {
        FileOutputStream os;
        try {
            Base64 d = new Base64();
            byte[] bs = d.decode(base64ImageString);
            os = new FileOutputStream(file.getAbsolutePath());
            os.write(bs);
            os.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /*
      * 图片缩放,w，h为缩放的目标宽度和高度
      * src为源文件目录，dest为缩放后保存目录
      */
    private static BufferedImage zoomImage(BufferedImage src, int w, int h) throws Exception {
        Image image;//设置缩放目标图片模板
        src.getScaledInstance(w, h, src.SCALE_SMOOTH);

        double wr = w * 1.0 / src.getWidth();     //获取缩放比例
        double wh = h * 1.0 / src.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, wh), null);
        image = ato.filter(src, null);
        return (BufferedImage) image;
    }

    /*
         * 图片缩放,w，h为缩放的目标宽度和高度
         * src为源文件目录，dest为缩放后保存目录
         */
    public static void zoomImage(String src, String dest, int w, int h) throws Exception {

        File srcFile = new File(src);
        File destFile = new File(dest);

        BufferedImage bufImg = ImageIO.read(srcFile); //读取图片
        bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板

        double wr = w * 1.0 / bufImg.getWidth();     //获取缩放比例
        double hr = h * 1.0 / bufImg.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Image itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) itemp, dest.substring(dest.lastIndexOf(".") + 1), destFile); //写入缩减后的图片
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Map hint = new HashMap();
        hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hint.put(EncodeHintType.CHARACTER_SET, Charsets.UTF_8.toString());

        String path = "/home/lijianying/Pictures/images.png";
        String outPath = "/home/lijianying/Pictures/qrcode.png";
        BufferedImage image = createQRCodeWithLogo("http://www.baidu.com",  new File(path));

        try {
            ImageIO.write(image, "png", new File(outPath));
//            System.out.println(getImageBase64String(ImageIO.read(new File(outPath))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
