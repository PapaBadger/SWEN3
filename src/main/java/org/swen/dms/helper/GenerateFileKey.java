package org.swen.dms.helper;

import java.time.LocalDate;
import java.util.UUID;

public class GenerateFileKey {
    public String generateFileKey() {
        LocalDate date = LocalDate.now();
        String uuid = UUID.randomUUID().toString();
        return String.format("docs/%d/%02d/%02d/%s.pdf",
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                uuid);
    }

}
