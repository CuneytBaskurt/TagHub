package com.taghub.room_server.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {

	@NotBlank(message = "Kullanıcı adı boş olamaz!")
	@Size(min = 3, max = 20, message = "Kullanıcı adı 3-20 karakter arasında olmalı!")
	private String name;

	@NotBlank(message = "Odanın kapasite değeri boş olamaz!")
	@Size(min = 1, message = "Kapasite değeri 1'den küçük olamaz!")
	private int capacity;


	private List<String> labels;

}

