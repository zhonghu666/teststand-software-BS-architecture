package com.cetiti.word;


import com.cetiti.utils.CreateWord;
import com.cetiti.utils.DateUtils;
import com.cetiti.utils.TableUtil;
import com.cetiti.utils.WordUtil;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class MakeWord {

    public static void main(String[] args) {
        List<String> resultList = Arrays.asList(UUID.randomUUID().toString(), "车辆序列测试报告_20231129_105318",
                "车辆序列", "张三", "4min28s", "合格", "ac\\bc\\cc", DateUtils.localDate2string(LocalDateTime.now()));

        try {
            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();
            //设置A4纸
            WordUtil.setPaper(document);
            //设置标题和第一个表格
            WordUtil.setTitleAndSummaryTable(document, resultList);
            //换行
            WordUtil.setBreak(document);
            //表格上方文字
            WordUtil.setParagraphText(document, "【主序列】步骤详情");
            //设置表格表头
            XWPFTable table = WordUtil.setTableHeader(document);

            CreateWord.ifTable(table, "test", "PASS", false, false);
//            TableUtil.createRowAndFill(table, 1500, Arrays.asList("合格/失败测试", "2023-9-20 16:24:30.123"));
            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Report Text"), 200);

            // 创建File对象
            File imageFile = new File("1.png");
            // 创建字节数组
            byte[] imageBytes = new byte[(int) imageFile.length()];
            // 创建FileInputStream对象
            FileInputStream fis = new FileInputStream(imageFile);
            // 读取图片数据到字节数组
            fis.read(imageBytes);
            // 关闭FileInputStream
            fis.close();
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Graph(MultipleY, Row Based)"));
            TableUtil.createRowAndMergeCell(table, 3500, 0, 7);
            TableUtil.addPicture(table, imageBytes, "picture");
//
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("数值限度测试", "2023-9-20 16:24:30.126"));
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("数值限度测试", "2023-9-20 16:24:30.126"));
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("执行结果:"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("NumericArray[0]"), 400);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("NumericArray[1]"), 400);
            CreateWord.ifTable(table, "test", "NUMBER", false, false);
            CreateWord.ifTable(table, "test", "NUMBER_MANY", false, false);

//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("字符串测试", "2023-9-20 16:24:30.126"));
            CreateWord.ifTable(table, "test", "STRING", false, false);
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("Action类步骤", "2023-9-20 16:24:30.126"));
//            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("ActionSettings"), 200);
            CreateWord.ifTable(table, "action", null, false, false);

            TableUtil.createRowAndMergeFill(table, 1000, 1, 7, Arrays.asList("Error Message"), 200);
            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Locals.ttc=CalculateRelativeDistance(Locals.data.[RunState.LoopIndex].HV_latitude,\n" +
                    "Locals.data.[RunState.LoopIndex].HV_longitude,Locals.data.[RunState.LoopIndex].RV_latitude.[0],\n" +
                    "Locals.data.[RunState.LoopIndex].RV_longitude.[0])/\n" +
                    "math.abs(math.sqrt(math.pow(Locals.data.[RunState.LoopIndex].HV_northSpeed,2)+\n" +
                    "math.pow(Locals.data.[RunState.LoopIndex].HV_eastSpeed,2))-Locals.data.[RunState.LoopIndex].RV_speed.[0])"));

            CreateWord.ifTable(table, "popup", null, false, false);
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("Message Popup步骤", "2023-9-20 16:24:30.126"));
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("执行结果:"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("ButtonHit"), 400);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("Response"), 400);

            CreateWord.ifTable(table, "call", null, false, false);
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("序列调用步骤", "2023-9-20 16:24:30.126"));

            CreateWord.ifTable(table, "statement", null, true, true);
//            TableUtil.createRowAndMergeFill(table, 1500, 1, 7, Arrays.asList("声明步骤", "表达式内容", "2023-9-20 16:24:30.123"));

            //todo 循环
//            TableUtil.createRowAndFill(table, 1000, Arrays.asList("××步骤循环", "2023-9-20 16:24:30.126"));
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("循环次数"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("成功次数"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("失败次数"), 200);
//
//
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("每个迭代结果:"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[1]"), 200);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("××步骤[2]"), 200);

            CreateWord.ifTable(table, "for", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("For", "{0} 设置的循环语句", "2023-9-20 16:24:30.123"));
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("序列调用步骤"));
            CreateWord.ifTable(table, "call", null, false, false);

            CreateWord.ifTable(table, "end for", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (For)"));

            CreateWord.ifTable(table, "for", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("For", "{1} 设置的循环语句", "2023-9-20 16:24:30.123"));

            CreateWord.ifTable(table, "call", null, false, false);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("序列调用步骤"));

            CreateWord.ifTable(table, "end for", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (For)"));

            CreateWord.ifTable(table, "if", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("If", "设置的条件语句", "2023-9-20 16:24:30.123"));
            CreateWord.ifTable(table, "else if", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1000, 0, 7, Arrays.asList("Else If", "设置的条件语句"));
            CreateWord.ifTable(table, "else", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Else"));

            CreateWord.ifTable(table, "test", "PASS", false, false);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("合格/失败测试"), 200);

            CreateWord.ifTable(table, "end if", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (If)"));


            CreateWord.ifTable(table, "select", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Select", "设置的待比较值", "2023-9-20 16:24:30.123"));
            CreateWord.ifTable(table, "case", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("Case 0"));
            CreateWord.ifTable(table, "test", "PASS", false, false);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("合格/失败测试"), 200);

            CreateWord.ifTable(table, "end case", null, false, false);

//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Case)"));
            CreateWord.ifTable(table, "end select", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Select)"));

            CreateWord.ifTable(table, "while", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("While", "设置的条件语句", "2023-9-20 16:24:30.123"));
            CreateWord.ifTable(table, "test", "PASS", false, false);
//          TableUtil.createRowAndFill(table, 500, Arrays.asList("合格/失败测试"), 200);
            CreateWord.ifTable(table, "end while", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (While)"));

            CreateWord.ifTable(table, "do while", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 1500, 0, 7, Arrays.asList("Do While", "设置的条件语句", "2023-9-20 16:24:30.123"));
            CreateWord.ifTable(table, "test", "PASS", false, false);
//            TableUtil.createRowAndFill(table, 500, Arrays.asList("合格/失败测试"), 200);
            CreateWord.ifTable(table, "end do while", null, false, false);
//            TableUtil.createRowAndMergeFill(table, 500, 0, 7, Arrays.asList("End (Do While)"));

            //表格下方文字
            WordUtil.setParagraphText(document, "【主序列】步骤详情 End");
//            XWPFParagraph paragraph3 = document.createParagraph();
//            paragraph3.setAlignment(ParagraphAlignment.LEFT);
//            XWPFRun paragraphRun3 = paragraph3.createRun();
//            paragraphRun3.setText("【主序列】步骤详情 End");
//            paragraphRun3.setFontSize(12);
//            paragraphRun3.setBold(true);
//            paragraphRun3.setFontFamily("微软雅黑");

            // 保存文档到本地
            OutputStream out = Files.newOutputStream(Paths.get("output.docx"));
            document.write(out);
            out.close();

            System.out.println("Word文档导出成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
