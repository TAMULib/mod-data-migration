package org.folio.rest.model.repo;

import java.util.List;
import java.util.Optional;

import org.folio.rest.model.ReferenceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource
public interface ReferenceLinkRepo extends JpaRepository<ReferenceLink, String>, ReferenceLinkRepoCustom {

  public Optional<ReferenceLink> findByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByTypeName(@Param("typeName") String typeName);

  public List<ReferenceLink> findAllByTypeNameAndExternalReference(@Param("typeName") String typeName,
      @Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByTypeNameAndExternalReferenceIn(@Param("typeName") String typeName,
      @Param("externalReferences") List<String> externalReferences);

  public List<ReferenceLink> findAllByTypeId(@Param("typeId") String typeId);

  public List<ReferenceLink> findAllByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByFolioReference(@Param("folioReference") String folioReference);

  public List<ReferenceLink> findAllByTypeIdAndFolioReference(@Param("typeId") String typeId,
      @Param("folioReference") String folioReference);

  public Optional<ReferenceLink> findByTypeIdAndExternalReferenceAndFolioReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference, @Param("folioReference") String folioReference);

  @Transactional
  public long deleteByTypeName(@Param("typeName") String typeName);

}