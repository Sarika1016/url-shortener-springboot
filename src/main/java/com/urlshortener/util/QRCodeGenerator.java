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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Utility component for generating QR Code images using the ZXing library.
 *
 * <p>ZXing (Zebra Crossing) encodes text into a 2D BitMatrix — a grid of
 * black (1) and white (0) modules — then renders it as a PNG image.
 *
 * <p>This class is a Spring @Component so it can be injected via @Autowired.
 */
@Component
@Slf4j
public class QRCodeGenerator {

    /**
     * Generates a QR Code as a PNG byte array.
     *
     * <p>Steps:
     * <ol>
     *   <li>Configure ZXing hints (error correction, margin, charset)</li>
     *   <li>Encode the text into a BitMatrix via QRCodeWriter</li>
     *   <li>Render the BitMatrix as a PNG via MatrixToImageWriter</li>
     *   <li>Return the raw PNG bytes</li>
     * </ol>
     *
     * @param text   Text or URL to encode into the QR code
     * @param width  Image width in pixels
     * @param height Image height in pixels
     * @return PNG image as a byte array
     * @throws QRCodeGenerationException if encoding or rendering fails
     */
    public byte[] generateQRCode(String text, int width, int height) {
        log.debug("Generating QR Code for: {} ({}x{}px)", text, width, height);

        try {
            // ── Step 1: Configure ZXing hints ──────────────────────────────
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,  // 30% recovery
                EncodeHintType.MARGIN,           2,                        // quiet zone
                EncodeHintType.CHARACTER_SET,    "UTF-8"                   // full Unicode
            );

            // ── Step 2: Encode text → BitMatrix ────────────────────────────
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            // ── Step 3: Render BitMatrix → PNG bytes ───────────────────────
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // MatrixToImageConfig: black modules = 0xFF000000, white = 0xFFFFFFFF
            MatrixToImageConfig config = new MatrixToImageConfig(
                MatrixToImageConfig.BLACK,
                MatrixToImageConfig.WHITE
            );
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);

            byte[] pngBytes = outputStream.toByteArray();
            log.debug("QR Code generated successfully: {} bytes", pngBytes.length);

            return pngBytes;

        } catch (WriterException e) {
            throw new QRCodeGenerationException(
                "Failed to encode QR Code content: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new QRCodeGenerationException(
                "Failed to render QR Code image: " + e.getMessage(), e);
        }
    }
}
