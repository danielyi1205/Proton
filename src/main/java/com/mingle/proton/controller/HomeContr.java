package com.mingle.proton.controller;

import com.mingle.proton.model.Page;
import com.mingle.proton.model.RequestHead;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Daniel on 2015/7/13.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
@Controller
public class HomeContr {
    @RequestMapping("/login")
    public String login (WebRequest webRequest, HttpServletRequest request, HttpServletResponse response, Model model) {
        Enumeration<String> headerNames =  request.getHeaderNames();
        Map<String, String> headMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            String head = request.getHeader(headName);
            headMap.put(headName,head);
        }
        String path = webRequest.getContextPath();
        model.addAttribute("reqHeads", headMap);
        return "/index";
    }

    @RequestMapping("/login/info.json")
    @ResponseBody
    public Page loginInfo (HttpServletRequest request) {
        Enumeration<String> headerNames =  request.getHeaderNames();
        Page<RequestHead> content = new Page<>();
        content.setRows(new ArrayList<RequestHead>());
        Map<String, String> headMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            String head = request.getHeader(headName);
            content.getRows().add(new RequestHead(headName,head));
        }
        content.setTotal(content.getRows().size());
        return content;
    }
}
