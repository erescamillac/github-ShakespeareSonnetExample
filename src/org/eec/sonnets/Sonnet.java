package org.eec.sonnets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Sonnet {
    private List<String> lines = new ArrayList<>();

    public void addLine(String line){
        lines.add(line);
    }

    public byte[] getCompressedBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try(GZIPOutputStream gzos = new GZIPOutputStream(bos);
                PrintWriter printWriter = new PrintWriter(gzos)){
            for(String line : lines){
                printWriter.println(line);
            }
        }

        return bos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sonnet { \n");
        for(String ln: lines){
            sb.append(ln).append("\n");
        }
        sb.append(" }");
        return sb.toString();
    }
}
