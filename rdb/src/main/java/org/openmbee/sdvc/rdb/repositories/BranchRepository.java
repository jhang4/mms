package org.openmbee.sdvc.rdb.repositories;

import java.util.Optional;
import org.openmbee.sdvc.data.domains.global.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByProject_ProjectIdAndBranchId(String projectId, String branchId);

}
