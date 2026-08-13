package com.poc.oauth.resourceserver.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.poc.oauth.resourceserver.dto.EmpleadoDTO;
import com.poc.oauth.resourceserver.entity.EmpleadoEntity;
import com.poc.oauth.resourceserver.repository.EmpleadoRepository;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

	private final EmpleadoRepository empleadoRepository;

	public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository) {
		this.empleadoRepository = empleadoRepository;
	}

	public List<EmpleadoDTO> obtenerEmpleados() {
		List<EmpleadoEntity> empleados = empleadoRepository.findAll();
		return empleados.stream().map(empleado -> {
			EmpleadoDTO dto = new EmpleadoDTO();
			dto.setId(empleado.getId());
			dto.setNombre(empleado.getNombre());
			dto.setApellidoPaterno(empleado.getApellidoPaterno());
			dto.setApellidoMaterno(empleado.getApellidoMaterno());
			dto.setFechaNacimiento(empleado.getFechaNacimiento());
			dto.setPuesto(empleado.getPuesto());
			return dto;
		}).toList();
	}
}
