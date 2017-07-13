package com.parking.controller;

import com.parking.model.business.impl.HibernateJPAUtil;
import com.parking.model.business.impl.singleton.ParkingSingleton;
import org.hibernate.HibernateException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by Yurii on 28.01.2017.
 */
@WebListener
public class Closer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ParkingSingleton.getInstance().thread.interrupt();
        try {
            HibernateJPAUtil.getEntityManagerFactory().close();
        } catch (HibernateException e){
            e.printStackTrace();
        }

    }
}
