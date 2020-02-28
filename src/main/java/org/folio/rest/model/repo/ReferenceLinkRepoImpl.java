package org.folio.rest.model.repo;

import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.folio.rest.model.ReferenceLink;
import org.folio.rest.model.response.CollectorReferenceLink;
import org.folio.rest.model.response.JoinReferenceLink;

public class ReferenceLinkRepoImpl implements ReferenceLinkRepoCustom {

  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String TYPE = "type";
  private static final String FOLIO_REFERENCE = "folioReference";
  private static final String EXTERNAL_REFERENCE = "externalReference";

  private static final String ARRAY_AGG = "array_agg";

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

  @Override
  public Stream<CollectorReferenceLink> streamAllByTypeIdCollectingTypeIdOrderByExternalReferenceAsc(String typeId,
      String collectTypeId, String orderClass) throws ClassNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<CollectorReferenceLink> cq = cb.createQuery(CollectorReferenceLink.class);

    Root<ReferenceLink> link = cq.from(ReferenceLink.class);
    Root<ReferenceLink> references = cq.from(ReferenceLink.class);

    Expression<String[]> stringAgg = cb.function(ARRAY_AGG, String[].class, references.get(FOLIO_REFERENCE));

    // @formatter:off
    cq.select(cb.construct(CollectorReferenceLink.class,
        link.get(ID),
        link.get(TYPE).get(ID),
        link.get(TYPE).get(NAME),
        link.get(FOLIO_REFERENCE),
        link.get(EXTERNAL_REFERENCE),
        stringAgg));

    cq.where(
      cb.and(cb.equal(link.get(ID), references.get(EXTERNAL_REFERENCE)),
      cb.equal(link.get(TYPE).get(ID), typeId),
      cb.equal(references.get(TYPE).get(ID), collectTypeId))
    );
    // @formatter:on

    cq.groupBy(link.get(ID));

    Class<?> orderByClass = Class.forName(String.format(JAVA_LANG__CLASS_TEMPLATE, orderClass));
    cq.orderBy(cb.asc(link.get(EXTERNAL_REFERENCE).as(orderByClass)));
    return entityManager.createQuery(cq).getResultStream();
  }

  public Stream<JoinReferenceLink> streamAllByTypeIdJoiningTypeIdOrderByExternalReferenceAsc(String typeId,
      String joinTypeId, String orderClass) throws ClassNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<JoinReferenceLink> cq = cb.createQuery(JoinReferenceLink.class);

    Root<ReferenceLink> link = cq.from(ReferenceLink.class);
    Root<ReferenceLink> reference = cq.from(ReferenceLink.class);

    // @formatter:off
    cq.select(cb.construct(JoinReferenceLink.class,
        link.get(ID),
        link.get(TYPE).get(ID),
        link.get(TYPE).get(NAME),
        link.get(FOLIO_REFERENCE),
        link.get(EXTERNAL_REFERENCE),
        reference.get(EXTERNAL_REFERENCE)));

    cq.where(
      cb.and(cb.equal(link.get(ID), reference.get(FOLIO_REFERENCE)),
      cb.equal(link.get(TYPE).get(ID), typeId),
      cb.equal(reference.get(TYPE).get(ID), joinTypeId))
    );
    // @formatter:on

    Class<?> orderByClass = Class.forName(String.format(JAVA_LANG__CLASS_TEMPLATE, orderClass));
    cq.orderBy(cb.asc(link.get(EXTERNAL_REFERENCE).as(orderByClass)));
    return entityManager.createQuery(cq).getResultStream();
  }

}