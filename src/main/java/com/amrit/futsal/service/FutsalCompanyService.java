package com.amrit.futsal.service;

import com.amrit.futsal.dto.FutsalCompanyRequest;
import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import com.amrit.futsal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + request.getOwnerId()));
        FutsalCompany company = new FutsalCompany();
        company.setOwner(owner);
        company.setName(request.getName());
        company.setLocation(request.getLocation());
        return futsalCompanyRepository.save(company);
    }

    public FutsalCompany createFutsalCompany(FutsalCompany futsalCompany) {
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

    public List<FutsalCompany> getAllFutsalCompanies() {
        return futsalCompanyRepository.findAll();
    }

    public void deleteFutsalCompany(UUID companyId) {
        futsalCompanyRepository.deleteById(companyId);
    }
}
