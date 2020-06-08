package excelengine;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileReader {
    InputStream inputStream;
    XSSFWorkbook workBook;
    XSSFSheet sheet;
    public FileReader(String filename) {
        try {

            inputStream = new FileInputStream(filename);
            workBook = new XSSFWorkbook(inputStream);
//            workBook = new XSSFWorkbook(filename);
            //workBook= (XSSFWorkbook) WorkbookFactory.create(new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println(heapFreeSize);

    }

    public void openFile() {
        sheet = workBook.getSheetAt(0);
    }

    public XSSFRow getRow(int i) {
        return sheet.getRow(i);
    }

    public int getRowCount(){ return sheet.getLastRowNum();}
}
