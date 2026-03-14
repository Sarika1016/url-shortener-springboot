package com.urlshortener.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.urlshortener.exception.QRCodeGenerationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeGenerator {

    public byte[] generateQRCode(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                text, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageConfig config = new MatrixToImageConfig(
                MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
            MatrixToImageWriter.writeToStream(
                bitMatrix, "PNG", outputStream, config);

            return outputStream.toByteArray();

        } catch (WriterException e) {
            throw new QRCodeGenerationException(
                "Failed to encode QR Code: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new QRCodeGenerationException(
                "Failed to render QR Code: " + e.getMessage(), e);
        }
    }
}
