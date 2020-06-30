package org.folio.rest.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.folio.rest.model.ReferenceLink;
import org.folio.rest.model.repo.ReferenceLinkRepo;
import org.folio.rest.model.response.BatchReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/referenceLinks")
public class ReferenceLinkController {

  private static final Logger logger = LoggerFactory.getLogger(ReferenceLinkController.class);

  @Autowired
  private ReferenceLinkRepo referenceLinkRepo;

  @Value("${data-extractor.batch.partition-size:10}")
  public int partitionSize;

  @Transactional
  @PostMapping("/batch")
  public BatchReport batchCreate(@RequestBody List<ReferenceLink> referenceLinks) throws IOException {
    long startTime = System.nanoTime();
    batches(referenceLinks, partitionSize).parallel().forEach(batch -> referenceLinkRepo.saveAll(batch));
    long stopTime = System.nanoTime();
    double duration = (stopTime - startTime) / (double) 1000000;
    logger.info("Created {} ReferenceLinks in {} milliseconds", referenceLinks.size(), duration);
    return new BatchReport(referenceLinks.size());
  }

  @PostMapping(value = "/list/{typeName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReferenceLink> lookupByTypeNameAndExternalReferences(@PathVariable String typeName,
      @RequestBody List<String> externalReferences) {
    logger.info("Lookup by type {} and external references", typeName);
    return referenceLinkRepo.findAllByTypeNameAndExternalReferenceIn(typeName, externalReferences);
  }

  public static <T> Stream<List<T>> batches(List<T> source, int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("length = " + length);
    }
    int size = source.size();
    if (size <= 0) {
      return Stream.empty();
    }
    int fullChunks = (size - 1) / length;
    return IntStream.range(0, fullChunks + 1)
      .mapToObj(n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
  }

}