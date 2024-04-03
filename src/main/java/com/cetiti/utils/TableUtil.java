package com.cetiti.utils;


import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.io.ByteArrayInputStream;
import java.util.List;

public class TableUtil {

    /**
     * word单元格行合并
     *
     * @param table    表格
     * @param col      合并行所在列
     * @param startRow 开始行
     * @param endRow   结束行
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            XWPFTableCell cell = table.getRow(i).getCell(col);
            if (i == startRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * word单元格列合并
     *
     * @param table     表格
     * @param row       合并列所在行
     * @param startCell 开始列
     * @param endCell   结束列
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int startCell, int endCell) {
        for (int i = startCell; i <= endCell; i++) {
            XWPFTableCell cell = table.getRow(row).getCell(i);
            if (i == startCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }


    public static void fillFirstColumn(XWPFTable xwpfTable, List<String> datas, int fontSize, String fontFamily, int left) {
        XWPFTableCell cell = xwpfTable.getRow(xwpfTable.getNumberOfRows() - 1).getCell(0);
        XWPFParagraph paragraphArray = cell.getParagraphArray(0);
        XWPFRun run = paragraphArray.createRun();
        for (int i = 0; i < datas.size(); i++) {
            run.setText(datas.get(i));
            if (i != datas.size() - 1) {
                run.addBreak();
            }
        }
        run.setFontSize(fontSize);
        run.setFontFamily(fontFamily);
        paragraphArray.setIndentationLeft(left);
        paragraphArray.setAlignment(ParagraphAlignment.LEFT);
    }

    /**
     * @param xwpfTable
     * @param datas
     */
    public static void fillTableData(XWPFTable xwpfTable, String... datas) {
        XWPFTableRow row = xwpfTable.getRow(xwpfTable.getNumberOfRows() - 1);
        for (int i = 0; i < datas.length; i++) {
            XWPFTableCell cell = row.getCell(i + 1);
            XWPFParagraph paragraphArray = cell.getParagraphArray(0);
            XWPFRun run = paragraphArray.createRun();
            run.setText(datas[i]);

            run.setFontSize(12);
            run.setFontFamily("微软雅黑");
//            paragraphArray.setIndentationLeft(left);
            paragraphArray.setAlignment(ParagraphAlignment.CENTER);
        }

    }

    public static void fillFirstColumn(XWPFTable xwpfTable, List<String> datas, int left) {
        fillFirstColumn(xwpfTable, datas, 12, "微软雅黑", left);
    }

    public static void fillFirstColumn(XWPFTable xwpfTable, List<String> datas) {
        fillFirstColumn(xwpfTable, datas, 12, "微软雅黑", 0);
    }

    public static void createRow(XWPFTable xwpfTable, int height) {
        xwpfTable.createRow();
        XWPFTableRow row = xwpfTable.getRow(xwpfTable.getNumberOfRows() - 1);
        row.setHeight(height);
        List<XWPFTableCell> tableCells = row.getTableCells();
        for (XWPFTableCell tableCell : tableCells) {
            tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        }
    }

    public static void createRowAndMergeCell(XWPFTable xwpfTable, int height, int from, int to) {
        createRow(xwpfTable, height);
        mergeCellsHorizontal(xwpfTable, xwpfTable.getNumberOfRows() - 1, from, to);
    }

    public static void createRowAndFill(XWPFTable xwpfTable, int height, List<String> datas) {
        createRow(xwpfTable, height);
        fillFirstColumn(xwpfTable, datas);
    }

    public static void createRowAndMergeFill(XWPFTable xwpfTable, int height, int from, int to, List<String> datas) {
        createRowAndMergeCell(xwpfTable, height, from, to);
        fillFirstColumn(xwpfTable, datas);
    }

    public static void createRowAndFill(XWPFTable xwpfTable, int height, List<String> datas, int left) {
        createRow(xwpfTable, height);
        fillFirstColumn(xwpfTable, datas, left);
    }

    public static void createRowAndMergeFill(XWPFTable xwpfTable, int height, int from, int to, List<String> datas, int left) {
        createRowAndMergeCell(xwpfTable, height, from, to);
        fillFirstColumn(xwpfTable, datas, left);
    }

    public static void addPicture(XWPFTable xwpfTable, byte[] bytes, String pictureName) {
        XWPFTableCell cell = xwpfTable.getRow(xwpfTable.getNumberOfRows() - 1).getCell(0);
        XWPFParagraph xwpfParagraph = cell.getParagraphArray(0);
        xwpfParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = xwpfParagraph.createRun();
        try {
            run.addPicture(new ByteArrayInputStream(bytes), Document.PICTURE_TYPE_PNG, pictureName + ".png", Units.toEMU(350), Units.toEMU(200));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
