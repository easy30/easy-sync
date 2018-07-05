package com.cehome.easysync;

import sun.misc.BASE64Decoder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("restriction")
public class HTTPBasicAuthorizeFilter implements Filter{
      
    private   String user;
    private   String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void destroy() {

    }
  
    @Override  
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  
            throws IOException, ServletException {  
        boolean ok = checkHTTPBasicAuthorize(request);
        if (!ok)
        {  
            HttpServletResponse httpResponse = (HttpServletResponse) response;  
            httpResponse.setCharacterEncoding("UTF-8");    
            httpResponse.setContentType("application/json; charset=utf-8");   
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setHeader("Cache-Control", "no-store");
            httpResponse.setDateHeader("Expires", 0);
            httpResponse.setHeader("WWW-authenticate", "Basic Realm=cehome");
            httpResponse.getWriter().write("没有权限！");
            return;  
        }  
        else  
        {  
            chain.doFilter(request, response);  
        }  
    }  
  
    @Override  
    public void init(FilterConfig arg0) throws ServletException {  

    }  
      
    private boolean checkHTTPBasicAuthorize(ServletRequest request)
    {


        try  
        {  
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            if(httpRequest.getSession().getAttribute("user")!=null){
                return true;
            }
           /* //-- xiao ke
            if(httpRequest.getParameter("codeSig")!=null &&  httpRequest.getParameter("code")!=null){
                JSONObject j= XiaoKeService.getOpenUserIdByCode(XiaoKeService.getAppAccessToken(), httpRequest.getParameter("code"));
                if ("0".equals(j.getString("errorCode"))) {
                    httpRequest.getSession().setAttribute("userId",1L);
                    return true;
                }
            }*/

            String auth = httpRequest.getHeader("Authorization");  
            if ((auth != null) && (auth.length() > 6))  
            {  
                String HeadStr = auth.substring(0, 5).toLowerCase();  
                if (HeadStr.compareTo("basic") == 0)  
                {  
                    auth = auth.substring(6, auth.length());    
                    String decodedAuth = getFromBASE64(auth);  
                    if (decodedAuth != null)  
                    {  
                        String[] userArray = decodedAuth.split(":");
                        String u=userArray[0];
                        String p=userArray.length==1?"":userArray[1];
                        if(u.equals(user)&& p.equals(password)){
                            httpRequest.getSession().setAttribute("user",user);
                            return true;
                        }

                    }  
                }  
            }  
            return false;
        }  
        catch(Exception ex)  
        {
            return false;
        }  
          
    }  
      
    private String getFromBASE64(String s) {    
        if (s == null)    
            return null;    
        BASE64Decoder decoder = new BASE64Decoder();    
        try {    
            byte[] b = decoder.decodeBuffer(s);    
            return new String(b);    
        } catch (Exception e) {    
            return null;    
        }    
    }  
  
}  