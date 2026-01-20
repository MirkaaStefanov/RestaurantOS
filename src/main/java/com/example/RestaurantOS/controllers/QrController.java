package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.services.impl.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrService qrService;

    @GetMapping("/generate")
    public ResponseEntity<?> generateQr(@AuthenticationPrincipal User user) {
        try {
            // Викаме сервиза, който прави проверките за membership
            String token = qrService.generateQrToken(user);

            // Връщаме JSON: { "qrCode": "uuid-string-..." }
            return ResponseEntity.ok(Map.of("qrCode", token));

        } catch (IllegalStateException e) {
            // Ако няма карта или е изтекла -> връщаме грешка 400
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. ВАЛИДИРАНЕ НА КОД (Вика се от Скенера/Турникета/Admin панела)
    // POST /api/v1/qr/validate
    // Body: { "qrCode": "uuid-string-..." }
    @PostMapping("/validate")
    public ResponseEntity<?> validateQr(@RequestBody Map<String, String> request) {
        String token = request.get("qrCode");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Липсва QR код в заявката."));
        }

        try {
            // Сервисът връща User обекта, ако всичко е наред
            // (И вече е таксувал посещението, ако е първо за деня)
            User user = qrService.validateQrToken(token);

            // Връщаме информация за дисплея на рецепцията/турникета
            // ВНИМАНИЕ: Не връщаме целия 'user' обект, за да избегнем JSON грешки
            return ResponseEntity.ok(Map.of(
                    "status", "ACCESS_GRANTED",
                    "userName", user.getName() + " " + user.getSurname(),
                    "membershipPlan", user.getCurrentMembership().getPlan().getName(),
                    "remainingVisits", user.getCurrentMembership().getRemainingVisits(), // -1 или число
                    "message", "Приятна тренировка!"
            ));

        } catch (Exception e) {
            // Ако кодът е невалиден, изтекъл или използван -> Грешка 403 (Forbidden)
            // Това ще накара екрана на скенера да светне в червено
            return ResponseEntity.status(403).body(Map.of(
                    "status", "ACCESS_DENIED",
                    "reason", e.getMessage()
            ));
        }
    }

    @PostMapping("/invalidate")
    public ResponseEntity<?> invalidateQr(@RequestBody Map<String, String> request) {
        String token = request.get("qrCode");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Липсва QR код в заявката."));
        }

        try {
            qrService.invalidateQrToken(token);
            return ResponseEntity.ok(Map.of("message", "QR кодът е анулиран успешно."));

        } catch (IllegalArgumentException e) {
            // Ако токенът не съществува в базата
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}