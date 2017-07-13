package com.parking.model.business.impl;

import com.parking.model.business.service.DAO;
import com.parking.model.entities.Item;
import org.hibernate.HibernateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Yurii on 17.01.2017.
 */
public class SQLDAO<T extends Item<T>> implements DAO<T> {
    public abstract class Action {
        private Object [] values=null;
        public abstract Object act(EntityManager entityManager) throws HibernateException;

        public Object [] getValues() {
            return values;
        }

        public void setValues(Object... values) {
            this.values = values;
        }
    }
    Class<T> tClass;
    String tableName;

    protected Action getter = new Action(){

        @Override
        public Object act(EntityManager entityManager) throws HibernateException{
            TypedQuery<T> query = entityManager.createQuery("SELECT t FROM " + tableName + " t where t.enabled=:e and t.id=:id",tClass);
            query.setParameter("id",(Long)getValues()[0]);
            query.setParameter("e",1);
            return query.getSingleResult();
        }

    };
    protected Action allGetter = new Action(){

        @Override
        public Object act(EntityManager entityManager) throws HibernateException{
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(tClass);
            Root<T> from = cq.from(tClass);

            ParameterExpression<Integer> enabled=cb.parameter(Integer.class);
            cq.select(from).where(cb.equal(from.get("enabled"),enabled));
            TypedQuery<T> q = entityManager.createQuery(cq);
            q.setParameter(enabled,1);
            return q.getResultList();
        }

    };
    protected Action updater=new Action(){

        @Override
        public Object act(EntityManager entityManager) throws HibernateException{
            T newItem=(T)getValues()[0];
            entityManager.merge(newItem);
//
            return null;
        }


    };
    protected Action adder=new Action(){

        @Override
        public Object act(EntityManager entityManager) throws HibernateException{
            Iterable<T> items=(Iterable<T>)getValues()[0];
            Long i=new Long(0);
            List<Long> ids=new ArrayList<>();
            for (T item : items) {
                entityManager.persist(item);
                ids.add(item.getId());
                if( i % 50 == 0 ) { // Same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    entityManager.flush();
                    entityManager.clear();
                }
                i++;
            }
            return ids;
        }

    };
    protected Action remover=new Action(){
        @Override
        public Object act(EntityManager entityManager) throws HibernateException{
            Item<T> item = (Item<T>) getValues()[0];
            item=entityManager.merge(item);
            item.setEnabled(0);
            return null;
        };

    };
    public SQLDAO(Class<T> tClass) {
        this.tClass=tClass;
        this.tableName=tClass.getSimpleName();
    }

    public Object setDB (Action action){
        EntityManagerFactory entityManagerFactory= HibernateJPAUtil.getEntityManagerFactory();
        EntityManager entityManager=null;
        Object result=null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            result = action.act(entityManager);
            entityManager.getTransaction().commit();
        }
        catch(HibernateException e){
            entityManager.getTransaction().rollback();
            e.printStackTrace();
        }
        finally {
            try {
                entityManager.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public List<Long> add(Iterable<T> items) {
        adder.setValues(items);
        return (List<Long>)setDB(adder);
    }

    @Override
    public Long add(T item) {
        return add(Collections.singletonList(item)).get(0);
    }

    @Override
    public void update(T item){
        updater.setValues(item);
        setDB(updater);
    };



    @Override
    public void remove(T item) {
        remover.setValues(item);
        setDB(remover);


    }



    @Override
    public T get(Long id) {

        getter.setValues(id);
        return (T)setDB(getter);

    }

    @Override
    public List<T> getAll() {

        return (List<T>)setDB(allGetter);

    }


}
