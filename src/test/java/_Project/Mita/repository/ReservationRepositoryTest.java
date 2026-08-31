package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.User;
import _Project.Mita.entity.enums.ReservationStatus;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;// @DataJpaTest専用の「テストデータを仕込むための道具」
    
    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        return entityManager.persist(user);
    }
    
    private Category createCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return entityManager.persist(category); 
    }

    private Book createBook(String title, Category category) {
        Book book = new Book();
        book.setTitle(title);
        book.setCategory(category);
        return entityManager.persist(book);
    }
    
    private Reservation createReservation(Book book, User user, ReservationStatus status, LocalDateTime reservedAt) {
        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setStatus(status);
        reservation.setReservedAt(reservedAt);
        return entityManager.persist(reservation);
    }
    
 //以下テスト
    
    @Test
    void 指定ユーザーの予約のみが予約日時の降順で取得できること() {
        // 1. 準備
        Category category = createCategory("教本");
        Book book = createBook("てすと", category);
        User user1 = createUser("一郎", "1ro@com");
        User user2 = createUser("二郎", "2ro@com");

        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 27, 10, 0);

        // ターゲットユーザー（user1）のデータ：日時の異なる2件
        createReservation(book, user1, ReservationStatus.WAITING, baseTime);          // 2番目に古い
        createReservation(book, user1, ReservationStatus.AVAILABLE, baseTime.plusHours(1)); // 1番新しい
        
        // 除外対象：他ユーザー（user2）のデータ
        createReservation(book, user2, ReservationStatus.WAITING, baseTime.plusHours(2));

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        List<Reservation> result = reservationRepository.findByUser_UserIdOrderByReservedAtDesc(user1.getUserId());

        // 3. 検証
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Reservation::getStatus).containsExactlyInAnyOrder(ReservationStatus.WAITING, ReservationStatus.AVAILABLE);
        assertThat(result).extracting(r -> r.getUser().getUserId()).containsOnly(user1.getUserId());
    }
    
    @Test
    void 指定した書籍ユーザー特定ステータスに合致する予約が取得できること() {
        // 1. 準備
        Category category = createCategory("漫画");
        Book book1 = createBook("漫", category);
        Book book2 = createBook("画", category);
        User user = createUser("一郎", "1ro@com");

        LocalDateTime now = LocalDateTime.now();

        // 検索条件のステータスリスト
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.WAITING, ReservationStatus.AVAILABLE);

        // 取得対象データ（書籍1、ユーザー1、ステータスがWAITING）
        createReservation(book1, user, ReservationStatus.WAITING, now);
        createReservation(book1, user, ReservationStatus.AVAILABLE, now);

        // 除外対象データ群
        createReservation(book2, user, ReservationStatus.WAITING, now);     // 書籍違い
        createReservation(book1, user, ReservationStatus.COMPLETED, now);   // ステータス違い
        createReservation(book1, user, ReservationStatus.CANCELLED, now);   // ステータス違い

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        List<Reservation> result = reservationRepository.findByBook_BookIdAndUser_UserIdAndStatusIn(
                book1.getBookId(), user.getUserId(), activeStatuses);

        // 3. 検証
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBook().getBookId()).isEqualTo(book1.getBookId());
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(result.get(1).getStatus()).isEqualTo(ReservationStatus.AVAILABLE);
        
    }

    @Test
    void 条件に合う最古の予約が1件のみ取得できること() {
        // 1. 準備
        Category category = createCategory("参考書");
        Book book = createBook("JavaS", category);
        User user1 = createUser("一郎", "1ro@com");
        User user2 = createUser("二郎", "2ro@com");

        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 27, 10, 0);

        // WAITING状態のデータを2件作成
        createReservation(book, user1, ReservationStatus.WAITING, baseTime);
        createReservation(book, user2, ReservationStatus.WAITING, baseTime.plusMinutes(30));
        createReservation(book, user2, ReservationStatus.CANCELLED, baseTime.minusHours(1));

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        Optional<Reservation> result = reservationRepository.findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(
                book.getBookId(), ReservationStatus.WAITING);

        // 3. 検証
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUserId()).isEqualTo(user1.getUserId());
        assertThat(result.get().getReservedAt()).isEqualTo(baseTime);
    }
    
    @Test
    void すべての予約が予約日時の降順で全件取得できること() {
        // 1. 準備
        Category category = createCategory("AI");
        Book book = createBook("IA", category);
        User user = createUser("一郎", "1ro@com");

        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 27, 10, 0);

        // ステータスやユーザーに関係なく3件作成
        createReservation(book, user, ReservationStatus.WAITING, baseTime);          // 3番目
        createReservation(book, user, ReservationStatus.CANCELLED, baseTime.plusHours(2)); // 1番目
        createReservation(book, user, ReservationStatus.AVAILABLE, baseTime.plusHours(1)); // 2番目

        entityManager.flush();
        entityManager.clear();

        // 2. 実行
        List<Reservation> result = reservationRepository.findAllByOrderByReservedAtDesc();

        // 3. 検証
        assertThat(result).hasSize(3);
        // 全件が「新しい日時順（降順）」に並んでいることを厳密に検証
        assertThat(result.get(0).getReservedAt()).isEqualTo(baseTime.plusHours(2));
        assertThat(result.get(1).getReservedAt()).isEqualTo(baseTime.plusHours(1));
        assertThat(result.get(2).getReservedAt()).isEqualTo(baseTime);
    }
}
