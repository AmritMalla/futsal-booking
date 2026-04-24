package com.amrit.futsal.service;

import com.amrit.futsal.dto.FutsalCompanyRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FutsalCompanyService {

    private final FutsalCompanyRepository futsalCompanyRepository;
    private final UserRepository userRepository;

    @Autowired
    public FutsalCompanyService(FutsalCompanyRepository futsalCompanyRepository, UserRepository userRepository) {
        this.futsalCompanyRepository = futsalCompanyRepository;
        this.userRepository = userRepository;
    }

    public FutsalCompany createFutsalCompany(FutsalCompanyRequest request) {
        log.info("Creating futsal company with name: {}", request.getName());
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> {
                    log.error("Owner not found with ID: {}", request.getOwnerId());
                    return new IllegalArgumentException("Owner not found: " + request.getOwnerId());
                });
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName(request.getName());
        company.setLocation(request.getLocation());
        return futsalCompanyRepository.save(company);
    }

    public FutsalCompany createFutsalCompany(FutsalCompany futsalCompany) {
        log.info("Creating futsal company with name: {}", futsalCompany.getName());
        return futsalCompanyRepository.save(futsalCompany);
    }

    public FutsalCompany updateFutsalCompany(UUID companyId, FutsalCompanyRequest request) {
        FutsalCompany company = futsalCompanyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + request.getOwnerId()));
        company.setOwner(owner);
        company.setName(request.getName());
        company.setLocation(request.getLocation());
        return futsalCompanyRepository.save(company);
    }

    public Optional<FutsalCompany> getFutsalCompanyById(UUID companyId) {
        return futsalCompanyRepository.findById(companyId);
    }
    
    public Optional<FutsalCompany> getFutsalCompanyByName(String name) {
        return futsalCompanyRepository.findByName(name);
    }

    public List<FutsalCompany> getFutsalCompaniesByOwnerId(UUID ownerId) {
        return futsalCompanyRepository.findByOwnerId(ownerId);
    }

    public Page<FutsalCompany> getAllFutsalCompanies(Pageable pageable) {
        return futsalCompanyRepository.findAll(pageable);
    }

    public void deleteFutsalCompany(UUID companyId) {
        futsalCompanyRepository.deleteById(companyId);
    }
}
