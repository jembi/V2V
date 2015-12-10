package interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import utils.LoggerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggingInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {
    LoggerUtil.logUrl(request);
    return super.preHandle(request, response, handler);
  }
}
