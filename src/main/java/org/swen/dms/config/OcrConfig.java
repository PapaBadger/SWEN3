package org.swen.dms.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcrConfig {
    @Bean
    public ITesseract tesseract(
            @Value("${ocr.lang:deu+eng}") String lang,
            @Value("${ocr.psm:3}") int psm,
            @Value("${ocr.oem:1}") int oem
    ) {
        Tesseract t = new Tesseract();
        t.setLanguage(lang);
        t.setPageSegMode(psm);
        t.setOcrEngineMode(oem);
        return t;
    }
}
