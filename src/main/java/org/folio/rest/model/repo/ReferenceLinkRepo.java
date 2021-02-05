package org.folio.rest.model.repo;

import java.util.List;
import java.util.Optional;

import org.folio.rest.model.ReferenceLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ReferenceLinkRepo extends JpaRepository<ReferenceLink, String>, ReferenceLinkRepoCustom {

  public Boolean existsByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public Optional<ReferenceLink> findByTypeIdAndExternalReference(@Param("typeId") String typeId,
      @Param("externalReference") String externalReference);

  public List<ReferenceLink> findByTypeIdAndExternalReferenceIn(@Param("typeId") String typeId,
      @Param("externalReferences") List<String> externalReferences);

  public List<ReferenceLink> findAllByTypeNameAndExternalReferenceIn(@Param("typeName") String typeName,
      @Param("externalReferences") List<String> externalReferences);

  public Long countByExternalReference(@Param("externalReference") String externalReference);

  public Long countByExternalReferenceAndTypeIdIn(@Param("externalReference") String externalReference,
        @Param("typeIds") List<String> typeIds);

  public Page<ReferenceLink> findAllByTypeId(@Param("typeId") String typeId, Pageable page);

  public Page<ReferenceLink> findAllByTypeName(@Param("typeName") String typeName, Pageable page);

  public List<ReferenceLink> findAllByExternalReference(@Param("externalReference") String externalReference);

  public List<ReferenceLink> findAllByExternalReferenceAndTypeId(@Param("externalReference") String externalReference,
      @Param("typeId") String typeId);

  public List<ReferenceLink> findAllByExternalReferenceAndTypeIdInOrderByTypeName(@Param("externalReference") String externalReference,
      @Param("typeIds") List<String> typeIds);

  public List<ReferenceLink> findAllByFolioReference(@Param("folioReference") String folioReference);

  public Optional<ReferenceLink> findAllByFolioReferenceAndTypeId(@Param("folioReference") String folioReference,
      @Param("typeId") String typeId);

  public List<ReferenceLink> findAllByFolioReferenceAndTypeIdInOrderByTypeName(@Param("folioReference") String folioReference,
      @Param("typeIds") List<String> typeIds);

}
