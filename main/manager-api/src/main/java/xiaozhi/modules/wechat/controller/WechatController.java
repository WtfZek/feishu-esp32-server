package xiaozhi.modules.wechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信相关接口
 */
@Slf4j
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
@Tag(name = "微信相关接口")
public class WechatController {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${wechat.pay.appid}")
    private String appid;
    
    @Value("${wechat.pay.secret}")
    private String secret;
    
    /**
     * 获取微信openid
     */
    @GetMapping("/openid")
    @Operation(summary = "获取微信openid")
    public Result<String> getOpenid(@RequestParam("code") String code) {
        try {
            log.info("获取微信openid，code: {}", code);
            
            // 微信获取openid接口
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
            
            Map<String, String> params = new HashMap<>();
            params.put("appid", appid);
            params.put("secret", secret);
            params.put("code", code);
            
            // 发送请求，使用String接收响应
            String responseStr = restTemplate.getForObject(url, String.class, params);
            log.info("微信API返回结果: {}", responseStr);
            
            // 手动解析JSON
            Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);
            
            if (response != null && response.containsKey("openid")) {
                String openid = (String) response.get("openid");
                log.info("获取微信openid成功: {}", openid);
                return ResultUtils.success(openid);
            } else if (response != null && response.containsKey("errcode")) {
                // 微信API返回错误
                Integer errcode = (Integer) response.get("errcode");
                String errmsg = (String) response.get("errmsg");
                log.error("微信API返回错误: errcode={}, errmsg={}", errcode, errmsg);
                return ResultUtils.error("获取openid失败: " + errmsg);
            } else {
                log.error("获取微信openid失败: {}", response);
                return ResultUtils.error("获取openid失败");
            }
        } catch (Exception e) {
            log.error("获取微信openid异常", e);
            return ResultUtils.error("获取openid异常: " + e.getMessage());
        }
    }
} 