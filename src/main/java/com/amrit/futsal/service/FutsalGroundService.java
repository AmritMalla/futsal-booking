package com.amrit.futsal.service;

import com.amrit.futsal.dto.FutsalGroundRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.exception.DuplicateResourceException;
import com.amrit.futsal.exception.ResourceNotFoundException;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.FutsalGroundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FutsalGroundService {

    private final FutsalGroundRepository futsalGroundRepository;
    private final FutsalCompanyRepository futsalCompanyRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public FutsalGroundService(FutsalGroundRepository futsalGroundRepository,
                               FutsalCompanyRepository futsalCompanyRepository,
                               FileStorageService fileStorageService) {
        this.futsalGroundRepository = futsalGroundRepository;
        this.futsalCompanyRepository = futsalCompanyRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FutsalGround createFutsalGround(FutsalGroundRequest request) {
        log.info("Creating futsal ground with name: {}", request.getName());
        // Check if ground name already exists
        if (futsalGroundRepository.findByName(request.getName()).isPresent()) {
            log.error("Duplicate resource exception: FutsalGround with name {} already exists", request.getName());
            throw new DuplicateResourceException("FutsalGround", "name", request.getName());
        }

        FutsalCompany company = futsalCompanyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> {
                    log.error("FutsalCompany not found with ID: {}", request.getCompanyId());
                    return new ResourceNotFoundException("FutsalCompany", "id", request.getCompanyId());
                });

        FutsalGround ground = new FutsalGround();
        ground.setCompany(company);
        ground.setName(request.getName());
        ground.setSurfaceType(request.getSurfaceType());
        ground.setPricePerHour(request.getPricePerHour());
        ground.setImageUrl(request.getImageUrl());

        return futsalGroundRepository.save(ground);
    }

    public FutsalGround createFutsalGround(FutsalGround futsalGround) {
        log.info("Creating futsal ground with name: {}", futsalGround.getName());
        return futsalGroundRepository.save(futsalGround);
    }

    public Optional<FutsalGround> getFutsalGroundById(UUID groundId) {
        return futsalGroundRepository.findById(groundId);
    }

    public Optional<FutsalGround> getFutsalGroundByName(String name) {
        return futsalGroundRepository.findByName(name);
    }

    public List<FutsalGround> getFutsalGroundsByCompanyId(UUID companyId) {
        return futsalGroundRepository.findByCompanyId(companyId);
    }

    public List<FutsalGround> getFutsalGroundsBySurfaceType(String surfaceType) {
        return futsalGroundRepository.findBySurfaceType(surfaceType);
    }

    public Page<FutsalGround> getAllFutsalGrounds(Pageable pageable) {
        return futsalGroundRepository.findAll(pageable);
    }

    @Transactional
    public FutsalGround updateGroundImage(UUID groundId, MultipartFile file) {
        FutsalGround ground = futsalGroundRepository.findById(groundId)
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", groundId));

        // Delete old image if exists
        if (ground.getImageUrl() != null && !ground.getImageUrl().isEmpty()) {
            String oldFileName = ground.getImageUrl().substring(ground.getImageUrl().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        // Store new image
        String fileName = fileStorageService.storeFile(file);
        ground.setImageUrl("/api/v1/files/" + fileName);

        return futsalGroundRepository.save(ground);
    }

    @Transactional
    public FutsalGround updateFutsalGround(UUID groundId, FutsalGroundRequest request) {
        FutsalGround ground = futsalGroundRepository.findById(groundId)
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", groundId));

        // Check if new name already exists for a different ground
        if (!ground.getName().equals(request.getName())) {
            if (futsalGroundRepository.findByName(request.getName()).isPresent()) {
                throw new DuplicateResourceException("FutsalGround", "name", request.getName());
            }
            ground.setName(request.getName());
        }

        if (request.getSurfaceType() != null) {
            ground.setSurfaceType(request.getSurfaceType());
        }
        if (request.getPricePerHour() != null) {
            ground.setPricePerHour(request.getPricePerHour());
        }
        if (request.getImageUrl() != null) {
            ground.setImageUrl(request.getImageUrl());
        }

        return futsalGroundRepository.save(ground);
    }

    public void deleteFutsalGround(UUID groundId) {
        FutsalGround ground = futsalGroundRepository.findById(groundId)
                .orElseThrow(() -> new ResourceNotFoundException("FutsalGround", "id", groundId));

        // Delete image if exists
        if (ground.getImageUrl() != null && !ground.getImageUrl().isEmpty()) {
            String fileName = ground.getImageUrl().substring(ground.getImageUrl().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(fileName);
        }

        futsalGroundRepository.deleteById(groundId);
    }

    public List<FutsalGround> searchGrounds(String location, String surfaceType,
                                            BigDecimal minPrice, BigDecimal maxPrice) {
        return futsalGroundRepository.searchGrounds(location, surfaceType, minPrice, maxPrice);
    }
}
