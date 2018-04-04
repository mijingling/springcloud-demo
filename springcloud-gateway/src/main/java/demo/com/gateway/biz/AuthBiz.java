package demo.com.gateway.biz;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import demo.com.agent.vo.authority.PermissionInfo;
import demo.com.agent.vo.gate.ClientInfo;
import demo.com.common.constant.UserConstant;
import demo.com.gateway.secruity.ApiTokenUtil;
import demo.com.gateway.service.GateService;

@Service
public class AuthBiz{

    private ApiTokenUtil jwtTokenUtil;
    private GateService gateService;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(UserConstant.PW_ENCORDER_SALT);

    @Value("${gate.api.tokenHead}")
    private String tokenHead;

    @Autowired
    public AuthBiz(
            ApiTokenUtil jwtTokenUtil,
            GateService gateService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.gateService = gateService;
    }


    public String login(String clientId, String secret) {
        ClientInfo info = gateService.getGateClientInfo(clientId);
        String token = "";
        if(encoder.matches(secret,info.getSecret())) {
           token = jwtTokenUtil.generateToken(info);
        }
        return token;
    }

    public String refresh(String oldToken) {
        final String token = oldToken.substring(tokenHead.length());
        String clientId = jwtTokenUtil.getClientIdFromToken(token);
        ClientInfo info = gateService.getGateClientInfo(clientId);
        if (jwtTokenUtil.canTokenBeRefreshed(token,info.getUpdTime())){
            return jwtTokenUtil.refreshToken(token);
        }
        return null;
    }

    public Boolean validate(String oldToken,String resource) {
        if(!oldToken.startsWith(tokenHead))
            return false;
        final String token = oldToken.substring(tokenHead.length());
        String clientId = jwtTokenUtil.getClientIdFromToken(token);
        ClientInfo info = gateService.getGateClientInfo(clientId);
        return info.getCode().equals(clientId)&&!jwtTokenUtil.isTokenExpired(token)&&validateResource(clientId,resource);
    }

    public Boolean validateResource(String clientId, String resource){
        String [] res = resource.split(":");
        final String requestUri = res[0];
        final String method = res[1];
        List<PermissionInfo> clientPermissionInfo = gateService.getGateServiceInfo(clientId);
        Collection<PermissionInfo> result = Collections2.filter(clientPermissionInfo, new Predicate<PermissionInfo>() {
            @Override
            public boolean apply(PermissionInfo permissionInfo) {
                String url = permissionInfo.getUri();
                String uri = url.replaceAll("\\{\\*\\}", "[a-zA-Z\\\\d]+");
                String regEx = "^" + uri + "$";
                return (Pattern.compile(regEx).matcher(requestUri).find() || requestUri.startsWith(url + "/"))
                        && method.equals(permissionInfo.getMethod());
            }
        });
        if (result.size() <= 0) {
            return false;
        }
        return true;
    }
    
}
