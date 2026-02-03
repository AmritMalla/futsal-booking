package com.amrit.futsal.service;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.repository.FutsalGroundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FutsalGroundService {

    private final FutsalGroundRepository futsalGroundRepository;

    @Autowired
    public FutsalGroundService(FutsalGroundRepository futsalGroundRepository) {
        this.futsalGroundRepository = futsalGroundRepository;
    }

    public FutsalGround createFutsalGround(FutsalGround futsalGround) {
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
    
    public List<FutsalGround> getAllFutsalGrounds() {
        return futsalGroundRepository.findAll();
    }

    public void deleteFutsalGround(UUID groundId) {
        futsalGroundRepository.deleteById(groundId);
    }
}
