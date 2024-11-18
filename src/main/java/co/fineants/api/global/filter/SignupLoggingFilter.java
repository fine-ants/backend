package co.fineants.api.global.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignupLoggingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		Part profileImageFilePart = ((HttpServletRequest)request).getPart("profileImageFile");
		if (profileImageFilePart != null) {
			readPart(profileImageFilePart);
		}

		Part signupDataPart = ((HttpServletRequest)request).getPart("signupData");
		if (signupDataPart != null) {
			readPart(signupDataPart);
		}
		chain.doFilter(request, response);
	}

	private static void readPart(Part part) {
		StringBuilder sb = new StringBuilder();
		try (InputStream inputStream = part.getInputStream();
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info("name={} : {}", part.getName(), sb);
	}
}
