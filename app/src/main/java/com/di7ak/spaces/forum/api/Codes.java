package com.di7ak.spaces.forum.api;

public class Codes {
	
	public static String getByCode(int code) {
		if(code == -2) return "Некорректный ответ сервера";
		if(code == -1) return "Ошибка подключения";
		else if(code == 1) return "Требуется ввод каптчи";
		else if(code == 3) return "Пользователь не найден";
		else if(code == 25) return "Пользователь заблокирован";
		else if(code == 1003) return "Неверный логин или пароль";
		else return "Ошибка код: " + code;
	}
}
