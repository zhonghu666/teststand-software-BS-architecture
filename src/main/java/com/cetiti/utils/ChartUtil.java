package com.cetiti.utils;

import com.alibaba.fastjson.JSON;
import com.cetiti.entity.StepAdditional;
import com.cetiti.entity.StepVariable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ChartUtil {


    @SuppressWarnings("unchecked")
    public static List<byte[]> chart(List<StepAdditional> list, StepVariable stepVariable) {
        List<byte[]> chartList = new ArrayList<>();
        list = list.stream().filter(stepAdditional -> Objects.equals(Boolean.TRUE, stepAdditional.getIsGraphs())).collect(Collectors.toList());
        for (StepAdditional stepAdditional : list) {
            String sourceExpression = stepAdditional.getSourceExpression();
            String[] split = sourceExpression.split(",");
            //Object valueByPath = stepVariable.getValueByPath(sourceExpression);
            Object valueByPath = stepVariable.getValueByPath(split[0]);
            log.info("sourceException value:{}", JSON.toJSONString(valueByPath));
            if (!(valueByPath instanceof List)) {
                throw new IllegalArgumentException("类型错误, 表达式: " + sourceExpression + " 的值不是数组");
            }
            List<?> t = (List<?>) valueByPath;
            Object o = t.get(0);
            XYChart chart = new XYChartBuilder().width(1200).height(500).title("折线图").xAxisTitle("x轴").yAxisTitle("y轴").build();
            //背景色
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            //样式大小
            chart.getStyler().setMarkerSize(1);
            //下方
            chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
            chart.getStyler().setLegendVisible(true); // 显示图例
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            if (o instanceof Number) {
                List<Number> yData = (List<Number>) t;
                List<Integer> xData = IntStream.range(0, t.size()).boxed().collect(Collectors.toList());
                chart.addSeries("数据", xData, yData);
                chartList.add(getBytes(chart));
            } else if (o instanceof StepVariable) {
                List<StepVariable> stepVariableList = (List<StepVariable>) t;
                Map<String, List<Number>> resultMap = generateMapFromStepVariables(stepVariableList);
                List<Number> xData = new ArrayList<>();
                Iterator<Map.Entry<String, List<Number>>> iterator = resultMap.entrySet().iterator();
                if (split.length == 1) {
                    Map.Entry<String, List<Number>> first = iterator.next();
                    xData = first.getValue();
                } else {
                    xData = resultMap.get(split[1]);
                }
                List<String> yKey = new ArrayList<>();
                if (split.length > 2) {
                    String[] subArray = Arrays.copyOfRange(split, 2, split.length);
                    yKey = Arrays.asList(subArray);
                }
                while (iterator.hasNext()) {
                    Map.Entry<String, List<Number>> next = iterator.next();
                    String key = next.getKey();
                    if (yKey.isEmpty()){
                        chart.addSeries(key, xData, next.getValue());
                    }else {
                        if (yKey.contains(key)){
                            chart.addSeries(key, xData, next.getValue());
                        }
                    }
                }
                chartList.add(getBytes(chart));
            } else {
                throw new IllegalArgumentException("Unsupported type");
            }
        }
        return chartList;
    }

    private static byte[] getBytes(XYChart chart) {
        byte[] chartBytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, outputStream, BitmapEncoder.BitmapFormat.PNG);
            chartBytes = outputStream.toByteArray();
            System.out.println("图表转换为byte数组成功！");
        } catch (IOException e) {
            System.out.println("图表转换为byte数组失败：" + e.getMessage());
        }
        return chartBytes;
    }

    /**
     * 抽离出的方法，用于处理StepVariable列表并生成所需的Map结构。
     *
     * @param stepVariables 输入的StepVariable列表。
     * @return 生成的Map，键为属性名称，值为基于属性名的Number列表。
     */
    public static Map<String, List<Number>> generateMapFromStepVariables(List<StepVariable> stepVariables) {
        Map<String, List<Number>> resultMap = new HashMap<>();
        for (StepVariable stepVariable : stepVariables) {
            collectNumbers(stepVariable.getAttributes(), resultMap, "");
        }
        return resultMap;
    }

    /**
     * 递归收集数值类型的属性。
     *
     * @param attributes 当前层的属性Map。
     * @param resultMap  结果Map，聚集所有数值类型的属性。
     * @param parentKey  上一层属性的名称，用于构建完整的属性路径。
     */
    private static void collectNumbers(Map<String, StepVariable.ValueWrapper<?>> attributes, Map<String, List<Number>> resultMap, String parentKey) {
        for (Map.Entry<String, StepVariable.ValueWrapper<?>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            StepVariable.ValueWrapper<?> wrapper = entry.getValue();
            Object value = wrapper.getValue();

            // 构建当前属性的完整路径名，如果有父级路径，则进行拼接。
            String currentKey = parentKey.isEmpty() ? key : parentKey + "." + key;

            if (value instanceof Number) {
                // 如果值是Number，将其添加到对应的List中
                resultMap.computeIfAbsent(currentKey, k -> new ArrayList<>()).add((Number) value);
            } else if (value instanceof StepVariable) {
                // 如果值是StepVariable，递归处理其属性
                collectNumbers(((StepVariable) value).getAttributes(), resultMap, currentKey);
            } else if (value instanceof List) {
                // 如果值是列表，遍历列表项，如果项是StepVariable，递归处理
                for (Object item : (List<?>) value) {
                    if (item instanceof StepVariable) {
                        collectNumbers(((StepVariable) item).getAttributes(), resultMap, currentKey);
                    }
                }
            }
        }
    }


    public static void main(String[] args) {

//         创建图表对象
        XYChart chart = new XYChartBuilder().width(400).height(200).title("折线图").xAxisTitle("X轴").yAxisTitle("Y轴").build();

        // 添加数据
        double[] xData = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yData = {1.0, 4.0, 9.0, 16.0, 25.0};
        chart.addSeries("数据", xData, yData);

        // 添加数据系列2
        double[] xData2 = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yData2 = {2.0, 4.0, 6.0, 8.0, 10.0};
        chart.addSeries("数据系列2", xData2, yData2);
        List<String> list = Arrays.asList("123", "456", "789", "666", "333");

        // 获取X坐标数组
        int[] xCoordinates = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            xCoordinates[i] = i;
        }

        // 获取Y坐标数组
        int[] yCoordinates = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            yCoordinates[i] = Integer.parseInt(list.get(i));
        }

        chart.addSeries("数据3", xCoordinates, yCoordinates);
        chart.addSeries("数据4", new int[]{66, 77, 88, 99});

        // 打印X坐标数组和Y坐标数组
        System.out.println("X坐标数组：" + Arrays.toString(xCoordinates));
        System.out.println("Y坐标数组：" + Arrays.toString(yCoordinates));

        // 自定义图表样式
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        // 显示图表
        new SwingWrapper<>(chart).displayChart();

//        try {
//            BitmapEncoder.saveBitmap(chart, "1.png", BitmapEncoder.BitmapFormat.PNG);
//            System.out.println("图表保存成功！");
//        } catch (IOException e) {
//            System.out.println("图表保存失败：" + e.getMessage());
//        }

        // 将图表转换为byte数组
        byte[] chartBytes = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, outputStream, BitmapEncoder.BitmapFormat.PNG);
            chartBytes = outputStream.toByteArray();
            System.out.println("图表转换为byte数组成功！");
        } catch (IOException e) {
            System.out.println("图表转换为byte数组失败：" + e.getMessage());
        }

        // 将byte数组保存到本地文件
        try (FileOutputStream outputStream = new FileOutputStream("1.png")) {
            outputStream.write(chartBytes);
            System.out.println("图表保存成功！");
        } catch (IOException e) {
            System.out.println("图表保存失败：" + e.getMessage());
        }
    }
}

