package br.com.karol.sistema.business.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.karol.sistema.domain.VerificationCode;
import br.com.karol.sistema.infra.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository repository;

    @Transactional
    public String createCode(String email) {
        this.repository.deleteByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));

        VerificationCode verificationCode = new VerificationCode()
                .setEmail(email)
                .setCode(code)
                .setExpiresAt(LocalDateTime.now().plusMinutes(15));

        this.repository.save(verificationCode);

        return code;
    }

    @Transactional
    public void validateCode(String email, String code) {
        VerificationCode verificationCode = repository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificação inválido ou não encontrado."));

        if (!verificationCode.isValid())
            throw new IllegalArgumentException("Código de verificação expirado.");

        this.repository.delete(verificationCode);
    }
}
