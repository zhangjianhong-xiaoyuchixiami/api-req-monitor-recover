package org.qydata.main;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.qydata.po.ApiBan;
import org.qydata.tools.SendEmail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonhn on 2017/4/19.
 */
public class Entrance {

    private static String [] to  = {"ld@qianyandata.com","it@qianyandata.com","zhangjianhong@qianyandata.com"};
    //private static String [] to  = {"zhangjianhong@qianyandata.com"};

    public static SqlSession masterSqlSession(){
        //String resource_master = "mybatis_master_test.xml";
        String resource_master = "mybatis_master.xml";
        InputStream is = Entrance.class.getClassLoader().getResourceAsStream(resource_master);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session = sessionFactory.openSession();
        return session;
    }

    public static SqlSession slaveSqlSession(){
        //String resource_slave = "mybatis_slave_test.xml";
        String resource_slave = "mybatis_slave.xml";
        InputStream is = Entrance.class.getClassLoader().getResourceAsStream(resource_slave);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session = sessionFactory.openSession();
        return session;
    }

    public static void main(String[] args) {

        List<ApiBan> apiBanList = queryAllApiBan();
        if (apiBanList != null && apiBanList.size() > 0){
            for (int i = 0; i < apiBanList.size() ; i++) {
                ApiBan apiBan = apiBanList.get(i);
                String product = "";
                String ts = "";
                if (apiBan != null){
                    if (apiBan.getPartnerId() != null){
                        product =  apiBan.getTypeName() + "@" + apiBan.getVendorName() + "-" + apiBan.getPartnerName() ;
                    }else {
                        product = apiBan.getTypeName() + "@" + apiBan.getVendorName();
                    }
                    if (apiBan.getTs() != null){
                        ts = apiBan.getTs();
                    }
                    String title = product + "恢复提醒";
                    String content = product +"已恢复，恢复时间：" +ts;
                    try {
                        SendEmail.sendMail(to,title,content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateApiMonitorRecover(apiBan.getApiId());
                    updateApiMonitor(apiBan.getApiId());

                }
            }
        }

    }


    public static List<ApiBan> queryAllApiBan(){
        SqlSession session = slaveSqlSession();
        String queryAllApiBan = "org.qydata.mapper.ApiBanMapper.queryAllApiBan";
        List<ApiBan> apiBanList = session.selectList(queryAllApiBan);
        session.close();
        return apiBanList;
    }

    public static void updateApiMonitorRecover(Integer apiId){
        SqlSession session = masterSqlSession();
        String updateApiMonitorRecover = "org.qydata.mapper.ApiBanMapper.updateApiMonitorRecover";
        Map<String, Object> param = new HashMap<>();
        param.put("apiId", apiId);
        param.put("recoverFlag", 0);
        param.put("sendFlag", 1);
        session.update(updateApiMonitorRecover,param);
        session.commit();
        session.close();
    }

    public static void updateApiMonitor(Integer apiId){
        SqlSession session = masterSqlSession();
        String updateApiMonitor = "org.qydata.mapper.ApiBanMapper.updateApiMonitor";
        Map<String, Object> updateParam = new HashMap<>();
        updateParam.put("apiId", apiId);
        updateParam.put("lastFc", 0);
        session.update(updateApiMonitor, updateParam);
        session.commit();
        session.close();
    }

}
