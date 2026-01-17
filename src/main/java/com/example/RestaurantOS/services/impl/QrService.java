package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.entity.Membership;
import com.example.RestaurantOS.models.entity.QrToken;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.models.entity.Visit;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final MembershipRepository membershipRepository;

    // --- ГЕНЕРИРАНЕ (Вика се от мобилното приложение) ---
    @Transactional
    public String generateQrToken(User user) {

        // 1. Проверяваме дали потребителят изобщо има право да влезе
        Membership currentMembership = user.getCurrentMembership();

        if (currentMembership == null || !currentMembership.isValid()) {
            throw new IllegalStateException("Нямате активен абонамент! Не може да генерирате QR код.");
        }

        // 2. Създаваме уникален токен
        QrToken qrToken = QrToken.builder()
                .token(UUID.randomUUID().toString()) // Уникален стринг
                .user(user)
                .membership(currentMembership)
                .expiresAt(LocalDateTime.now().plusSeconds(15)) // ВАЖНО: Живее само 15 секунди
                .used(false)
                .build();

        qrTokenRepository.save(qrToken);

        // Връщаме само стринга (фронтендът ще го нарисува като картинка)
        return qrToken.getToken();
    }

    // --- ВАЛИДИРАНЕ (Вика се от скенера/турникета) ---
    @Transactional
    public User validateQrToken(String tokenString) {

        // 1. Валидация на токена (съществува ли, изтекъл ли е)
        QrToken qrToken = qrTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Невалиден QR код."));

        if (qrToken.isUsed()) {
            throw new IllegalStateException("Този код вече е използван!");
        }

        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Кодът е изтекъл.");
        }

        User user = qrToken.getUser();
        Membership membership = qrToken.getMembership();

        // 2. Проверка дали картата е валидна (дата, кредити)
        // ВАЖНО: Тук разчитаме на твоя метод isValid(), който написахме преди
        if (!membership.isValid()) {
            throw new IllegalStateException("Абонаментът ви е изтекъл или нямате кредити.");
        }

        // --- ЛОГИКА ЗА ЕДНОКРАТНО ТАКСУВАНЕ НА ДЕН ---

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // Днес 00:00
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // Днес 23:59:59

        // Проверяваме: Има ли този user посещение днес?
        boolean hasVisitedToday = visitRepository.existsByUserAndEntryTimeBetween(user, startOfDay, endOfDay);

        // Ако НЕ е влизал днес И картата е с кредити (не е -1 unlimited) -> Намаляваме
        if (!hasVisitedToday && membership.getRemainingVisits() != -1) {

            // Намаляваме кредита
            membership.setRemainingVisits(membership.getRemainingVisits() - 1);

            // Тъй като променяме membership, трябва да го запазим.
            // Благодарение на @PreUpdate в твоя Entity клас, valid статусът ще се обнови сам!
            membershipRepository.save(membership);
        }

        // 3. Записваме посещението (Винаги записваме, дори да е 2-ри път за деня - за статистика)
        Visit visit = Visit.builder()
                .user(user)
                .membership(membership)
                .entryTime(LocalDateTime.now())
                .build();

        visitRepository.save(visit);

        // 4. Маркираме токена като използван
        qrToken.setUsed(true);
        qrTokenRepository.save(qrToken);

        return user;
    }
}