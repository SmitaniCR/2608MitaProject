package _Project.Mita.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _Project.Mita.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUser_UserIdOrderByLoanDateDesc(Long userId);

    Optional<Loan> findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(Long bookId, Long userId);

    List<Loan> findAllByOrderByLoanDateDesc();

    List<Loan> findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate date);
}
