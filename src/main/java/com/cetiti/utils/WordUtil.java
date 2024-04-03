package com.cetiti.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class WordUtil {

    /**
     * 设置纸张大小
     *
     * @param document
     */
    public static void setPaper(XWPFDocument document) {
        CTBody body = document.getDocument().getBody();
        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr sectPr = body.getSectPr();

        /// 设置页面为A4纸大小
        CTPageSz pageSize = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));  // A4纸宽度为11906
        pageSize.setH(BigInteger.valueOf(16838));  // A4纸高度为16838

        // 设置页边距
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(1440));  // 页边距左侧
        pageMar.setRight(BigInteger.valueOf(1440));  // 页边距右侧
        pageMar.setTop(BigInteger.valueOf(1440));  // 页边距顶部
        pageMar.setBottom(BigInteger.valueOf(1440));  // 页边距底部
    }

    public static void setTitleAndSummaryTable(XWPFDocument document, List<String> resultList) {
        // 创建标题
        XWPFParagraph title = document.createParagraph();
        //居中
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("测试序列执行报告");
        //字体大小 四号
        titleRun.setFontSize(14);
        //加粗
        titleRun.setBold(true);
        titleRun.setFontFamily("微软雅黑");

        // 创建表格
        XWPFTable table = document.createTable(8, 2);
        // 设置表格宽度为100%
        CTTblWidth tblWidth = table.getCTTbl().addNewTblPr().addNewTblW();
        tblWidth.setType(STTblWidth.PCT);
        tblWidth.setW(BigInteger.valueOf(5000));
        // 设置表格居中
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
        jc.setVal(STJc.CENTER);

        // 设置第一列和第二列的比例为3:5
        for (int i = 0; i < 8; i++) {
            table.getRow(i).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(3000));
            table.getRow(i).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));
            // 设置行高
            table.getRow(i).setHeight(500);
        }

        List<String> headers = Arrays.asList("报告编号", "报告名称", "执行序列", "执行用户", "执行时长", "执行结果", "数据存储", "报告时间");

        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < row.getTableCells().size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                XWPFRun run = paragraph.createRun();
                // 设置字体大小
                run.setFontSize(12);
                run.setFontFamily("微软雅黑");
                // 根据列索引设置对齐方式
                if (j == 0) {
                    run.setBold(true);
                    run.setText(headers.get(i));
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                } else {
                    run.setText(resultList.get(i));
                    paragraph.setAlignment(ParagraphAlignment.LEFT);
                }
            }
        }
    }

    public static void setTitleAndSummaryTable(XWPFDocument document) {
        // 创建标题
        XWPFParagraph title = document.createParagraph();
        //居中
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("测试序列执行报告");
        //字体大小 四号
        titleRun.setFontSize(14);
        //加粗
        titleRun.setBold(true);
        titleRun.setFontFamily("微软雅黑");

        // 创建表格
        XWPFTable table = document.createTable(8, 2);
        // 设置表格宽度为100%
        CTTblWidth tblWidth = table.getCTTbl().addNewTblPr().addNewTblW();
        tblWidth.setType(STTblWidth.PCT);
        tblWidth.setW(BigInteger.valueOf(5000));
        // 设置表格居中
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
        jc.setVal(STJc.CENTER);

        // 设置第一列和第二列的比例为3:5
        for (int i = 0; i < 8; i++) {
            table.getRow(i).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(3000));
            table.getRow(i).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));
            // 设置行高
            table.getRow(i).setHeight(500);
        }

        List<String> headers = Arrays.asList("报告编号", "报告名称", "执行序列", "执行用户", "执行时长", "执行结果", "数据存储", "报告时间");

        //设置每行第一列
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);

            XWPFTableCell cell = row.getCell(0);
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            // 设置字体大小
            run.setFontSize(12);
            run.setFontFamily("微软雅黑");
            run.setBold(true);
            run.setText(headers.get(i));
            paragraph.setAlignment(ParagraphAlignment.CENTER);
        }
    }


    public static void fillFirstTable(XWPFDocument document, List<String> resultList) {
        XWPFTable table = document.getTableArray(0);
        //设置第一个表格第二列数据
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);

            XWPFTableCell cell = row.getCell(1);
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            // 设置字体大小
            run.setFontSize(12);
            run.setFontFamily("微软雅黑");
            run.setText(resultList.get(i));
            paragraph.setAlignment(ParagraphAlignment.LEFT);
        }
    }

    public static void setBreak(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText("\r");
    }

    public static void setParagraphText(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphRun = paragraph.createRun();
        paragraphRun.setText(text);
        paragraphRun.setFontSize(12);
        paragraphRun.setBold(true);
        paragraphRun.setFontFamily("微软雅黑");
    }

    public static XWPFTable setTableHeader(XWPFDocument document) {
        // 创建一个两行八列的表格
        XWPFTable table = document.createTable(2, 8);

        // 设置表格宽度为100%
        CTTblWidth tblWidth2 = table.getCTTbl().addNewTblPr().addNewTblW();
        tblWidth2.setType(STTblWidth.PCT);
        tblWidth2.setW(BigInteger.valueOf(5000));

        // 设置列宽比例
        int[] colWidths = {3000, 720, 2240, 630, 835, 835, 835, 835};
        for (int i = 0; i < 8; i++) {
            table.getRow(0).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(colWidths[i]));
            table.getRow(1).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(colWidths[i]));
        }
        //设置行高
        int[] rowHeight = {500, 1000};
        for (int i = 0; i < 2; i++) {
            table.getRow(i).setHeight(rowHeight[i]);
        }
        //合并单元格
        TableUtil.mergeCellsHorizontal(table, 0, 4, 7);
        for (int i = 0; i < 4; i++) {
            TableUtil.mergeCellsVertically(table, i, 0, 1);
        }

        List<String> list = Arrays.asList("步骤", "执行状态", "执行结果", "单位", "通过条件", "常规值", "下限值", "上限值", "比较方法");
        for (int i = 0; i < 2; i++) {
            XWPFTableRow row = table.getRow(i);
            for (int j = 0; j < 8; j++) {
                XWPFTableCell cell = row.getCell(j);
                //垂直居中
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                XWPFParagraph xwpfParagraph = cell.getParagraphs().get(0);
                xwpfParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun run = xwpfParagraph.createRun();
                run.setBold(true);
                run.setFontSize(12);
                run.setFontFamily("微软雅黑");
                if (i == 0 && j < 5) {
                    run.setText(list.get(j));
                } else if (i == 1 && j >= 4) {
                    run.setText(list.get(j + 1));
                }
            }
        }
        return table;
    }
}
