package _Project.Mita.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import _Project.Mita.entity.Loan;
import _Project.Mita.response.MonthlyLoanCountView;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	List<Loan> findByUser_UserIdOrderByLoanDateDesc(Long userId);

	Optional<Loan> findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(Long bookId, Long userId);

	List<Loan> findAllByOrderByLoanDateDesc();

	List<Loan> findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate date);

	@Query(value = "SELECT EXTRACT(YEAR FROM loan_date) AS loanyear, EXTRACT(MONTH FROM loan_date) AS loanmonth, COUNT(loan_id) AS loanCount " +
			"FROM loan " +
			"GROUP BY EXTRACT(YEAR FROM loan_date), EXTRACT(MONTH FROM loan_date) " +
			"ORDER BY 1 ASC, 2 ASC", nativeQuery = true)
	List<MonthlyLoanCountView> findMonthlyLoanCounts();

}
