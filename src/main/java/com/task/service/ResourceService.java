package com.task.service;

import com.task.dto.PagedResponse;
import com.task.dto.ResourceRequest;
import com.task.dto.ResourceResponse;
import com.task.entity.Resource;
import com.task.exception.ResourceNotFoundException;
import com.task.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        Resource entity = new Resource();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPricePerHour(request.getPricePerHour());
        entity.setAvailable(request.getAvailable() == null || request.getAvailable());

        return toDto(resourceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> getAllResources(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Resource> pageResult = resourceRepository.findAll(pageable);

        List<ResourceResponse> items = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PagedResponse<>(
                items,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPricePerHour(request.getPricePerHour());
        if (request.getAvailable() != null) {
            entity.setAvailable(request.getAvailable());
        }

        return toDto(resourceRepository.save(entity));
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource entity = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        resourceRepository.delete(entity);
    }

    public ResourceResponse toDto(Resource r) {
        return new ResourceResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getPricePerHour(),
                r.isAvailable(),
                r.getCreatedAt()
        );
    }
}
