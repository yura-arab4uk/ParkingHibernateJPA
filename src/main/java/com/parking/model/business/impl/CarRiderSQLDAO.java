package com.parking.model.business.impl;

import com.parking.model.entities.CarRider;
import org.hibernate.HibernateException;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Yurii on 18.01.2017.
 */
public class CarRiderSQLDAO extends SQLDAO<CarRider> {


    public CarRiderSQLDAO() {
        super(CarRider.class);

        remover=new Action() {
            @Override
            public Object act(EntityManager entityManager) throws HibernateException{
                CarRider carRider = (CarRider) getValues()[0];
                carRider=entityManager.merge(carRider);
                carRider.setEnabled(0);
                carRider.getCar().setEnabled(0);
                carRider.getCar().setCheckout_datetime(new Timestamp(new Date().getTime()));
                return null;
            };
        };
    }

}
