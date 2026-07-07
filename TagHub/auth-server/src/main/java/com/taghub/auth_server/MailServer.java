package com.taghub.auth_server;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServer {

	private final JavaMailSender mailSender;

	public void sendPasswordResetMail(String toEmail, String resetCode) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("cuneytbaskurt123@gmail.com");
		message.setTo(toEmail);
		message.setSubject("TagHub - Şifre Sıfırlama Kodu 🔑");
		message.setText("Merhaba,\n\n" +
				"Şifrenizi sıfırlamak için talepte bulundunuz. " +
				"Aşağıdaki 6 haneli güvenlik kodunu uygulamaya girerek yeni şifrenizi belirleyebilirsiniz:\n\n" +
				"👉 KOD: " + resetCode + "\n\n" +
				"Bu kod güvenlik nedeniyle 5 dakika geçerlidir.\n" +
				"Eğer bu talebi siz yapmadıysanız bu maili dikkate almayınız.");

		mailSender.send(message);
	}
}
