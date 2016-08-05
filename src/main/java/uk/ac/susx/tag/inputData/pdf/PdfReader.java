package uk.ac.susx.tag.inputData.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.inputData.TextInputData;

import java.io.File;
import java.io.IOException;

/*
Reads in a single PDF file and constructs an InputData object out of it
 */
public class PdfReader {

    public static InputData getPdfData(File file) {
        PDDocument document = null;
        String documentText = "";
        try {
            document = PDDocument.load(file);
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper textStripper = new PDFTextStripper();
                documentText = textStripper.getText(document);
            }
            document.close();
        } catch (IOException e) {
            return null;
        }
        return new TextInputData(file.getName(), documentText, file.getPath());
    }
}
