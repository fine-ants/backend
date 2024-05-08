package codesquad.fineants.global.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
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

	private static void readPart(Part part) throws IOException {
		InputStream inputStream = part.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		log.info("{} : {}", part.getName(), sb);
	}
}
