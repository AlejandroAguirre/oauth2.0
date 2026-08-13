package com.poc.oauth.resourceserver.service;

import java.util.List;

import com.poc.oauth.resourceserver.dto.EmpleadoDTO;

public interface EmpleadoService {
	List<EmpleadoDTO> obtenerEmpleados();
}
