package fr.uem.efluid.system.common;

import fr.uem.efluid.services.types.ExportFile;

import java.util.Arrays;

public class ImportExportHelper {


    public static ExportFile generateFile(int size, String name) {
        byte[] data = new byte[size];
        Arrays.fill(data, (byte) 'X');
        return new TestExportFile(data, name, "");
    }

    private static final class TestExportFile extends ExportFile {

        TestExportFile(byte[] fileData, String filename, String contentType) {
            super(fileData, filename, contentType);
        }
    }
}
