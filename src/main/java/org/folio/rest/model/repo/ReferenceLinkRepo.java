package org.folio.rest.model.repo;

import java.util.List;
import java.util.Optional;

import org.folio.rest.model.ReferenceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ReferenceLinkRepo extends JpaRepository<ReferenceLink, String> {

  public Boolean existsByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public Optional<ReferenceLink> findByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByTypeId(@Param("typeId") String typeId);

  public List<ReferenceLink> findAllByTypeName(@Param("typeName") String typeName);

  public List<ReferenceLink> findAllByExternalReference(@Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByFolioReference(@Param("folioReference") String folioReference);

  public List<ReferenceLink> findAllByTypeNameAndExternalReferenceIn(@Param("typeName") String typeName,
      @Param("externalReferences") List<String> externalReferences);

  public List<ReferenceLink> findAllByTypeNameAndFolioReferenceIn(@Param("typeName") String typeName,
      @Param("folioReferences") List<String> folioReferences);

}