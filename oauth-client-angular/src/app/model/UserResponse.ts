import { Empleado } from "./Empleado";

export interface UserResponse {
  mensaje: string;
  usuario: string;
  authorities: string[];
  empleados: Empleado[];
}