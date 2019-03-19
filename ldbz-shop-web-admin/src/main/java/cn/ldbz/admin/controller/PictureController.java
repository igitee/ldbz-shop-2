package cn.ldbz.admin.controller;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.ldbz.utils.FastJsonConvert;
import cn.ldbz.utils.StorageService;

/**
 * 图片上传Controller
 * Created by cynicism on 17/1/26.
 */
@Controller
public class PictureController {

    private static Logger logger = LoggerFactory.getLogger(PictureController.class);

//    @Value("${fastdfs.base.url}")
//    private String FASTDFS_BASE_URL;
    
//    @Autowired
    private StorageService storageService;

    @RequestMapping("/pic/upload")
    @ResponseBody
    public String picUpload(MultipartFile uploadFile, MultipartFile wangEditorH5File) {
        if (uploadFile != null) {

            String oName = uploadFile.getOriginalFilename();

            String extName = oName.substring(oName.indexOf(".") + 1);

            HashMap<String, Object> map = new HashMap<>();

            try {
                // String uploadUrl = FastDFSClientUtils.upload(uploadFile.getBytes(), extName);
            	String uploadUrl = storageService.upload(uploadFile.getBytes(), extName);
                map.put("success", "上传成功");
                map.put("url", uploadUrl);

            } catch (IOException e) {
                logger.error("图片上传失败！");

                map.put("error", 1);
                map.put("message", "图片上传失败");
            }
            return FastJsonConvert.convertObjectToJSON(map);
        } else if (wangEditorH5File != null) {
            String oName = wangEditorH5File.getOriginalFilename();

            String extName = oName.substring(oName.indexOf(".") + 1);

            try {
                //String uploadUrl = FastDFSClientUtils.upload(wangEditorH5File.getBytes(), extName);
            	String uploadUrl = storageService.upload(wangEditorH5File.getBytes(), extName);
                String url = uploadUrl;

                return url;
            } catch (IOException e) {
                logger.error("图片上传失败！");

                String error = "error|服务器端错误";

                return error;
            }
        }

        return "";
    }
}
