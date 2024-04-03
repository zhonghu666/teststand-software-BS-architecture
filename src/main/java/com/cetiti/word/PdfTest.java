package com.cetiti.word;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfTest {

    public static void main(String[] args) throws Exception {
        //word 转 pdf
//        String outputWordPath = "word1.docx";
//        XWPFDocument document = new XWPFDocument();
//        XWPFParagraph paragraph = document.createParagraph();
//        XWPFRun run = paragraph.createRun();
//        run.setText("hahahaha123456789");
//        try (FileOutputStream fileOutputStream = new FileOutputStream(outputWordPath)) {
//            // 将Apache POI的XWPFDocument对象写入文件
//            document.write(fileOutputStream);
//
//            System.out.println("POI的Word文档成功保存到本地！");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        byte[] pdf = pdf();
        String outputFilePath = "pdf3.pdf"; // 输出的文件路径

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            // 将字节数组写入文件
            fileOutputStream.write(pdf);

            System.out.println("字节数组成功保存到本地文件！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte[] pdf() {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("hahahaha123456789");
            document.write(outputStream);

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                 ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
                com.aspose.words.Document doc = new Document(inputStream);
                doc.save(pdfOutputStream, SaveFormat.PDF);
                return pdfOutputStream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
