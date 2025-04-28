package org.eec.sonnets;

import java.io.*;

public class SonnetReader extends BufferedReader {

    public SonnetReader(Reader reader){
        super(reader);
    }

    public SonnetReader(InputStream inputStream){
        this(new InputStreamReader(inputStream));
    }

    public void skipLines(int lines) throws IOException {
        for(int i = 0; i < lines; i++){
            readLine();
        }
    }

    private String skipSonnetHeader() throws IOException{
        String line = readLine();
        while(line.isBlank()){
            line = readLine();
        }

        if(line.startsWith("*** END OF THE PROJECT GUTENBERG EBOOK")){
            // Pseudo-EOF reached.
            return null;
        }

        System.out.println("Sonnet Header: [" + line +"] FOUND!");

        line = readLine();

        while(line.isBlank()){
            line = readLine();
        }

        // return the 1st line of the current Sonnet
        System.out.println("Returning the 1st line of the Sonnet: [" + line + "]");
        return line;
    }

    public Sonnet readNextSonnet() throws IOException{
        String line = skipSonnetHeader();
        if(line == null){
            return null;
        }else{
            Sonnet sonnet = new Sonnet();
            while(!line.isBlank()){
                sonnet.addLine(line);
                line = readLine();
            }
            return sonnet;
        }
    }

}
