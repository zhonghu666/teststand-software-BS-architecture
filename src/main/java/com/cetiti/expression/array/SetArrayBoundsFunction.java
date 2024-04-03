package com.cetiti.expression.array;

import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetArrayBoundsFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "SetArrayBounds";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
        List<Object> array = (List<Object>) FunctionUtils.getJavaObject(arg1, env);
        array = Lists.newArrayList(array);

        String lowerBounds = FunctionUtils.getStringValue(arg2, env);
        String upperBounds = FunctionUtils.getStringValue(arg3, env);
        Integer[] lowerBoundsArr = parseBound(lowerBounds);
        Integer[] upperBoundsArr = parseBound(upperBounds);

        array = resizeDimension(array, upperBoundsArr);
        env.put("array", array);
        return AviatorNumber.valueOf(0);

    }

    private List<Object> resizeDimension(List<Object> list, Integer[] maxIndices) {
        Object defaultValue = getDefaultValue(list);
        if (countListDimensions(list) == maxIndices.length) {
            return change(list, defaultValue, 0, maxIndices);
        } else {

            return createListByDimension(defaultValue, maxIndices);
        }
    }

    private List<Object> change(List<Object> list, Object defaultValue, int currentDimension, Integer[] maxIndices) {
        if (currentDimension >= maxIndices.length) {
            return list;
        }
        int maxIndex = maxIndices[currentDimension];

        if (list.size() > maxIndex + 1) {
            list.subList(maxIndex + 1, list.size()).clear();
        } else if (list.size() <= maxIndex) {
            for (int i = list.size(); i <= maxIndex; i++) {
                if (currentDimension < maxIndices.length - 1) {
                    List<Object> nestedList = new ArrayList<>();
                    list.add(nestedList);
                    change(nestedList, defaultValue, currentDimension + 1, maxIndices);
                } else {
                    list.add(defaultValue);
                }
            }
        }

        if (currentDimension < maxIndices.length - 1) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof List) {
                    List<Object> objList = Lists.newArrayList((List<Object>) list.get(i));
                    list.set(i, objList);
                    change(objList, defaultValue, currentDimension + 1, maxIndices);
                }
            }
        }
        return list;
    }

    private Object getDefaultValue(List<?> array) {
        if (CollectionUtils.isEmpty(array)) {
            return null;
        }
        Object o = array.get(0);
        if (o instanceof List) {
            return getDefaultValue((List<?>) o);
        } else {
            if (o instanceof Boolean) {
                return false;
            } else if (o instanceof String) {
                return "";
            } else if (o instanceof Number) {
                return 0;
            } else {
                return null;
            }
        }
    }

    private List<Object> createListByDimension(Object defaultValue, Integer[] dimensions) {
        List<Object> newList = new ArrayList<>();
        if (dimensions.length > 0) {
            int dimension = dimensions[0];
            for (int i = 0; i <= dimension; i++) {
                if (dimensions.length > 1) {
                    List<Object> nestedList = createListByDimension(defaultValue, Arrays.copyOfRange(dimensions, 1, dimensions.length));
                    newList.add(nestedList);
                } else {
                    newList.add(defaultValue);
                }
            }
        }
        return newList;
    }

    /**
     * 计算list维度
     *
     * @param list
     * @return
     */
    private int countListDimensions(List<Object> list) {
        int maxDepth = 1;
        for (Object obj : list) {
            if (obj instanceof List) {
                int depth = countListDimensions((List<Object>) obj) + 1;
                if (depth > maxDepth) {
                    maxDepth = depth;
                }
            }
        }
        return maxDepth;
    }


    private Integer[] parseBound(String bounds) {
        Pattern pattern = Pattern.compile("\\[(\\d+)]");
        Matcher matcher = pattern.matcher(bounds);
        List<Integer> boundsList = new ArrayList<>();
        while (matcher.find()) {
            boundsList.add(Integer.parseInt(matcher.group(1)));
        }
        return boundsList.toArray(new Integer[0]);
    }

    public static void main(String[] args) {

        AviatorEvaluator.addFunction(new SetArrayBoundsFunction());
        Map<String, Object> env = new HashMap<>();

        env.put("array", Arrays.asList(Arrays.asList("22", "33", "44", "77", "88"), Arrays.asList("22")));

        String expression = "SetArrayBounds(array, '[0][0]', '[1][2]')";
        System.out.println(AviatorEvaluator.execute(expression, env));
        System.out.println(env.get("array"));
    }

}
