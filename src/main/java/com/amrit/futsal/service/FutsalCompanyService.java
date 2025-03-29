package com.amrit.futsal.service;

import com.amrit.futsal.entity.FutsalCompany;
import com.amrit.futsal.repository.FutsalCompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<FutsalCompany> getFutsalCompanyById(Long futsalId) {
        return futsalCompanyRepository.findById(futsalId);
    }

    public List<FutsalCompany> getFutsalCompaniesByUserId(Long userId) {
        return futsalCompanyRepository.findByUser_UserId(userId);
    }

    public List<FutsalCompany> getAllFutsalCompanies() {
        return futsalCompanyRepository.findAll();
    }

    public void deleteFutsalCompany(Long futsalId) {
        futsalCompanyRepository.deleteById(futsalId);
    }
}
