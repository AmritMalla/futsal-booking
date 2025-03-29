package com.amrit.futsal.service;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.repository.FutsalGroundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<FutsalGround> getFutsalGroundById(Long groundId) {
        return futsalGroundRepository.findById(groundId);
    }

    public List<FutsalGround> getFutsalGroundsByFutsalId(Long futsalId) {
        return futsalGroundRepository.findByFutsalCompany_FutsalId(futsalId);
    }

    public void deleteFutsalGround(Long groundId) {
        futsalGroundRepository.deleteById(groundId);
    }
}
