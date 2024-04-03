package com.cetiti.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class JwtToken {

    private static final Logger log = LoggerFactory.getLogger(JwtToken.class);

    /**
     * token秘钥，请勿泄露，请勿随便修改 backups:JKKLJOoasdlfj
     */
    public static final String SECRET = "JKKLJOoasdlfjcetiti";
    /**
     * token 过期时间: 10天
     */
    public static final int calendarField = Calendar.DATE;
    public static final int calendarInterval = 10;

    /**
     * JWT生成Token.<br/>
     * <p>
     * JWT构成: header, payload, signature
     *
     * @param user_id 登录成功后用户user_id, 参数user_id不可传空
     */
    public static String createToken(String user_id, Long updateDate, String companyId, String username, Integer type, String companyName) throws Exception {
        Date iatDate = new Date();
        // expire time
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(calendarField, calendarInterval);
        Date expiresDate = nowTime.getTime();

        // header Map
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        //map.put("typ", "JWT");

        // build token
        // param backups {iss:Service, aud:APP}

        return JWT.create().withHeader(map) // header
                //.withClaim("iss", "Service") // payload
                //.withClaim("aud", "APP")
                .withClaim("user_id", null == user_id ? null : user_id)
                .withClaim("company_id", null == companyId ? null : companyId)
                .withClaim("company_name", null == companyName ? null : companyName)
                .withClaim("username", null == username ? null : username)
                .withClaim("user_type", null == type ? null : type)
                .withClaim("update_date", updateDate)
                .withIssuedAt(iatDate) // sign time
                .withExpiresAt(expiresDate) // expire time
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 解密Token
     *
     */
    public static Map<String, Claim> verifyToken(String token) {
        DecodedJWT jwt = null;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token);
        } catch (Exception e) {
            log.info("token校验token:" + token);
            log.info("token校验失败原因:" + e.getMessage());
            // e.printStackTrace();
            // token 校验失败, 抛出Token验证非法异常
            return null;
        }
        return jwt.getClaims();
    }

    /**
     * 根据Token获取user_id
     *
     */
    public static String getUserId(String token) {
        Map<String, Claim> claims = verifyToken(token);
        Claim user_id_claim = (claims == null ? null : claims.get("user_id"));
        if (null == user_id_claim || StringUtils.isEmpty(user_id_claim.asString())) {
            return null;
        }
        return user_id_claim.asString();
    }

    public static Integer getUserType(String token) {
        Map<String, Claim> claims = verifyToken(token);
        Claim user_type_claim = (claims == null ? null : claims.get("user_type"));
        if (null == user_type_claim) {
            return null;
        }
        return user_type_claim.asInt();
    }

    /**
     * 根据token获取companyId
     *
     */
    public static String getCompanyId(String token) {
        Map<String, Claim> claims = verifyToken(token);
        Claim company_id_claim = (claims == null ? null : claims.get("company_id"));
        if (null == company_id_claim || StringUtils.isEmpty(company_id_claim.asString())) {
            return null;
        }
        return company_id_claim.asString();
    }

    public static String getCompanyName(String token) {
        Map<String, Claim> claims = verifyToken(token);
        Claim company_name_claim = (claims == null ? null : claims.get("company_name"));
        if (null == company_name_claim || StringUtils.isEmpty(company_name_claim.asString())) {
            return null;
        }
        return company_name_claim.asString();
    }


    /*
     * 获取业务的实际companyId
     *
     * @param token     用户的token
     * @param companyId 前端传的企业id
     * @return 业务的实际companyId
     */
//    public static String getBusinessCompanyId(String token, String companyId) {
//        String myCompanyId = JwtToken.getCompanyId(token);
//        Integer userType = JwtToken.getUserType(token);
//        if (myCompanyId == null && userType != USER_TYPE.ADMIN.getType()) {
//            //如果不是超管且没绑定企业Id ,则myCompanyId置为0，使其查询不到数据
//            myCompanyId = "0";
//        }
//        return userType == USER_TYPE.ADMIN.getType() ? companyId : myCompanyId;
//    }

    /**
     * 根据token获取username
     */
    public static String getUsername(String token) {
        Map<String, Claim> claims = verifyToken(token);
        Claim username_claim = (claims == null ? null : claims.get("username"));
        if (null == username_claim || StringUtils.isEmpty(username_claim.asString())) {
            return null;
        }
        return username_claim.asString();
    }

    /**
     * 获取过期时间
     */
    public static Date getExpDate(String token) {
        Map<String, Claim> claims = verifyToken(token);
        return claims.get("exp").asDate();
    }


    public static Long getUpdateDate(String token) {
        Map<String, Claim> claims = verifyToken(token);
        if (claims != null) {
            return claims.get("update_date") == null ? null : claims.get("update_date").asLong();
        } else {
            return null;
        }
    }
	
	
	/*public static void main(String[] args){
		try {
			System.out.println(createToken(2l));
			Map<String, Claim> map = verifyToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJBUFAiLCJ1c2VyX2lkIjoiMiIsImlzcyI6IlNlcnZpY2UiLCJleHAiOjE1MzQ1NTkyMDgsImlhdCI6MTUzMzY5NTIwOH0.-qeGNqircd1JObb5ZT-hIFWmDz3Nco8bCFuxA6jyOwQ");
			System.out.println(map.toString());
			System.out.println(map.get("exp").asDate());
			//System.out.println(verifyToken("2322"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

}
