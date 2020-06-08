package fileengine;

import defines.defaultValues;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public class FileReader implements Iterator {
    String fileName;
    String[] header;
    String[] record;
    String[] lines;
    BufferedReader fileReader=null;
    private boolean endOfFile;

    public String[] getHeader() {
        return header;
    }

    public String[] getRecord() {
        return record;
    }

    public String[] getLines() {
        return lines;
    }

    public FileReader(String s) {
        fileName=s;
    }

//    public void readInfoFromFile(){
//        ArrayList<String> lines = getInfoLinesFromFile();
//        if (lines.size()>1){
//            header=lines.get(0).split(defaultValues.getDELIMITER());
//            record=lines.get(1).split(defaultValues.getDELIMITER());
//        }
//        return ;
//    }

    private ArrayList<String> getInfoLinesFromFile() {
        ArrayList<String> lines=new ArrayList<>();
        String line;
        BufferedReader reader=null;

        try {
            reader = new BufferedReader(
            new InputStreamReader(
                    new FileInputStream(fileName), Charset.forName("cp866")));

            int i=0;

            while (((line= reader.readLine()) != null) && (i<2)) {
                lines.add(line);
                i++;
            }
        } catch (IOException e) {}
        finally {
              if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { }
            }
        }
        return lines;
    }

    public void initReader(){
        try {
            fileReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileName), Charset.forName("cp866")));
        } catch (IOException e) {}
        endOfFile =false;
    }


    public String getLineFromFile() throws IOException {
        String line;
        if (fileReader==null) initReader();

            if ((line= fileReader.readLine()) != null) return line;
            else endOfFile =true;
        return "";
    }

    public void closeReader(){
            if (fileReader!= null) {
                try {
                    fileReader.close();
                } catch (IOException e) { }
            }
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }

    public String getHeaderFromFile() throws IOException {
        return getLineFromFile();
    }

    public int getFieldsCountFromArray(String s){
        return s.split(defaultValues.getDELIMITER()).length;
    }

//    public String getDataLineFromFile(int fieldsCount) throws LineReadError, IOException {
//        String line =getLineFromFile();
//        if ((line!="")&&!eof()) {
//            StringBuilder s=new StringBuilder();
//            s.append(line);
//            while (!eof() && (getFieldsCountFromArray(s.toString()) < fieldsCount)) {
//                s.append(getLineFromFile());
//            }
//
//            if (getFieldsCountFromArray(s.toString()) > fieldsCount)
//                throw new LineReadError("Ошибка чтения строки: " + s.toString());
//            return s.toString();
//        }
//        else
//            return line;
//    }

    public boolean eof() {
        return this.endOfFile;
    }

}
