package org.eec.sonnets;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Main {

    private static long skip(BufferedInputStream bis, int offset) throws IOException{
        long skip = 0L;

        while(skip < offset){
            skip += bis.skip(offset - skip);
        }

        return skip;
    }

    private static byte[] readBytes(BufferedInputStream bis, int length) throws IOException{
        byte[] bytes = new byte[length];
        byte[] buffer = new byte[length];

        int read = bis.read(buffer);
        int copied = 0;

        while(copied < length){
            System.arraycopy(buffer, 0, bytes, copied, read);
            copied += read;
            read = bis.read(buffer);
        }

        return bytes;
    }

    public static void main(String[] args) {
        System.out.println("##--Testing: Reading the Sonnets Text File...");
        int start = 27;

        List<Sonnet> sonnets = new ArrayList<>();
        URI sonnetsURI = URI.create("https://www.gutenberg.org/cache/epub/1041/pg1041.txt");
        HttpRequest request = HttpRequest.newBuilder(sonnetsURI).GET().build();
        HttpClient client = HttpClient.newBuilder().build();

        try{
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try(InputStream inputStream = response.body();
                    SonnetReader sonnetReader = new SonnetReader(inputStream)){
                sonnetReader.skipLines(start);
                Sonnet sonnet = sonnetReader.readNextSonnet();
                while (sonnet != null){
                    sonnets.add(sonnet);
                    sonnet = sonnetReader.readNextSonnet();
                }
            }
        }catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("InterruptedException: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Number of Sonnets READ: [" + sonnets.size() + "]");

        if(!sonnets.isEmpty()){
            System.out.println("2st Sonnet Sample:: \n");
            System.out.println(sonnets.get(1));
        }

        // Writing ALL-Sonnets compressed bytes to a 'Final' binary file.
        System.out.println("\n##--Test: Writing ALL-Sonnets compressed bytes to a 'Final' binary file...");

        int numberOfSonnets = sonnets.size();
        Path sonnetsFile = Path.of("files/sonnets.bin");

        if(!Files.exists(sonnetsFile.getParent())){
            try {
                Files.createDirectories(sonnetsFile.getParent());
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
        }

        try(OutputStream outputStream = Files.newOutputStream(sonnetsFile);
                DataOutputStream dos = new DataOutputStream(outputStream)){

            List<Integer> offsets = new ArrayList<>();
            List<Integer> lengths = new ArrayList<>();

            byte[] encodeSonnetsBytesArray = null;

            try(ByteArrayOutputStream encodedSonnets = new ByteArrayOutputStream()){
                for(Sonnet sonnet : sonnets){
                    byte[] sonnetCompressedBytes = sonnet.getCompressedBytes();

                    offsets.add(encodedSonnets.size());
                    lengths.add(sonnetCompressedBytes.length);
                    encodedSonnets.write(sonnetCompressedBytes);
                }

                dos.writeInt(numberOfSonnets);
                for(int index = 0; index < numberOfSonnets; index++){
                    dos.writeInt(offsets.get(index));
                    dos.writeInt(lengths.get(index));
                }

                encodeSonnetsBytesArray = encodedSonnets.toByteArray();
            }

            outputStream.write(encodeSonnetsBytesArray);

            System.out.println("Sonnets compressed bytes SUCCESSFULLY saved to file: [" + sonnetsFile + "]");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }

        // READING a specific Sonnet from .bin file...
        /*
        Note that the offsets are computed from the start of the array containing
        all the compressed sonnets, not the start of the file.
        If you prefer to have them from the start of the file,
        you just to add 4 + 2*4*numberOfSonnets to each offset,
        which represents the size of the header of the file.

        [numberOfSonnets]{[offset][length]} :: int primitive-type (4 bytes)
        */
        System.out.println("##--READING a specific Sonnet from .BIN file...");

        try(InputStream file = Files.newInputStream(sonnetsFile);
                BufferedInputStream bis = new BufferedInputStream(file);
                DataInputStream dis = new DataInputStream(file)){

            int numOfSonnets = dis.readInt();
            System.out.println("numOfSonnets [READ-from-file]: [" + numOfSonnets + "]");
            List<Integer> offsets = new ArrayList<>();
            List<Integer> lengths = new ArrayList<>();

            // Read file-HEADER ({[offset][length]}*)
            for(int i = 0; i < numOfSonnets; i++){
                offsets.add(dis.readInt());
                lengths.add(dis.readInt());
            }

            System.out.println("--->> Testing reading a specific Sonnet (from File: " + sonnetsFile + ")...");
            int sonnet = 75; // the sonnet you are reading
            int offset = offsets.get(sonnet - 1);
            int length = lengths.get(sonnet - 1);

            skip(bis, offset);
            byte[] bytes = readBytes(bis, length);

            try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzbais = new GZIPInputStream(bais);
                InputStreamReader isr = new InputStreamReader(gzbais);
                    BufferedReader reader = new BufferedReader(isr)){
                List<String> sonnetLines = reader.lines().toList();

                System.out.println(">> Sonnet [" + sonnet + "] CONTENTS:: ");
                sonnetLines.forEach(System.out::println);
                System.out.println(">> (End-of-Sonnet) [" + sonnet + "]");
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }

    } //--fin: main()
}
