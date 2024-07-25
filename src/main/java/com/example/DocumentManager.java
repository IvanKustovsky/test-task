package com.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documents;

    public DocumentManager() {
        this.documents = new HashMap<>();
    }

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        Objects.requireNonNull(document);

        boolean isUpdatingDocument = document.getId() != null;
        String id = isUpdatingDocument ? document.getId() : UUID.randomUUID().toString();
        String title = isUpdatingDocument ? document.getTitle() : generateTitle(document.getContent());
        Instant creationDate = isUpdatingDocument ? document.getCreated() : Instant.now();

        Document savedDocument = Document.builder()
                .id(id)
                .content(document.getContent())
                .author(document.getAuthor())
                .title(title)
                .created(creationDate)
                .build();

        documents.put(id, savedDocument);

        return savedDocument;
    }

    private String generateTitle(String content) {
        final int maxTitleLength = 20;
        content = content.split("\n")[0].trim();
        if (content.length() > maxTitleLength) {
            return content.substring(0, maxTitleLength);
        }
        return content;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        return documents.values().stream()
                .filter(document ->
                        (request.createdFrom == null || document.created.isAfter(request.createdFrom)) &&
                                (request.createdTo == null || document.created.isBefore(request.createdTo)) &&
                                (request.getTitlePrefixes() == null ||
                                        request.getTitlePrefixes().stream().anyMatch(prefix -> document.getTitle().startsWith(prefix))) &&
                                (request.getContainsContents() == null ||
                                        request.getContainsContents().stream().anyMatch(content -> document.getContent().contains(content))) &&
                                (request.getAuthorIds() == null ||
                                        request.getAuthorIds().contains(document.getAuthor().getId()))
                )
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}