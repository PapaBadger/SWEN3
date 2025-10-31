package org.swen.dms.helper;

import java.time.LocalDate;
import java.util.UUID;

//unique name for every pdf
public class GenerateFileKey {
    public String generateFileKey() {
        String uuid = UUID.randomUUID().toString();
        return String.format("%s.pdf", uuid);
    }

}
