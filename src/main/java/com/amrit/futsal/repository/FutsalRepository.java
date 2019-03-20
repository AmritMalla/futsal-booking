package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Futsal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FutsalRepository extends JpaRepository<Futsal,Integer> {

    @Override
    List<Futsal> findAll();

    @Override
    Futsal getOne(Integer integer);

    @Override
    <S extends Futsal> S saveAndFlush(S s);

    @Override
    Optional<Futsal> findById(Integer id);

    @Override
    boolean existsById(Integer integer);

    @Override
    void delete(Futsal futsal);
}
