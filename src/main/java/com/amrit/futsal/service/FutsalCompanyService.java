package com.amrit.futsal.service;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FutsalCompanyService {

    private final FutsalCompanyRepository futsalCompanyRepository;

    @Autowired
    public FutsalCompanyService(FutsalCompanyRepository futsalCompanyRepository) {
        this.futsalCompanyRepository = futsalCompanyRepository;
    }

    public FutsalCompany createFutsalCompany(FutsalCompany futsalCompany) {
        return futsalCompanyRepository.save(futsalCompany);
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
