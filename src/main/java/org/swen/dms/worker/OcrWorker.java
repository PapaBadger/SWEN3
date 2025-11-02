package org.swen.dms.worker;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import net.sourceforge.tess4j.ITesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.repository.DocumentRepository;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.swen.dms.config.RabbitConfig.QUEUE_OCR;

/**
 * Simulated OCR worker service.
 *
 * Acts as a RabbitMQ message listener, consuming messages from the {@code docs.ocr.queue}.
 * Currently logs receipt of {@link DocumentCreatedEvent} objects to represent future OCR processing.
 *
 * In later sprints, this component can be expanded to perform actual text extraction.
 */

@Component
public class OcrWorker {
    private static final Logger log = LoggerFactory.getLogger(OcrWorker.class);
    private final MinioClient minio;
    private final ITesseract tess;
    private final int dpi;
    private final GenAIWorker genAIWorker;

    private final DocumentRepository repo;

    public OcrWorker(MinioClient minio, ITesseract tess,
                     @Value("${ocr.dpi:300}") int dpi, DocumentRepository repo) {
        this.minio = minio; this.tess = tess; this.dpi = dpi;
        this.repo = repo;
        this.genAIWorker = new GenAIWorker();
    }

    @RabbitListener(queues = QUEUE_OCR)
    public void handle(DocumentCreatedEvent e) {
        log.info("OCR start id={} title={} bucket={} key={}",
                e.getId(), e.getTitle(), e.getBucket(), e.getFileKey());

        File tmp = null;
        try (InputStream in = minio.getObject(
                GetObjectArgs.builder().bucket(e.getBucket()).object(e.getFileKey()).build())) {

            tmp = File.createTempFile("dms_", ".pdf");
            try (OutputStream out = new FileOutputStream(tmp)) { in.transferTo(out); }

            String text = ocrPdfWithPdfBox(tmp, dpi, tess);

            var doc = repo.findById(e.getId()).orElseThrow(() -> new RuntimeException("Document not found: " + e.getId()));

            doc.setOcrText(text);

            //creating summary with genai and saving to db
            doc.setOcrSummaryText(genAIWorker.summarize(text));

            repo.save(doc);

            log.info("OCR text saved for id={} ({} chars)", e.getId(), text.length());

            log.info("OCR done id={} ({} chars)", e.getId(), text.length());
//            log.info("DOCUMENT TEXT: {}", text);
            log.info("DOCUMENT TEXT FROM DB: ={}", doc.getOcrText());
            log.info("OCR SUMMARY------------------------- = {}", doc.getOcrSummaryText());

        } catch (Exception ex) {
            log.error("OCR failed id={}: {}", e.getId(), ex.getMessage(), ex);
        } finally {
            if (tmp != null) tmp.delete();
        }
    }

    private static String ocrPdfWithPdfBox(File pdf, int dpi, ITesseract tess) throws Exception {
        try (var doc = Loader.loadPDF(pdf)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            StringBuilder sb = new StringBuilder();
            for (int page = 0; page < doc.getNumberOfPages(); page++) {
                BufferedImage bim = renderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                File tmpImg = File.createTempFile("page_", ".png");
                try {
                    javax.imageio.ImageIO.write(bim, "png", tmpImg);
                    sb.append(tess.doOCR(tmpImg)).append('\n');
                } finally {
                    tmpImg.delete();
                }
            }
            return sb.toString();
        }
    }

}

