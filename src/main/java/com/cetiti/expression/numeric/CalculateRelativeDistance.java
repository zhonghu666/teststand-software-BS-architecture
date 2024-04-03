package com.cetiti.expression.numeric;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class CalculateRelativeDistance extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject lat1, AviatorObject lon1, AviatorObject lat2, AviatorObject lon2) {
        double latitude1 = toDouble(lat1,env);
        double longitude1 = toDouble(lon1,env);
        double latitude2 = toDouble(lat2,env);
        double longitude2 = toDouble(lon2,env);

        return new AviatorDouble(haversine(latitude1, longitude1, latitude2, longitude2) * 1000);
    }
    private double toDouble(AviatorObject aviatorObject, Map<String, Object> env) {
        Object value = aviatorObject.getValue(env);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else {
            throw new IllegalArgumentException("无法解析的参数类型: " + value);
        }
    }


    @Override
    public String getName() {
        return "CalculateRelativeDistance";
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径，单位为千米
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 返回距离，单位为千米
    }
}

