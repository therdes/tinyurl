package com.therdes.app.qrcode.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;

@Controller
@RequestMapping("/qrcode")
@Slf4j
public class QRCodeUrlController {

    private static final int DEFAULT_WIDTH = 500;

    private static final int DEFAULT_HEIGHT = 500;

    private static final String PARSE_UPLOAD_MAPPING = "parseUpload";

    @RequestMapping
    @ResponseBody
    public String index() {
        return "Welcome to use QRCodeUrl Service!";
    }

    @RequestMapping(value = "/generate", method = {RequestMethod.GET, RequestMethod.POST})
    public void generateQRCode(HttpServletResponse response,
                               @RequestParam("url") final String url,
                               @RequestParam(value = "width", required = false) final Integer width,
                               @RequestParam(value = "height", required = false) final Integer height) throws IOException {
        var originUrl = URLDecoder.decode(url, "UTF-8");

        int finalWidth = width == null ? DEFAULT_WIDTH : width;
        int finalHeight = height == null ? DEFAULT_HEIGHT : height;

        var multiFormatWriter = new MultiFormatWriter();
        var hints = new HashMap<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(originUrl, BarcodeFormat.QR_CODE, finalWidth, finalHeight, hints);
        } catch (WriterException e) {
            log.error("无法将地址转换为二维码：" + e.getMessage(), e);
        }

        if (bitMatrix == null) {
            response.setCharacterEncoding("UTF-8");
            var printWriter = response.getWriter();
            printWriter.println("转换地址为二维码失败");
            printWriter.close();
        } else {
            var bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ImageIO.write(bufferedImage, "JPEG", response.getOutputStream());
            response.getOutputStream().close();
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadQRCode() {
        return "qrcode/upload";
    }

    @RequestMapping(value = "/" + PARSE_UPLOAD_MAPPING, method = RequestMethod.POST)
    @ResponseBody
    public String parseQRCode(@RequestParam("qrCodeImg") MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return "文件上传失败，请重新选择。";
        }

        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(multipartFile.getInputStream());
        } catch (IOException e) {
            log.error("无法读取上传二维码图片", e);
            return "无法读取上传文件，请重新上传。";
        }

        var luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
        var binarizer = new HybridBinarizer(luminanceSource);
        var binaryBitmap = new BinaryBitmap(binarizer);
        var hints = new HashMap<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        try {
            return new MultiFormatReader().decode(binaryBitmap, hints).getText();
        } catch (NotFoundException e) {
            return "无法从图片中找到二维码，请确认是否上传正确的图片。";
        }
    }
}
