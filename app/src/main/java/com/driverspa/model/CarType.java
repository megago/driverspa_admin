package com.driverspa.model;

import java.util.HashMap;
import java.util.Map;

public enum CarType {

	Mini(10),Sedan(1),Universal(11), Crossover(2),MiniJeep(30), Jeep(3), Minivan(4),Minibus(41),Moto(5),Other(6), Unknown(-1);
	private final int id;
	private static Map<Integer, CarType> map = new HashMap<Integer, CarType>();
	static {
		for (CarType carType : CarType.values()) {
			map.put(carType.id, carType);
		}
	}

	public static CarType valueOf(int id) {
		return map.get(id);
	}

	public static CarType toEnum(int enumVal) {
		try {
			return valueOf(enumVal);
		} catch (Exception ex) {
			// For error cases
			return Unknown;
		}
	}

	CarType(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public String toString(){
		return Integer.toString(id);
	}
}
