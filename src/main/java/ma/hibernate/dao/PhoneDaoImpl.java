package ma.hibernate.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Couldn't save phone " + phone + " to DB", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return phone;
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = cb.createQuery(Phone.class);
            Root<Phone> phoneRoot = query.from(Phone.class);
            List<CriteriaBuilder.In<String>> criteriaList = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                CriteriaBuilder.In<String> predicate = cb.in(phoneRoot.get(entry.getKey()));
                for (String param : entry.getValue()) {
                    predicate.value(param);
                }
                criteriaList.add(predicate);
            }
            Predicate predicates = cb.and(criteriaList.toArray(Predicate[]::new));
            query.where(predicates);
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            String printParams = params.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()) + " ")
                    .collect(Collectors.joining());
            throw new RuntimeException("Can't find all phones with params: " + printParams, e);
        }
    }
}
