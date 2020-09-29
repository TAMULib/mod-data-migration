package org.folio.rest.model.repo;

import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.folio.rest.model.ReferenceLink;

public class ReferenceLinkRepoImpl implements ReferenceLinkRepoCustom {

  private static final String ID = "id";
  private static final String TYPE = "type";
  private static final String EXTERNAL_REFERENCE = "externalReference";

  private static final String JAVA_LANG__CLASS_TEMPLATE = "java.lang.%s";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Stream<ReferenceLink> streamAllByTypeIdOrderByExternalReferenceAsc(String typeId, String orderClass)
      throws ClassNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ReferenceLink> cq = cb.createQuery(ReferenceLink.class);
    Root<ReferenceLink> link = cq.from(ReferenceLink.class);
    cq.where(cb.equal(link.get(TYPE).get(ID), typeId));
    Class<?> orderByClass = Class.forName(String.format(JAVA_LANG__CLASS_TEMPLATE, orderClass));
    cq.orderBy(cb.asc(link.get(EXTERNAL_REFERENCE).as(orderByClass)));
    return entityManager.createQuery(cq).getResultStream();
  }

}
