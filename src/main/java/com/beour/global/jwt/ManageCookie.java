package com.beour.global.jwt;

import jakarta.servlet.http.Cookie;

public class ManageCookie {

    public static Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60); //refresh와 값 같게
        //todo : 운영 배포시에 아래 주석 활성화
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}
