package com.example.ODC_Academy.formation;

import com.example.ODC_Academy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findByTrainersContaining(User trainer);
}
