package codesquad.fineants.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtils {

	public static String getAccessToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("accessToken")) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static String getRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("refreshToken")) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
