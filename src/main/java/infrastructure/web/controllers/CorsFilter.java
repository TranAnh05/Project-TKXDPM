package infrastructure.web.controllers;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/api/admin/*")
public class CorsFilter implements Filter{

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // === Đây là các header cho phép CORS ===
        
        // Cho phép origin (React app) của bạn
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); 
        
        // Các phương thức (GET, POST, v.v.)
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Các header được phép
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // Cho phép gửi cookie/credentials (nếu có)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Trình duyệt sẽ gửi một request "OPTIONS" (pre-flight) trước các lệnh PUT/DELETE
        // Chúng ta cần xử lý nó bằng cách trả về OK.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Cho request đi tiếp
            chain.doFilter(req, res);
        }
	}

}
