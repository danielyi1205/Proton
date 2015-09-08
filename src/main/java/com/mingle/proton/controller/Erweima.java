package com.mingle.proton.controller;

import com.google.zxing.common.BitMatrix;
import com.mingle.proton.utils.MatrixUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Daniel on 2015/7/10.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
@Controller
public class Erweima {

    private static final int QR_WIDTH = 90;
    private static final int QR_HEIGHT = 90;


    @RequestMapping("/erweima")
    public void dood( HttpServletRequest req, HttpServletResponse response) throws IOException {

        //生成二维码
        String text = "。。。。";

        BitMatrix matrix = MatrixUtil.toQRCodeMatrix(text, null, null);
        MatrixUtil.writeToStream(matrix, "JPEG", response.getOutputStream());
        response.getOutputStream().flush();
    }
}
