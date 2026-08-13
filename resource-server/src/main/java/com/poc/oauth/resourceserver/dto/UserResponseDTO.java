package com.poc.oauth.resourceserver.dto;

import java.util.List;

public class UserResponseDTO {
	private String mensaje;
	private String usuario;
	private List<String> authorities;
	private List<EmpleadoDTO> empleados;

	public UserResponseDTO(String mensaje, String usuario, List<String> authorities, List<EmpleadoDTO> empleados) {
		this.mensaje = mensaje;
		this.usuario = usuario;
		this.authorities = authorities;
		this.empleados = empleados;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	public List<EmpleadoDTO> getEmpleados() {
		return empleados;
	}

	public void setEmpleados(List<EmpleadoDTO> empleados) {
		this.empleados = empleados;
	}
}
