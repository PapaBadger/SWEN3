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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.messaging.OcrCompletedEvent;
import org.swen.dms.repository.jpa.DocumentRepository;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.repository.search.DocumentSearchRepository;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.swen.dms.config.RabbitConfig.*;

/**
 * Simulated OCR worker service.
 *
 * Acts as a RabbitMQ message listener, consuming messages from the {@code docs.ocr.queue}.
 * Currently logs receipt of {@link DocumentCreatedEvent} objects to represent future OCR processing.
 *
 * In later sprints, this component can be expanded to perform actual text extraction.
 */

@Component
@Profile("ocrWorker")
public class OcrWorker {
    private static final Logger log = LoggerFactory.getLogger(OcrWorker.class);
    private final MinioClient minio;
    private final ITesseract tess;
    private final int dpi;

    private final DocumentRepository repo;
    private final RabbitTemplate rabbitTemplate;
    private final DocumentSearchRepository searchRepository;

    public OcrWorker(MinioClient minio, ITesseract tess,
                     @Value("${ocr.dpi:300}") int dpi, DocumentRepository repo, RabbitTemplate rabbitTemplate, DocumentSearchRepository searchRepository) {
        this.minio = minio; this.tess = tess; this.dpi = dpi;
        this.repo = repo;
        this.rabbitTemplate = rabbitTemplate;
        this.searchRepository = searchRepository;
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

            // Save to Postgres
            var doc = repo.findById(e.getId()).orElseThrow(() -> new RuntimeException("Document not found: " + e.getId()));
            doc.setOcrText(text);
            repo.save(doc);

            try {
                DocumentSearch esDoc = new DocumentSearch();
                esDoc.setId(String.valueOf(e.getId()));
                esDoc.setTitle(e.getTitle());
                esDoc.setContent(text);

                searchRepository.save(esDoc);
                log.info("Indexed document {} in Elasticsearch", e.getId());
            } catch (Exception esEx) {
                log.error("Failed to index document in Elasticsearch: {}", esEx.getMessage());
            }

            OcrCompletedEvent event = new OcrCompletedEvent(e.getId());
            rabbitTemplate.convertAndSend(EXCHANGE_DOCS, ROUTING_OCR_COMPLETED, event);

            log.info("OCR done id={} ({} chars)", e.getId(), text.length());

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

