package oh.publisher.repository;

import oh.publisher.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepo extends JpaRepository<Contract, Integer> {

}
