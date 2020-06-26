package excelengine;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class ExcelReader {
    //private final int MINCOUNT=20;

    private final String fileName;
    private final IParserCallBack callBack;
    private  OPCPackage xlsxPackage;
    private final int minColumns;

    public ExcelReader(String filename, IParserCallBack callback, int minColumns) {
        fileName=filename;
        callBack=callback;
        this.minColumns = minColumns;
    }



    private void processSheet(
            Styles styles,
            SharedStrings strings,
            XSSFSheetXMLHandler.SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws IOException, SAXException {
            DataFormatter formatter = new DataFormatter();
            InputSource sheetSource = new InputSource(sheetInputStream);
            try {
                XMLReader sheetParser = XMLHelper.newXMLReader();
                ContentHandler handler = new XSSFSheetXMLHandler(
                        styles, null, strings, sheetHandler, formatter, false);
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);
            } catch(ParserConfigurationException e) {
                throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
            }
        }

    private void process() throws IOException, OpenXML4JException, SAXException {
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        while (iter.hasNext()) {
            try (InputStream stream = iter.next()) {
                //String sheetName = iter.getSheetName();
                processSheet(styles, strings, new SheetToFields(), stream);
            }
        }
    }

    public void read() throws IOException, SAXException, OpenXML4JException {
        File xlsxFile = new File(fileName);
        if (!xlsxFile.exists()) {
            throw new IOException("File not found");
        }
        this.xlsxPackage= OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);
        process();
    }

    public void close() {
        try {
            this.xlsxPackage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class SheetToFields implements XSSFSheetXMLHandler.SheetContentsHandler {
    private int currentRow = -1;
    private int currentCol = -1;
    private final LinkedList<String> list;

    SheetToFields() {
        this.list = new LinkedList<>();
    }

    @Override
    public void startRow(int rowNum) {
        list.clear();
        currentRow = rowNum;
        currentCol = -1;
    }

    @Override
    public void endRow(int rowNum) {
        for (int i=currentCol; i<minColumns; i++) {
            list.add("");
        }
        if (callBack!=null) callBack.call(list);
    }

    @Override
    public void cell(String cellReference, String formattedValue,
                     XSSFComment comment) {
        if(cellReference == null) {
            cellReference = new CellAddress(currentRow, currentCol).formatAsString();
        }
        int thisCol = (new CellReference(cellReference)).getCol();
        int missedCols = thisCol - currentCol - 1;
        for (int i=0; i<missedCols; i++) {
            list.add("");
        }
        currentCol = thisCol;

        try {
            Double.parseDouble(formattedValue);
            list.add(formattedValue);
        } catch (NumberFormatException e) {
            list.add(formattedValue);
        }
    }
}
}
