package com.atguigu.common.jwt;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Date;

/**Jwt工具类
     1.生成token字符串
     2.token字符串获取用户id
     3.token字符串获取用户名称
 */
public class JwtHelper{

    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "123456";
    //生成token字符串  根据用户id和用户名称
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder()
                //分类
                .setSubject("AUTH-USER")
                //设置token有效时长
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                //主体部分
                .claim("userId", userId)
                .claim("username", username)
                //签名部分
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    //从生成token字符串获取用户id
    public static Long getUserId(String token) {
        try {
            if (org.springframework.util.StringUtils.isEmpty(token)) return null;

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            Integer userId = (Integer) claims.get("userId");
            return userId.longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //从生成token字符串获取用户名称
    public static String getUsername(String token) {
        try {
            if (org.springframework.util.StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //测试方法

    public static void main(String[] args) {
       RedisTemplate redisTemplate = new RedisTemplate<>();

        String token = JwtHelper.createToken(1L, "admin");
        System.out.println(token);
        /* System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUsername(token));*/
    }
}
