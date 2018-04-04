package demo.com.gateway.biz;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import demo.com.common.biz.BaseBiz;
import demo.com.common.constant.UserConstant;
import demo.com.gateway.constant.CommonConstant;
import demo.com.gateway.entity.Element;
import demo.com.gateway.entity.GateClient;
import demo.com.gateway.mapper.ElementMapper;
import demo.com.gateway.mapper.GateClientMapper;

/**
 * ${DESCRIPTION}
 *
 * @author wanghaobin
 * @create 2017-06-29 15:58
 */
@Service
public class GateClientBiz extends BaseBiz<GateClientMapper,GateClient> {
    @Autowired
    private ElementMapper elementMapper;
    @Override
    public void insertSelective(GateClient entity) {
        String secret = new BCryptPasswordEncoder(UserConstant.PW_ENCORDER_SALT).encode(entity.getSecret());
        entity.setSecret(secret);
        entity.setLocked(CommonConstant.BOOLEAN_NUMBER_FALSE);
        super.insertSelective(entity);
    }

    @Override
    public void updateById(GateClient entity) {
        GateClient old =  new GateClient();
        old.setId(entity.getId());
        old = mapper.selectOne(old);
        if(!entity.getSecret().equals(old.getSecret())){
            String secret = new BCryptPasswordEncoder(UserConstant.PW_ENCORDER_SALT).encode(entity.getSecret());
            entity.setSecret(secret);
        }
        super.updateById(entity);
    }

    public void modifyClientServices(int id, String services) {
        mapper.deleteClientServiceById(id);
        if(!StringUtils.isEmpty(services)){
            String[] mem = services.split(",");
            for(String m:mem){
                mapper.insertClientServiceById(id, Integer.parseInt(m));
            }
        }
    }

    public List<Element> getClientServices(int id) {
       return elementMapper.selectAuthorityElementByClientId(id+"");
    }
}
