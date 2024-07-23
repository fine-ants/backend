package codesquad.fineants.global.util;

import org.springframework.http.ResponseCookie;

import com.google.common.net.HttpHeaders;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

	public static void setCookie(HttpServletResponse response, ResponseCookie cookie) {
		Cookie cookie1 = new Cookie(cookie.getName(), cookie.getValue());
		cookie1.setDomain(cookie.getDomain());
		cookie1.setPath(cookie.getPath());
		cookie1.setSecure(cookie.isSecure());
		cookie1.setHttpOnly(cookie.isHttpOnly());
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
