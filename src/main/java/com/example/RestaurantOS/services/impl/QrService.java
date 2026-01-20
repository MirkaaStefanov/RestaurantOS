package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.entity.DailyWorkout;
import com.example.RestaurantOS.models.entity.Membership;
import com.example.RestaurantOS.models.entity.QrToken;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.models.entity.Visit;
import com.example.RestaurantOS.repositories.DailyWorkoutRepository;
import com.example.RestaurantOS.repositories.MembershipRepository;
import com.example.RestaurantOS.repositories.QrTokenRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import com.example.RestaurantOS.repositories.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final MembershipRepository membershipRepository;
    private final DailyWorkoutRepository dailyWorkoutRepository;

    // --- ГЕНЕРИРАНЕ (Вика се от мобилното приложение) ---
    @Transactional
    public String generateQrToken(User user) {

        Membership membership = user.getCurrentMembership();

        if (membership == null) {
            throw new IllegalStateException("Нямате абонамент.");
        }

        // 1. Проверка за кредити
        boolean strictlyValid = membership.isValid();

        // 2. ОПТИМИЗАЦИЯ: Проверка дали има дневен запис (вместо да ровим във visits)
        // Това е много по-лека заявка към базата
        boolean hasWorkoutToday = dailyWorkoutRepository
                .findByUserAndDate(user, LocalDate.now())
                .isPresent();

        // 3. Проверка за валидност на датата
        boolean isDateValid = membership.getEndDate() == null || !LocalDate.now().isAfter(membership.getEndDate());


        if (!strictlyValid && !(hasWorkoutToday && isDateValid)) {
            throw new IllegalStateException("Нямате кредити и не сте влизали днес. Купете нова карта.");
        }

        // 4. Създаване на токена
        QrToken qrToken = QrToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .membership(membership)
                .expiresAt(LocalDateTime.now().plusSeconds(15))
                .used(false)
                .build();

        qrTokenRepository.save(qrToken);

        return qrToken.getToken();
    }

    // --- ВАЛИДИРАНЕ (Вика се от скенера/турникета) ---
    @Transactional
    public User validateQrToken(String tokenString) {

        // 1. ВАЛИДАЦИЯ НА ТОКЕНА
        QrToken qrToken = qrTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Невалиден QR код."));

        if (qrToken.isUsed()) throw new IllegalStateException("Кодът е използван!");
        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("Кодът е изтекъл.");

        User user = qrToken.getUser();
        Membership membership = qrToken.getMembership();

        // 2. ПРОВЕРКА ЗА ДНЕВЕН ЗАПИС
        LocalDate today = LocalDate.now();
        Optional<DailyWorkout> todayWorkout = dailyWorkoutRepository.findByUserAndDate(user, today);

        // 3. БИЗНЕС ЛОГИКА
        if (todayWorkout.isPresent()) {
            // --- СЛУЧАЙ А: RE-ENTRY (Вече има запис за днес) ---

            if (membership.getEndDate() != null && LocalDate.now().isAfter(membership.getEndDate())) {
                throw new IllegalStateException("Картата е изтекла по давност.");
            }

            DailyWorkout workout = todayWorkout.get();

            // ЛОГИКА ЗА ПОВТОРНА ТРЕНИРОВКА:
            // Ако е имало EndTime (значи е бил излязъл), а сега влиза пак:
            if (workout.getEndTime() != null) {
                workout.setReStartTime(LocalTime.now()); // <--- Записваме новия час на влизане
                workout.setEndTime(null);                // <--- Нулираме излизането (отново е активен)
                dailyWorkoutRepository.save(workout);
            }

            // Ако endTime е бил null (човекът е вътре и просто е излязъл за малко без checkout),
            // НЕ пипаме нищо - нито startTime, нито reStartTime.

        } else {
            // --- СЛУЧАЙ Б: NEW ENTRY (Първо влизане) ---

            if (!membership.isValid()) {
                throw new IllegalStateException("Нямате кредити или картата е изтекла.");
            }

            if (membership.getRemainingVisits() != -1) {
                membership.setRemainingVisits(membership.getRemainingVisits() - 1);
                membershipRepository.save(membership);
            }

            // Създаваме нов запис
            DailyWorkout newWorkout = DailyWorkout.builder()
                    .user(user)
                    .date(today)
                    .startTime(LocalTime.now()) // Тук ползваме основния startTime
                    .reStartTime(null)          // Няма повторно влизане още
                    .userNote("")
                    .build();
            dailyWorkoutRepository.save(newWorkout);
        }

        // 4. SECURITY LOG
        Visit visit = Visit.builder()
                .user(user)
                .membership(membership)
                .entryTime(LocalDateTime.now())
                .build();
        visitRepository.save(visit);

        // 5. ФИНАЛ
        qrToken.setUsed(true);
        qrTokenRepository.save(qrToken);

        return user;
    }

    @Transactional
    public void invalidateQrToken(String tokenString) {
        // 1. Намираме токена
        QrToken qrToken = qrTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Токенът не е намерен."));

        // 2. Ако вече е използван или изтекъл, няма какво да правим
        if (qrToken.isUsed()) {
            return;
        }

        // 3. Маркираме го като използван (така валидацията ще гръмне с "Кодът е използван!")
        qrToken.setUsed(true);

        // 4. Опционално: Можеш да "изтечеш" времето му веднага, за да е 100% сигурно
        qrToken.setExpiresAt(LocalDateTime.now());

        qrTokenRepository.save(qrToken);
    }
}